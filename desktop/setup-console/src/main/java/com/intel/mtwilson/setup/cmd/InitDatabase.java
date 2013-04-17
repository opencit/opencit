/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.io.Classpath;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupContext;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
/**
 * Bug #509 create java program to handle database updates and ensure that 
 * old updates (already executed) are not executed again
 * 
 * TODO: List each .sql file that is supposed to be run (index them by changelog date), check for
 * current state of database before running scripts (via the mw_changelog table) so we know
 * which scripts to run based on what has already been noted in the mw_changelog.
 * 
 * TODO:  consolidate the persistence units into ASDataPU... the MSDataPU can't really be separate from
 * the ASDataPU because audit logs need to refer to users and to whitelist data...
 * 
 * References:
 * http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
 * http://stackoverflow.com/questions/1044194/running-a-sql-script-using-mysql-with-jdbc
 * @author jbuhacoff
 */
public class InitDatabase implements Command {
    private SetupContext ctx = null;

    @Override
    public void setContext(SetupContext ctx) {
        this.ctx = ctx;
    }

    private Configuration options = null;
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws SetupException {
        // first arg:  mysql or postgres  (installer detects and invokes this command with that argument)
        if( args.length < 1 ) {
            throw new SetupException("Usage: InitializeMysqlDatabase mysql|postgres");
        }
        
        try {
            initDatabase(args[0]);
        }
        catch(Exception e) {
            throw new SetupException("Cannot setup database: "+e.toString(), e);
        }

    }
    
    public static class ChangelogEntry {
        public String id;
        public String applied_at;
        public String description;
        public ChangelogEntry() { }
        public ChangelogEntry(String id, String applied_at, String description) {
            this.id = id;
            this.applied_at = applied_at;
            this.description = description;
        }
    }
    private String vendor = null;
    private void initDatabase(String databaseVendor) throws SetupException, IOException, SQLException {
        vendor = databaseVendor;
        Map<Long,Resource> sql = getSql(databaseVendor); //  TODO change to Map<Long,Resource> and then pass it directly to the populator !!!!
        
//        Configuration attestationServiceConf = ASConfig.getConfiguration();
        DataSource ds = getDataSource();
        Connection c = ds.getConnection();  // username and password should already be set in the datasource
        List<ChangelogEntry> changelog = getChangelog(c);
        HashMap<Long,String> presentChanges = new HashMap<Long,String>(); // what is already in the database according to the changelog
        for(ChangelogEntry entry : changelog) {
            presentChanges.put(Long.valueOf(entry.id), entry.applied_at);
        }
        
        HashSet<Long> changesToApply = new HashSet<Long>(sql.keySet());
        changesToApply.removeAll(presentChanges.keySet());
        
        if( changesToApply.isEmpty() ) {
            System.out.println("No database updates available");
            return;
        }
        
        ArrayList<Long> changesToApplyInOrder = new ArrayList<Long>(changesToApply);
        Collections.sort(changesToApplyInOrder);
        
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        // removing unneeded output as user can't choice what updates to apply
        // XXX-TODO stdalex this should all be log.info
        //System.out.println("Available database updates:");
        for(Long id : changesToApplyInOrder) {
            //System.out.println(String.format("%d %s", id, basename(sql.get(id).getURL())));
            rdp.addScript(sql.get(id)); // new ClassPathResource("/com/intel/mtwilson/database/mysql/bootstrap.sql")); // must specify full path to resource
        }
        
        rdp.setContinueOnError(true);
        rdp.setIgnoreFailedDrops(true);
        rdp.setSeparator(";");
        rdp.populate(c);
        
        c.close();
    }
    
    private String getDatabaseHostname(Connection c) throws SQLException {
        String hostname = null;
        Statement s = c.createStatement();
//            ResultSet rs = s.executeQuery("SELECT @@version"); // output is 1 column (VARCHAR) with content like this: 5.1.63-0ubuntu0.11.10.1
//            ResultSet rs = s.executeQuery("SELECT version()"); // output is same as for @@version
        ResultSet rs = s.executeQuery("SELECT @@hostname"); // output is 1 column (VARCHAR) with content like this: mtwilsondev    (hostname of database server)
        if( rs.next() ) {
            int columns = rs.getMetaData().getColumnCount();
            System.out.println("Got "+columns+" columns from datbase server");
            System.out.println("First column type: "+rs.getMetaData().getColumnTypeName(1));
            System.out.println("First column: "+rs.getString(1));
            hostname = rs.getString(1); 
        }
        rs.close();
        s.close();
         return hostname;
    }
    
    /**
     * Locates the SQL files for the specified vendor, and reads them to
     * create a mapping of changelog-date to SQL content. This mapping can
     * then be used to select which files to execute against an existing
     * database.
     * See also iBatis, which we are (very) roughly emulating.
     * @param databaseVendor
     * @return 
     */
    private Map<Long,Resource> getSql(String databaseVendor) throws SetupException {
        System.out.println("Scanning for "+databaseVendor+" SQL files");
        HashMap<Long,Resource> sqlmap = new HashMap<Long,Resource>();
        try {
            Resource[] list = listResources(databaseVendor); // each URL like: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
            for(Resource resource : list) {
                URL url = resource.getURL();
//                InputStream in = url.openStream();
//                String sql = IOUtils.toString(in, "UTF-8");
//                IOUtils.closeQuietly(in);
                Long timestamp = getTimestampFromSqlFilename(basename(url));
                if( timestamp != null ) {
                    sqlmap.put(timestamp, resource);
                }
                else {
                    System.err.println("SQL filename is not in recognized format: "+url.toExternalForm());
                }
            }
        }
        catch(IOException e) {
            throw new SetupException("Error while scanning for SQL files: "+e.getLocalizedMessage(), e);
        }
        System.err.println("Number of SQL files: "+sqlmap.size());
        return sqlmap;        
    }
    
