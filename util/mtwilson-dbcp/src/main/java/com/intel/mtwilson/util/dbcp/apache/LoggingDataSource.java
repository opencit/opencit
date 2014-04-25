/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp.apache;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.PoolingDataSource;

/**
 *
 * @author jbuhacoff
 */
public class LoggingDataSource extends BasicDataSource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingDataSource.class);

    public LoggingDataSource() {
        super();
        log.debug("constructor");
    }

    @Override
    protected void createDataSourceInstance() throws SQLException {
        PoolingDataSource pds = new CustomPoolingDataSource(connectionPool);
        log.debug("access to underlying connection allowed? {}", isAccessToUnderlyingConnectionAllowed());
        pds.setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        pds.setLogWriter(logWriter);
        dataSource = pds;
    }


    @Override
    public Connection getConnection() throws SQLException {
        log.debug("getConnection");
        return super.getConnection();
    }

    @Override
    public synchronized void close() throws SQLException {
        log.debug("close");
        super.close();
    }
    
    
}
