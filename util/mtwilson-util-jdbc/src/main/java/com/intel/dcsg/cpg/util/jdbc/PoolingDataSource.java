/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author jbuhacoff
 */
public class PoolingDataSource extends DelegatingDataSource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PoolingDataSource.class);
    private ConnectionPool connectionPool = null;
    
    /**
     * Use another data source to populate the pool as necessary but
     * manage connections using our own pool.
     * 
     * The provided pool is assumed to be creating connections from the
     * same provided datasource.
     * 
     * @param ds 
     */
    public PoolingDataSource(ConnectionPool connectionPool) {
        super(connectionPool.getDataSource());
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
        throw new UnsupportedOperationException();
    }

}