    private Resource[] listResources(String databaseVendor) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources = resolver.getResources("classpath:com/intel/mtwilson/database/"+databaseVendor+"/*.sql");
        return resources; 
        /*
        ArrayList<URL> list = new ArrayList<URL>();
        for(Resource resource : resources) {
            list.add(resource.getURL());
        }
        return list;*/
    }
    
    Pattern pTimestampName = Pattern.compile("^([0-9]+).*");
    
    /**
     * Given a URL, returns the final component filename
     * 
     * Example URL: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
     * Example output: 20121226000000_remove_created_by_patch_rc3.sql
     * @param url
     * @return 
     */
    private String basename(URL url) {
        String[] parts = StringUtils.split(url.toExternalForm(), "/");
        return parts[parts.length-1];
    }
    
    /**
     * @param filename without any path like: 20121226000000_remove_created_by_patch_rc3.sql
     * @return 
     */
    private Long getTimestampFromSqlFilename(String filename) {
        Matcher mTimestampName = pTimestampName.matcher(filename);
        if( mTimestampName.matches() ) {
            String timestamp = mTimestampName.group(1); // the timestamp like: 20121226000000
            return Long.valueOf(timestamp);
        }
        return null;
    }
    
    private void printSqlMap(Map<Long,String> sqlmap) {
        Set<Long> timestampSet = sqlmap.keySet();
        ArrayList<Long> timestampList = new ArrayList<Long>();
        timestampList.addAll(timestampSet);
        Collections.sort(timestampList);
        for(Long timestamp : timestampList) {
            System.out.println("File timestamp: "+timestamp);
        }
    }
    
    /**
     * 
     * @return datasource object for mt wilson database, guaranteed non-null
     * @throws SetupException if the datasource cannot be obtained
     */
    private DataSource getDataSource() throws SetupException {
        // load the sql files and run them
        //InputStream in = getClass().getResourceAsStream("/bootstrap.sql");
        try {
            DataSource ds = PersistenceManager.getPersistenceUnitInfo("ASDataPU", ASConfig.getJpaProperties()).getNonJtaDataSource();
            if( ds == null ) {
                throw new SetupException("Cannot load persistence unit info");
            }
            return ds;
        }
        catch(IOException e) {
            throw new SetupException("Cannot load persistence unit info", e);
        }
        
    }
    
    private List<String> getTableNames(Connection c) throws SQLException {
       ArrayList<String> list = new ArrayList<String>();
        Statement s = c.createStatement();
        String sql = "";
        if (vendor.equals("mysql")){
            sql = "SHOW TABLES";
        }
        else if (vendor.equals("postgres")){
            sql = "SELECT table_name FROM information_schema.tables;";          
        }
       
        ResultSet rs = s.executeQuery(sql);
        while(rs.next()) {
            list.add(rs.getString(1));
        }
        return list;

    }
    
    private List<ChangelogEntry> getChangelog(Connection c) throws SQLException {
        ArrayList<ChangelogEntry> list = new ArrayList<ChangelogEntry>();
        
        // first determine if we have the new changelog table `mw_changelog`, or the old one `changelog`, or none at all
        List<String> tableNames = getTableNames(c);
        boolean hasMwChangelog = false;
        boolean hasChangelog = false;
        if( tableNames.contains("mw_changelog") ) {
            hasMwChangelog = true;
        }
        if( tableNames.contains("changelog") ) {
            hasChangelog = true;
        }
        
        if( !hasChangelog && !hasMwChangelog) {
            return list; /*  empty list indicates database is not initialized and all scripts need to be executed */ 
        }
        
        String changelogTableName = null;
        // if we have both changelog tables, copy all records from old changelog to new changelog and then use that
        if( hasChangelog && hasMwChangelog ) {
            PreparedStatement check = c.prepareStatement("SELECT APPLIED_AT FROM mw_changelog WHERE ID=?");
            PreparedStatement insert = c.prepareStatement("INSERT INTO mw_changelog SET ID=?, APPLIED_AT=?, DESCRIPTION=?");
            Statement select = c.createStatement();
            ResultSet rs = select.executeQuery("SELECT ID,APPLIED_AT,DESCRIPTION FROM changelog");
            while(rs.next()) {
                check.setLong(1, rs.getLong("ID"));
                ResultSet rsCheck = check.executeQuery();
                if( rsCheck.next() ) {
                    // the id is already in the new mw_changelog table
                }
                else {
                    insert.setLong(1, rs.getLong("ID"));
                    insert.setString(2, rs.getString("APPLIED_AT"));
                    insert.setString(3, rs.getString("DESCRIPTION"));
                    insert.executeUpdate();
                }
                rsCheck.close();
            }
            rs.close();
            select.close();
            insert.close();
            check.close();
            changelogTableName = "mw_changelog"; 
        }
        else if( hasMwChangelog ) {
            changelogTableName = "mw_changelog";
        }
        else if( hasChangelog ) {
            changelogTableName = "changelog";
        }
        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(String.format("SELECT ID,APPLIED_AT,DESCRIPTION FROM %s", changelogTableName));
        while(rs.next()) {
            ChangelogEntry entry = new ChangelogEntry();
            entry.id = rs.getString("ID");
            entry.applied_at = rs.getString("APPLIED_AT");
            entry.description = rs.getString("DESCRIPTION");
            list.add(entry);
        }
        rs.close();
        s.close();
        return list;
    }
    
    
}
