/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jooq;

import com.intel.mtwilson.tag.dao.jdbi.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import javax.sql.DataSource;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class CreateDatabase {
    private static Logger log = LoggerFactory.getLogger(CreateDatabase.class);
    
    private boolean dropTables = false;
    
    public boolean isDropTablesEnabled() {
        return dropTables;
    }
    public void setDropTablesEnabled(boolean enabled) {
        dropTables = enabled;
    }
    
    /**
     * Uses the system property "derby.system.home" when configuring derby.  see also http://db.apache.org/derby/docs/10.4/tuning/rtunproper32066.html
     * You can set derby.system.home=${project.build.directory}/derby in your maven config, or -Dderby.system.home=target\derby on command line.
     * @param args
     * @throws Exception 
     */
    public void execute(String[] args) throws SQLException {
//        Derby.protocol = "jdbc:derby:directory:mytestdb;create=true"; // was:   jdbc:derby:directory:target/derby/mytestdb;create=true
        log.debug("Starting Derby...");
        Derby.startDatabase();
        log.debug("Derby started");
        if( dropTables ) {
            log.debug("Deleting database tables...");
            dropTables();
            log.info("Deleted database tables");
        }
        log.debug("Creating database tables...");
        createTables();
        log.info("Created database tables");
        log.debug("Stopping Derby...");
        Derby.stopDatabase();
        log.debug("Derby stopped");
    }
    
    public void dropTables() throws SQLException {
        Connection c = Derby.getConnection();
        Set<String> tables = Derby.listTablesAndViews(c);
        for(String table : tables) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("DROP TABLE "+table);
            }
        }
    }
    
    /* 
    private <T extends GenericDAO> void createTable(String name, Class<T> daoClass, DBI dbi) {
        T dao = dbi.open(daoClass);
        if( !Derby.tableExists(name) ) { dao.create(); }        
        dao.close();
        
    }
    */
    
    public void createTables() throws SQLException {
        DataSource ds = Derby.getDataSource();
        DBI dbi = new DBI(ds);
        
        // tag
        KvAttributeDAO tagDao = dbi.open(KvAttributeDAO.class);
        if( !Derby.tableExists("mw_tag_kvattribute") ) { tagDao.create(); }        
        tagDao.close();
        
        // tag value
        SelectionKvAttributeDAO tagValueDao = dbi.open(SelectionKvAttributeDAO.class);
        if( !Derby.tableExists("mw_tag_selection_kvattribute") ) { tagValueDao.create(); }
        tagValueDao.close();
        
        // certificate request
        CertificateRequestDAO certificateRequestDao = dbi.open(CertificateRequestDAO.class);
        if( !Derby.tableExists("mw_tag_certificate_request") ) { certificateRequestDao.create(); }
        certificateRequestDao.close();
        

        // certificate
        CertificateDAO certificateDao = dbi.open(CertificateDAO.class);
        if( !Derby.tableExists("mw_tag_certificate") ) { certificateDao.create(); }
        certificateDao.close();

        // certificate request
        SelectionDAO selectionDao = dbi.open(SelectionDAO.class);
        if( !Derby.tableExists("mw_tag_selection") ) { selectionDao.create(); }
        selectionDao.close();
        

        // configuration
        ConfigurationDAO configurationDao = dbi.open(ConfigurationDAO.class);
        if( !Derby.tableExists("mw_configuration") ) { configurationDao.create(); }
        configurationDao.close();

        // file
        FileDAO fileDao = dbi.open(FileDAO.class);
        if( !Derby.tableExists("mw_file") ) { fileDao.create(); }
        fileDao.close();
        
        TpmPasswordDAO tpmPasswordDao = dbi.open(TpmPasswordDAO.class);
        if( !Derby.tableExists("mw_host_tpm_password")) { tpmPasswordDao.create();}
        tpmPasswordDao.close();
        
    }    
 
    public static void main(String args[]) throws SQLException {
        CreateDatabase cmd = new CreateDatabase();
        cmd.setDropTablesEnabled(true);
        cmd.execute(new String[0]);
        
    }    
}
