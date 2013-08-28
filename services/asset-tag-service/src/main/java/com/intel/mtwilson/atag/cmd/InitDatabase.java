/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.Derby;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.model.Configuration;
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
 * This command creates a default "main" configuration and TODO: create the default asset tag authority
 * @author jbuhacoff
 */
public class InitDatabase extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(InitDatabase.class);
    
    @Override
    public void execute(String[] args) throws Exception {
        log.debug("Starting Derby...");
        Derby.startDatabase();
        log.debug("Derby started");
        insertMainConfiguration();
        log.debug("Stopping Derby...");
        Derby.stopDatabase();
        log.debug("Derby stopped");
    }
    
    public void insertMainConfiguration() throws SQLException {
        String configuration = "{\"allowTagsInCertificateRequests\":false,\"allowAutomaticTagSelection\":false,\"automaticTagSelectionName\":\"default\",\"approveAllCertificateRequests\":false}";
        Derby.configurationDao().insert(new UUID(), "main", Configuration.ContentType.JSON, configuration);
    }
    

    public static void main(String args[]) throws Exception {
        InitDatabase cmd = new InitDatabase();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);
        
    }    
}
