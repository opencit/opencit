/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc.retry;

import com.intel.dcsg.cpg.util.jdbc.DelegatingConnection;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

/**
 * A delegating connection that will automatically retry an action if it catches
 * an exception that indicates temporary link failure (idle too long, database
 * server restarted, etc)
 *
 * The retrying connection needs both an existing connection and a datasource to
 * provide replacement connections. If a retryable error is caught the existing
 * connection is closed and a new connection is obtained from the datasource.
 *
 * This functionality is completely separated from the connection pool and any
 * data source can be used to obtain new connections. It is recommended to wrap
 * connections received from the pool with this delegate so that the pool
 * functionality will not be affected.
 *
 * <pre>
 * [poolable] data source -- retrying connection -- application
 * </pre>
 *
 * TODO: use javassist to generate this proxy dynamically; only shortcoming is
 * the wrapped connection must have a no-arg constructor
 *
 * @author jbuhacoff
 */
public class RetryingConnection extends DelegatingConnection {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RetryingConnection.class);

    protected DataSource ds;

    public RetryingConnection(Connection connection, DataSource ds) {
        super(connection);
        this.ds = ds;
    }

    /*
    private void replaceConnection() throws SQLException {
        delegate.close(); // return to pool or close connection
        delegate = ds.getConnection(); // get a new connection to replace it
    }
    */

    /*
    @Override
    public Statement createStatement() throws SQLException {
        return 
        log.debug("create statemetn in retryingconnection");
        try {
            return delegate.createStatement();
        } catch (SQLException e) {
            log.debug("caught exception {}", e);
            if (isCommunicationFailure(e)) {
                log.debug("it's a comm failure, retrying");
                replaceConnection();
                return delegate.createStatement();
            } else {
                log.debug("other failure, rethrowing");
                throw e;
            }
        }
    }
*/

}
