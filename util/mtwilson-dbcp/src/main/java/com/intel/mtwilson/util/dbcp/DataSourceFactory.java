/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp;

import com.intel.dcsg.cpg.util.jdbc.ConnectionPool;
import com.intel.dcsg.cpg.util.jdbc.PoolingDataSource;
//import org.apache.commons.dbcp.BasicDataSource;
import com.intel.dcsg.cpg.util.jdbc.ValidatingConnectionPool;
import javax.sql.DataSource;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jbuhacoff
 */
public class DataSourceFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataSourceFactory.class);
//    private static DataSource ds = null;

    /**
     * @param ds
     * @return
     */
    public static DataSource createObjectPool(DataSource ds) {
        ConnectionPool connectionPool = new ConnectionPool();
        connectionPool.setDataSource(ds);
        return new PoolingDataSource(connectionPool);
    }

    /**
     * @param ds
     * @param configuration
     * @return
     */
    public static DataSource createConfigurableObjectPool(DataSource ds, Configuration configuration) {
        String validationQuery = configuration.getString("dbcp.validation.query"); // dbcp.validation.query default null
        boolean validateOnBorrow = configuration.getBoolean("dbcp.validation.on.borrow", true); // dbcp.validation.on.borrow default true
        boolean validateOnReturn = configuration.getBoolean("dbcp.validation.on.return", false); // dbcp.validation.on.return default false
        if (validationQuery == null || validationQuery.isEmpty()) {
            log.debug("validate on borrow and validate on return are forced off because validation query is not defined");
            validateOnBorrow = false;
            validateOnReturn = false;
        }
        ValidatingConnectionPool connectionPool = new ValidatingConnectionPool();
        connectionPool.setDataSource(ds);
        log.debug("validating on borrow={}, on return={}, with query={}", validateOnBorrow, validateOnReturn, validationQuery);
        connectionPool.setValidateOnBorrow(validateOnBorrow);
        connectionPool.setValidateOnReturn(validateOnReturn);
        connectionPool.setValidationQuery(validationQuery);
        return new PoolingDataSource(connectionPool);
    }

    /*
     public static DataSource getInstance() {        
     try {
     if (ds == null) {
     String driver = My.jdbc().driver();
     String dbUrl = My.jdbc().url();      
     BasicDataSource dataSource = new BasicDataSource();
     dataSource.setDriverClassName(driver); // or com.mysql.jdbc.Driver  for mysql
     dataSource.setUrl(dbUrl);
     dataSource.setUsername(My.configuration().getDatabaseUsername());
     dataSource.setPassword(My.configuration().getDatabasePassword());
                
     ds = createConfigurableObjectPool(dataSource, My.configuration().getConfiguration());
     }
     } catch (Exception ex) {
     log.error("Error connecting to the database. {}", ex.getMessage());
     }
     return ds;
     }
     */
}
