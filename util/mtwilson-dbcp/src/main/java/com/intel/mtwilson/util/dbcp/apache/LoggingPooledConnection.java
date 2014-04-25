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
import org.apache.commons.pool.ObjectPool;

/**
 *
 * @author jbuhacoff
 */
public class LoggingPooledConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingPooledConnection.class);
    private ObjectPool pool;
    
    public LoggingPooledConnection(Connection connection, ObjectPool pool) {
        super(connection);
        this.pool = pool;
        log.debug("constructor wrapping {}", connection);
    }
    
    @Override
    public void close() throws SQLException {
        log.debug("close");
        super.close();
        /*
        // apache dbcp connections become "passive" when close() is called but
        // they remain connected to the database; unfortunately they are also
        // not returned to the pool until they become idle too long (?) so if
        // the application has a max active limit of 1 connection, opens a connection,
        // "closes" it, then tries to open another one, it will hang
        try {
            pool.returnObject(_conn);
        }
        catch(Exception e) {
            log.debug("cannot return object to pool", e);
        }
        */
    }

    @Override
    public void commit() throws SQLException {
        log.debug("commit");
        super.commit();
    }

    @Override
    public Statement createStatement() throws SQLException {
        log.debug("createStatement");
        return super.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        log.debug("prepareStatement {}", sql);
        return super.prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        log.debug("prepareStatement {} with columns {}", sql, columnNames);
        return super.prepareStatement(sql, columnNames);
    }

    
    @Override
    public String nativeSQL(String sql) throws SQLException {
        log.debug("nativeSQL {}", sql);
        return super.nativeSQL(sql);
    }

    
}
