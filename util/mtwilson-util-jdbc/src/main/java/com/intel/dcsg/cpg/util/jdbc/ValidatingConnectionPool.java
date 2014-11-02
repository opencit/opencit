/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.util.jdbc;

import com.intel.dcsg.cpg.objectpool.AbstractObjectPool;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 *
 * @author jbuhacoff
 */
public class ValidatingConnectionPool extends ConnectionPool {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidatingConnectionPool.class);
    private boolean validateOnBorrow = false;
    private boolean validateOnReturn = false;
    private String validationQuery = null;

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

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getValidationQuery() {
        return validationQuery;
    }
    
    @Override
    public Connection borrowObject() {
        Connection object = super.borrowObject();
        if( isValidateOnBorrow() ) {
            while( !isValid(object) ) {
                log.debug("Revoking invalid object on borrow and trying again");
                revokeObject(object);
                returnObject(object);
                object = super.borrowObject();
            }
        }
        return object;
    }

    @Override
    public void returnObject(Connection object) {
        if( isValidateOnReturn() ) {
            if( !isValid(object) ) {
                log.debug("Revoking invalid object on return");
                revokeObject(object);
            }
        }
        super.returnObject(object);
    }

    protected boolean isValid(Connection connection) {
        log.debug("Validating connection {}", connection);
        try {
            if( connection.isClosed() ) { return false; }
        }
        catch(SQLException e) {
            log.debug("Cannot check if connection is closed", e);
            return false;
        }
        if( validationQuery == null ) { throw new NullPointerException("Validation query is undefined"); }
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
