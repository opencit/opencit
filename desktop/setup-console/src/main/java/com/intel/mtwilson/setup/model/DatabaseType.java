/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

/**
 * For more jdbc drivers see also: http://www.devx.com/tips/Tip/28818 
 * @author jbuhacoff
 */
public enum DatabaseType {
    MYSQL("MySQL", 3306, "com.mysql.jdbc.Driver"),
    POSTGRES("Postgres", 5432, "org.postgresql.Driver");

    private String displayName;
    private int defaultPort;
    private String defaultJdbcDriver;
    
    DatabaseType(String name, int tcpPort, String jdbcDriver) {
        displayName = name;
        defaultPort = tcpPort;
        defaultJdbcDriver = jdbcDriver;
    }
    
    public String displayName() { return displayName; }
    public int defaultPort() { return defaultPort; }
    public String defaultJdbcDriver() { return defaultJdbcDriver; }

    @Override
    public String toString() { return displayName; }
    
    public static DatabaseType fromDriver(String driverClassName) {
        DatabaseType[] types = DatabaseType.values();
        for(DatabaseType t : types) {
            if( t.defaultJdbcDriver().equals(driverClassName) ) {
                return t;
            }
        }
        return null;
    }

    public static DatabaseType fromPort(int portNumber) {
        DatabaseType[] types = DatabaseType.values();
        for(DatabaseType t : types) {
            if( t.defaultPort() == portNumber ) {
                return t;
            }
        }
        return null;
    }

}
