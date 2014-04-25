/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author jbuhacoff
 */
public class HybridPoolingDataSource implements DataSource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HybridPoolingDataSource.class);
    private ObjectPool<Connection> connectionPool = null;
    
    /**
     * Use another data source to populate the pool as necessary but
     * manage connections using our own pool.
     * @param ds 
     */
    public HybridPoolingDataSource(DataSource ds) {
        this.connectionPool = new ConnectionPool(ds);
    }
    /**
     * Use an existing connection pool to borrow connections from. It's assumed
     * that borrowed connections already know to return themselves to the pool
     * when the close() method is called on the connection.
     * @param connectionPool 
     */
    public HybridPoolingDataSource(ObjectPool<Connection> connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * First we try our own ConnectionPool and then if that pool needs a new
     * connection it gets the new connection from the datasource.
     * 
     * @return
     * @throws SQLException 
     */
    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.borrowObject();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
