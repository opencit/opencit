/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Adds convenience methods for working with databases
 * 
 * @author jbuhacoff
 */
public abstract class DatabaseSetupTask extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseSetupTask.class);
    

    protected boolean isTableCreated(Connection connection, String tableName) throws SQLException {
        boolean created = false;
        try(ResultSet rs = connection.getMetaData().getTables(null, null, tableName, new String[] { "TABLE" })) {
        if(rs.next()) {
            created = true;
            /*
            int columns = rs.getMetaData().getColumnCount();
            for(int i=1; i<=columns; i++) {
                log.debug("Column: {}", rs.getMetaData().getColumnName(i));
            }
            */
        }
        }
        return created;
    }
    
    protected void requireTable(Connection connection, String tableName) throws SQLException {
            if (!isTableCreated(connection, tableName)) {
                configuration("Table %s must be created in database", tableName);
            }
        
    }
}
