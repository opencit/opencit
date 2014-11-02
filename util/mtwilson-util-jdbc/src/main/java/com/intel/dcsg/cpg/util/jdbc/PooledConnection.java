/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc;

import com.intel.dcsg.cpg.objectpool.ObjectPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The only important methods are the constructor and close. 
 * All other methods are overridden for logging purposes only.
 * 
 * @author jbuhacoff
 */
public class PooledConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PooledConnection.class);
    protected ObjectPool<Connection> pool;
    protected final long id;
    
    public PooledConnection(Connection connection, ObjectPool<Connection> objectPool) {
        super(connection);
        id = Math.round(Math.random()*Long.MAX_VALUE);
        pool = objectPool;
        log.debug("[{}] constructor wrapping {}", id, connection);
    }
    
    @Override
    public void close() throws SQLException {
        log.debug("[{}] close", id);
        pool.returnObject(this);
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
    public String toString() {
        return String.format("PooledConnection[%s] wrapping %s", id, delegate);
    }

    
    
}
