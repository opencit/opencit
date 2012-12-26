/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.cmd;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.setup.Command;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.commons.configuration.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
/**
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
public class InitializeMysqlDatabase implements Command {
    
    @Override
    public void execute(String[] args) throws SetupException {
        Configuration attestationServiceConf = ASConfig.getConfiguration();
        
        // load the sql files and run them
        //InputStream in = getClass().getResourceAsStream("/bootstrap.sql");
        DataSource ds = null;
        try {
            ds = PersistenceManager.getPersistenceUnitInfo("ASDataPU").getNonJtaDataSource();
        }
        catch(IOException e) {
            throw new SetupException("Cannot load persistence unit info", e);
        }
        
        if( ds == null ) {
            throw new SetupException("Cannot load persistence unit info");
        }
        
        // check connection
        Connection c = null;
        try {
            c = ds.getConnection();  // username and password should already be set in the datasource
        }
        catch(SQLException e) {
            throw new SetupException("Cannot connect to database", e);
        }
        
        try {
            Statement s = c.createStatement();
//            ResultSet rs = s.executeQuery("SELECT @@version"); // output is 1 column (VARCHAR) with content like this: 5.1.63-0ubuntu0.11.10.1
//            ResultSet rs = s.executeQuery("SELECT version()"); // output is same as for @@version
            ResultSet rs = s.executeQuery("SELECT @@hostname"); // output is 1 column (VARCHAR) with content like this: mtwilsondev    (hostname of database server)
            if( rs.next() ) {
                int columns = rs.getMetaData().getColumnCount();
                System.out.println("Got "+columns+" columns from datbase server");
                System.out.println("First column type: "+rs.getMetaData().getColumnTypeName(1));
                System.out.println("First column: "+rs.getString(1));
            }
            rs.close();
            s.close();
        }
        catch(SQLException e) {
            throw new SetupException("Cannot check database version", e);
        }
        
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("/com/intel/mtwilson/database/mysql/bootstrap.sql")); // must specify full path to resource
        // XXX can add more calls to addScript(...)... or loop around it once each so we know which file generated which errors...
        
        try {
            rdp.populate(c);
        }
        catch(SQLException e) {
            throw new SetupException("Cannot execute SQL statements", e);
        }
        
        try {
            c.close();
        }
        catch(SQLException e) {
            throw new SetupException("Error while closing database connection", e);
        }
    }
    
}
