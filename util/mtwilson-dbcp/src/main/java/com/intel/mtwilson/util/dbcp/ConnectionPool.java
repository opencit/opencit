/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author jbuhacoff
 */
class ConnectionPool extends AbstractObjectPool<Connection> {
    private DataSource ds;

    public ConnectionPool(DataSource ds) {
        this.ds = ds;
    }

    @Override
    protected Connection createObject() {
        try {
            return new PooledConnection(ds.getConnection(), this);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create connection", e);
        }
    }
    
}
