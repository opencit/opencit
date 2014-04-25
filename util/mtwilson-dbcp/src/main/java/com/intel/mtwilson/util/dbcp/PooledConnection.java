/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp;

import com.intel.mtwilson.util.dbcp.apache.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp.DelegatingConnection;

/**
 * TODO:  instead of extending apache dbcp DelegatingConnection, it should
 * be re-implemented and the connection should be revoked if any exceptions
 * are thrown unless we can clear all statements and other associated objects
 * and continue to use the same connection
 * 
 * @author jbuhacoff
 */
public class PooledConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PooledConnection.class);
    private ObjectPool pool;
    private final long id;
    
    public PooledConnection(Connection connection, ObjectPool objectPool) {
        super(connection);
        id = Math.round(Math.random()*Long.MAX_VALUE);
        pool = objectPool;
        log.debug("[{}] constructor wrapping {}", id, connection);
    }
    
    @Override
    public void close() throws SQLException {
        log.debug("[{}] close", id);
        pool.returnObject(this); // must come before super.close() or else the pool will not recognize us and say we weren't borrowed...
//        super.close(); // closes the underlying connection and sets hashcode to zero which would cause us not be recognized by the pool anymore
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
        log.debug("[{}] prepareStatement {}",id,  sql);
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
    public String toString() {
        return String.format("PooledConnection[%s] wrapping %s", id, _conn);
    }

    
    
}
