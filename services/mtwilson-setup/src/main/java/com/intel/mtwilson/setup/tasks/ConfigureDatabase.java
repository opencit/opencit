/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author jbuhacoff
 */
public class ConfigureDatabase extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureDatabase.class);

    private String databaseHost = "127.0.0.1";
    private String databasePort = "5432";
    private String databaseDriver = "postgresql";
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;

    public String getDatabaseHost() {
        return databaseHost;
    }

    public String getDatabasePort() {
        return databasePort;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }
    
    

    @Override
    protected void configure() throws Exception {
        databaseDriver = My.jdbc().driver();
        if( databaseDriver == null ) {
            configuration("Database driver not configured");
        }
        else {
            log.debug("Database driver: {}", databaseDriver);
        }
        databaseUrl = My.jdbc().url();
        if( databaseUrl == null ) {
            configuration("Database URL not configured");
        }
        else {
            log.debug("Database URL: {}", databaseUrl);
        }
        getConfiguration().set("mtwilson.db.host", databaseHost);
        getConfiguration().set("mtwilson.db.port", databasePort);
        getConfiguration().set("mtwilson.db.driver", databaseDriver);
        getConfiguration().set("mtwilson.db.username", databaseUsername);
        getConfiguration().set("mtwilson.db.password", databasePassword);
//        getConfiguration().setString("mtwilson.db.url", databaseUrl);
    }

    @Override
    protected void validate() throws Exception {
        try (Connection c = My.jdbc().connection()) {
            try (Statement s = c.createStatement()) {
                s.executeQuery("SELECT 1"); 
            } catch (Exception ex) {
                log.error("Error creating select statement",ex);
                validation("Error creating select statement");
            }
        } catch(Exception e) {
            log.error("Cannot connect to database", e);
            validation("Cannot connect to database");
        }
    }

    @Override
    protected void execute() throws Exception {
        // we cannot install a database from here... nothing to do.
        // but a separate derby configuration task maybe would create a local derby db??
    }
    
}
