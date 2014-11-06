/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jooq.util;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.jooq.DSLContext;

/**
 * Facilitates automatically closing database connections when using JOOQ
 * 
 * @author jbuhacoff
 */
public class JooqContainer implements Closeable {
    private DSLContext dslContext;
    private Connection connection;
    
    public JooqContainer(DSLContext dslContext, Connection connection) {
        this.dslContext = dslContext;
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public DSLContext getDslContext() {
        return dslContext;
    }
    
    /**
     * Closes the JOOQ database connection (or returns it to the pool if 
     * it came from a connection pool)
     * 
     * You should close any open statements from DSLContext before calling
     * close().  
     * 
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        try {
            connection.close();
        }
        catch(SQLException e) {
            throw new IOException(e);
        }
    }
    
}
