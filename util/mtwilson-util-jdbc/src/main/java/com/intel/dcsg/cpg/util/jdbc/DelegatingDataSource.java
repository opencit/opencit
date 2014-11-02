/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc;

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
public class DelegatingDataSource implements DataSource {
    protected DataSource ds;
    
    public DelegatingDataSource(DataSource ds) {
        this.ds = ds;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return ds.getConnection(username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if( iface.isAssignableFrom(DataSource.class)) {
            return (T)ds;
        }
        return ds.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if( iface.isAssignableFrom(DataSource.class)) {
            return true;
        }
        return ds.isWrapperFor(iface);
    }
    
}
