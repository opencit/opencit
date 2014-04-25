/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp.apache;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;

/**
 *
 * @author jbuhacoff
 */
public class CustomPoolingDataSource extends PoolingDataSource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomPoolingDataSource.class);
    
    public CustomPoolingDataSource() {
        super();
    }

    public CustomPoolingDataSource(ObjectPool pool) {
        super(new LoggingObjectPool(pool));// we just log what happens with the apache dbcp,  but this is NOT the pool instance we use;  we use our pool first for reference and only get an object from the apache pool if we need a new connection to add...
    }

    /**
     * PoolGuardConnectionWrapper wraps native connection (postgresl driver etc)
     * LoggingPooledConnection wraps PoolGuardConnectionWrapper
     * 
     * @return
     * @throws SQLException 
     */
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        if( connection != null ) {
            log.debug("wrapping connection");
//            connection = new LoggingConnection(connection);
            connection = new LoggingPooledConnection(connection, _pool);
        }
        return connection;
    }
    
}
