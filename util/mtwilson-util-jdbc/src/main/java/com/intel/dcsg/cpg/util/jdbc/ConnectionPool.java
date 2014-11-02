/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc;

import com.intel.dcsg.cpg.objectpool.AbstractObjectPool;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author jbuhacoff
 */
public class ConnectionPool extends AbstractObjectPool<Connection> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionPool.class);
    private DataSource ds;
    private int created = 0;
    private int trashed = 0;
    
    public void setDataSource(DataSource ds) { this.ds = ds; }
    public DataSource getDataSource() { return ds; }
    
    /**
     * Obtains a new connection for the pool by calling getConnection() on the
     * datasource passed to the constructor. 
     * @return 
     */
    @Override
    protected Connection createObject() {
        try {
            created++;
            log.debug("creating new object for pool, now created {} trashed {} total {}", created, trashed, (created-trashed));
            return new PooledConnection(ds.getConnection(), this);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create connection", e);
        }
    }

    /**
     * Closes connections when they are removed from the pool.
     * 
     * @param object 
     */
    @Override
    protected void trashObject(Connection object) {
        try {
            if( object.isClosed() ) { 
                return;
            }
            if( object instanceof PooledConnection ) {
                log.debug("unwrapping connection to close");
                ((PooledConnection)object).delegate.close();
                ((PooledConnection)object).pool = null;
            }
            else {
                log.debug("closing other connection");
                object.close();
            }
            trashed++;
            log.debug("trashed object from pool, now created {} trashed {} total {}",created,trashed, (created-trashed));
        }
        catch(SQLException e) {
            throw new IllegalStateException("Cannot close connection", e);
        }
    }

}
