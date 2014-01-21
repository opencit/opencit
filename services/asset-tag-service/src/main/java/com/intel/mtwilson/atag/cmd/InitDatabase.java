/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.model.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command creates a default "main" configuration and TODO: create the default asset tag authority
 * @author jbuhacoff
 */
public class InitDatabase extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(InitDatabase.class);
    public final static String OID_CUSTOMER_ROOT = "1.3.6.1.4.1.99999";
    
    private    ConfigurationDAO configurationDao;
    private    TagDAO tagDao;
    private    TagValueDAO tagValueDao;
    private    SelectionDAO selectionDao;
    private    SelectionTagValueDAO selectionTagValueDao;
    private    RdfTripleDAO rdfTripleDao;
    
    private String oid(String relative) {
        return OID_CUSTOMER_ROOT+"."+relative;
    }
    
    @Override
    public void execute(String[] args) throws Exception {
        log.debug("Starting Derby...");
        Derby.startDatabase();
        log.debug("Derby started");
        try {
            open();
            if( isDatabaseEmpty() ) {
                insertMainConfiguration();            
            }
        }
        catch(Exception e) {
            log.error("Cannot initialize database", e);
        }
        finally {
            close();
        }
        log.debug("Stopping Derby...");
        Derby.stopDatabase();
        log.debug("Derby stopped");
    }
    
    public void open() throws SQLException {
        log.debug("get daos");
         configurationDao = Derby.configurationDao();
         tagDao = Derby.tagDao();
         tagValueDao = Derby.tagValueDao();
         selectionDao = Derby.selectionDao();
         selectionTagValueDao = Derby.selectionTagValueDao();
         rdfTripleDao = Derby.rdfTripleDao();        
    }
    
    public void close() {
        // close daos'
        if(configurationDao!=null) {configurationDao.close(); configurationDao = null;}
        if(tagDao!=null) {tagDao.close(); tagDao = null; }
        if(tagValueDao!=null) {tagValueDao.close(); tagValueDao = null;}
        if(selectionDao!=null) {selectionDao.close(); selectionDao = null; }
        if(selectionTagValueDao!=null) {selectionTagValueDao.close(); selectionTagValueDao = null;        }
    }
    
    public boolean isDatabaseEmpty() throws SQLException, IOException {
        Configuration main = configurationDao.findByName("main");
        if( main == null ) {
            return true;
        }
        return false;
    }
    
    public void insertMainConfiguration() throws SQLException, IOException {
        // open daos
        // example tags
        log.debug("inserting tags");
        long countryTagId = tagDao.insert(new UUID(), "country", oid("1"));
        long stateTagId = tagDao.insert(new UUID(), "state", oid("2"));
        long cityTagId = tagDao.insert(new UUID(), "city", oid("3"));
        long customerTagId = tagDao.insert(new UUID(), "customer", oid("4"));
        long buildingTagId = tagDao.insert(new UUID(), "building", oid("5"));
        long[] countryTagValueIds = new long[3];
        countryTagValueIds[0] = tagValueDao.insert(countryTagId, "US"); 
        countryTagValueIds[1] = tagValueDao.insert(countryTagId, "CA"); 
        countryTagValueIds[2] = tagValueDao.insert(countryTagId, "MX"); 
        long[] stateTagValueIds = new long[5];
        stateTagValueIds[0] = tagValueDao.insert(stateTagId, "CA"); 
        stateTagValueIds[1] = tagValueDao.insert(stateTagId, "AZ"); 
        stateTagValueIds[2] = tagValueDao.insert(stateTagId, "OR"); 
        stateTagValueIds[3] = tagValueDao.insert(stateTagId, "TX"); 
        stateTagValueIds[4] = tagValueDao.insert(stateTagId, "NY"); 
        long[] cityTagValueIds = new long[5];
        cityTagValueIds[0] = tagValueDao.insert(cityTagId, "Folsom"); 
        cityTagValueIds[1] = tagValueDao.insert(cityTagId, "Santa Clara"); 
        cityTagValueIds[2] = tagValueDao.insert(cityTagId, "Hillsboro"); 
        cityTagValueIds[3] = tagValueDao.insert(cityTagId, "Austin"); 
        cityTagValueIds[4] = tagValueDao.insert(cityTagId, "New York"); 
        long[] customerTagValueIds = new long[3];
        customerTagValueIds[0] = tagValueDao.insert(customerTagId, "Coke"); 
        customerTagValueIds[1] = tagValueDao.insert(customerTagId, "Pepsi"); 
        customerTagValueIds[2] = tagValueDao.insert(customerTagId, "US Govt"); 
        long[] buildingTagValueIds = new long[3];
        buildingTagValueIds[0] = tagValueDao.insert(buildingTagId, "Bldg 100"); 
        buildingTagValueIds[1] = tagValueDao.insert(buildingTagId, "Bldg 200"); 
        buildingTagValueIds[2] = tagValueDao.insert(buildingTagId, "Bldg 300"); 
        // default tag selection
        log.debug("insert selections");
        UUID defaultSelectionUuid = new UUID(); // will be reused in the configuration below
        long defaultSelectionId = selectionDao.insert(defaultSelectionUuid, "default");
        long otherSelectionId = selectionDao.insert(new UUID(), "other");
        selectionTagValueDao.insert(defaultSelectionId, countryTagId, countryTagValueIds[0]);
        selectionTagValueDao.insert(defaultSelectionId, stateTagId, stateTagValueIds[0]);
        selectionTagValueDao.insert(defaultSelectionId, cityTagId, cityTagValueIds[0]);
        selectionTagValueDao.insert(defaultSelectionId, cityTagId, cityTagValueIds[1]);
        selectionTagValueDao.insert(defaultSelectionId, customerTagId, customerTagValueIds[0]);
        selectionTagValueDao.insert(defaultSelectionId, customerTagId, customerTagValueIds[1]);
        selectionTagValueDao.insert(otherSelectionId, countryTagId, countryTagValueIds[1]);
        selectionTagValueDao.insert(otherSelectionId, countryTagId, countryTagValueIds[2]);
        selectionTagValueDao.insert(otherSelectionId, stateTagId, stateTagValueIds[1]);
        selectionTagValueDao.insert(otherSelectionId, stateTagId, stateTagValueIds[2]);
        selectionTagValueDao.insert(otherSelectionId, stateTagId, stateTagValueIds[3]);
        selectionTagValueDao.insert(otherSelectionId, cityTagId, cityTagValueIds[2]);
        selectionTagValueDao.insert(otherSelectionId, cityTagId, cityTagValueIds[3]);
        selectionTagValueDao.insert(otherSelectionId, buildingTagId, buildingTagValueIds[0]);        
        selectionTagValueDao.insert(otherSelectionId, buildingTagId, buildingTagValueIds[1]);
        // XXX in production should probably  use the uuids, not the names ... just in case there are duplicate names 
        rdfTripleDao.insert(new UUID(), "country", "contains", "state");
        rdfTripleDao.insert(new UUID(), "state", "contains", "city");
        
        
    // mtwilsonUrl, mtwilsonClientKeystoreUsername, mtwilsonClientKeystorePassword (should generate automatically)
//        String mtwilsonClientKeystoreUsername = "asset-tag-prov-svc";
//        String mtwilsonClientKeystorePassword = RandomStringUtils.randomAlphanumeric(16);
        
        // configuration to allow automatic tag selection & approval
        log.debug("inserting configuration");
        Properties p = new Properties();
        p.setProperty("allowTagsInCertificateRequests", "true");
        p.setProperty("allowAutomaticTagSelection", "true");
        p.setProperty("automaticTagSelectionName", defaultSelectionUuid.toString());
        p.setProperty("approveAllCertificateRequests", "true");
        // removed the following into mtwilson.properties: ,\"mtwilsonUrl\":\"https://mtwilson.com\",\"mtwilsonClientKeystoreUsername\":\""+mtwilsonClientKeystoreUsername+"\",\"mtwilsonClientKeystorePassword\":\""+mtwilsonClientKeystorePassword+"\"
        Configuration configuration = new Configuration("main", p);
        configurationDao.insert(new UUID(), configuration.getName(), configuration.getXmlContent());
        
    }
    

    public static void main(String args[]) throws Exception {
        InitDatabase cmd = new InitDatabase();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }    
}
