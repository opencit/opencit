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

    private String databaseHost;
    private String databasePort;
    private String databaseDriver;
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

    // XXX TODO INSECURE  need to protect this with apache shiro so it will require administrator permission to view
    public String getDatabaseUsername() {
        return databaseUsername;
    }

    // XXX TODO INSECURE  need to protect this with apache shiro so it will require administrator permission to view
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
            log.debug("Database URL: {}", databaseUrl); // XXX TODO INSECURE do not print this in the log after things are worknig
        }
    }

    @Override
    protected void validate() throws Exception {
        try {
            Connection c = My.jdbc().connection();
            Statement s = c.createStatement();
            s.executeQuery("SELECT 1"); // XXX TODO  this doesn't work on all databases;  need to have dialect-specific query to check connection
            s.close();
            c.close();
        }
        catch(Exception e) {
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
