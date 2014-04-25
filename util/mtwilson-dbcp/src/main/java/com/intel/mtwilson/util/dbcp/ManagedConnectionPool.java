/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 *
 * @author jbuhacoff
 */
public class ManagedConnectionPool extends ConnectionPool {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagedConnectionPool.class);
    private boolean validateOnBorrow = true;
    private boolean validateOnReturn = false;
    private String validationQuery = null;
    
    public ManagedConnectionPool(DataSource ds) {
        super(ds);
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public boolean isValidateOnBorrow() {
        return validateOnBorrow;
    }

    public void setValidateOnBorrow(boolean validateOnBorrow) {
        this.validateOnBorrow = validateOnBorrow;
    }

    public boolean isValidateOnReturn() {
        return validateOnReturn;
    }

    
    public void setValidateOnReturn(boolean validateOnReturn) {
        this.validateOnReturn = validateOnReturn;
    }
    
    

    @Override
    public Connection borrowObject() {
        Connection connection = super.borrowObject();
        if( isValidateOnBorrow() ) {
            while( !isValid(connection) ) {
                log.debug("Revoking invalid connection on borrow and trying again");
                revokeObject(connection);
                returnObject(connection);
                connection = super.borrowObject();
            }
        }
        return connection;
    }

    @Override
    public void returnObject(Connection object) {
        if( isValidateOnReturn() ) {
            if( !isValid(object) ) {
                log.debug("Revoking invalid connection on return");
                revokeObject(object);
            }
        }
        super.returnObject(object);
    }
    
    protected boolean isValid(Connection connection) {
        if( validationQuery == null ) { return true; }
        try(Statement s = connection.createStatement()) {
            try(ResultSet rs = s.executeQuery(validationQuery)) {
                while(rs.next()) {
                    log.debug("Validation result");
                }
                return true;
            }
        }
        catch(SQLException e) {
            log.debug("Validation failed", e);
            return false;
        }
    }
}
