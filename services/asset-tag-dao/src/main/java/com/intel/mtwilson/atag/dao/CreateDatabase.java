/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao;

//import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.dao.jdbi.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.configuration.MapConfiguration;
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
    
    
    public void execute(String[] args) throws Exception {
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
            Statement s = c.createStatement();
            s.executeUpdate("DROP TABLE "+table);
            s.close();
        }
    }
    
    public void createTables() throws SQLException {
        DataSource ds = Derby.getDataSource();
        DBI dbi = new DBI(ds);
        
        // tag
        TagDAO tagDao = dbi.open(TagDAO.class);
        if( !Derby.tableExists("tag") ) { tagDao.create(); }        
        tagDao.close();
        
        // tag value
        TagValueDAO tagValueDao = dbi.open(TagValueDAO.class);
        if( !Derby.tableExists("tag_value") ) { tagValueDao.create(); }
        tagValueDao.close();
        
        // rdf triple
        RdfTripleDAO rdfTripleDao = dbi.open(RdfTripleDAO.class);
        if( !Derby.tableExists("rdf_triple") ) { rdfTripleDao.create(); }
        rdfTripleDao.close();
        
        // certificate request
        CertificateRequestDAO certificateRequestDao = dbi.open(CertificateRequestDAO.class);
        if( !Derby.tableExists("certificate_request") ) { certificateRequestDao.create(); }
        certificateRequestDao.close();
        
        // certificate request tag value
        CertificateRequestTagValueDAO certificateRequestTagValueDao = dbi.open(CertificateRequestTagValueDAO.class);
        if( !Derby.tableExists("certificate_request_tag_value") ) { certificateRequestTagValueDao.create(); }
        certificateRequestTagValueDao.close();

        // certificate
        CertificateDAO certificateDao = dbi.open(CertificateDAO.class);
        if( !Derby.tableExists("certificate") ) { certificateDao.create(); }
        certificateDao.close();

        // certificate request
        SelectionDAO selectionDao = dbi.open(SelectionDAO.class);
        if( !Derby.tableExists("selection") ) { selectionDao.create(); }
        selectionDao.close();
        
        // certificate request tag value
        SelectionTagValueDAO selectionTagValueDao = dbi.open(SelectionTagValueDAO.class);
        if( !Derby.tableExists("selection_tag_value") ) { selectionTagValueDao.create(); }
        selectionTagValueDao.close();

        // configuration
        ConfigurationDAO configurationDao = dbi.open(ConfigurationDAO.class);
        if( !Derby.tableExists("configuration") ) { configurationDao.create(); }
        configurationDao.close();

        // file
        FileDAO fileDao = dbi.open(FileDAO.class);
        if( !Derby.tableExists("file") ) { fileDao.create(); }
        fileDao.close();
        
        
    }    
 
    public static void main(String args[]) throws Exception {
        CreateDatabase cmd = new CreateDatabase();
        cmd.setDropTablesEnabled(true);
        cmd.execute(new String[0]);
        
    }    
}
