/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp.apache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.DelegatingConnection;

/**
 *
 * @author jbuhacoff
 */
public class LoggingConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingConnection.class);
    private final long id;
    
    public LoggingConnection(Connection connection) {
        super(connection);
        id = Math.round(Math.random()*Long.MAX_VALUE);
        log.debug("[{}] constructor", id);
    }

    public LoggingConnection(Connection connection, AbandonedConfig config) {
        super(connection, config);
        id = Math.round(Math.random()*Long.MAX_VALUE);
        log.debug("[{}] constructor with abanoned configuration", id);
    }

    @Override
    public void close() throws SQLException {
        log.debug("[{}] close", id);
        super.close();
    }

    @Override
    public void commit() throws SQLException {
        log.debug("[{}] commit", id);
        super.commit();
    }

    @Override
    public Statement createStatement() throws SQLException {
        log.debug("[{}] createStatement", id);
        return super.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        log.debug("[{}] prepareStatement {}", id, sql);
        return super.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        log.debug("[{}] prepareStatement {} with columns {}", id, sql, columnNames);
        return super.prepareStatement(sql, columnNames);
    }

    
    @Override
    public String nativeSQL(String sql) throws SQLException {
        log.debug("[{}] nativeSQL {}", id, sql);
        return super.nativeSQL(sql);
    }

    @Override
    protected void passivate() throws SQLException {
        log.debug("[{}] passivate", id);
        super.passivate();
    }

    @Override
    protected void activate() {
        log.debug("[{}] activate", id);
        super.activate();
    }

    
}
