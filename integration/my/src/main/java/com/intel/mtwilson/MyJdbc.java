/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class MyJdbc {
    private MyConfiguration config;
    public MyJdbc(MyConfiguration config) {
        this.config = config;
    }
    
    public String url() {
        // if we just use the defaults then we lose an opportunity for clever auto-fill of properties (port/protocol/driver correlations)
        // that doesn't really belong in the configuration class
        // so first we try to fill in missing values:
        Properties p = config.getProperties("mtwilson.db.protocol", "mtwilson.db.driver", "mtwilson.db.port");
        String protocol = p.getProperty("mtwilson.db.protocol", "");
        String driver = p.getProperty("mtwilson.db.driver", "");
        String port = p.getProperty("mtwilson.db.port", "");
        if( protocol.isEmpty() && !driver.isEmpty() ) {
            if( driver.contains("postgresql") ) {
                protocol = "postgresql";
            }
            if( driver.contains("mysql") ) {
                protocol = "mysql";
            }
        }
        if( protocol.isEmpty() && !port.isEmpty() ) {
            if( port.equals("5432") ) {
                protocol = "postgresql";
            }
            if( port.equals("3306") ) {
                protocol = "mysql";
            }
        }
        if( port.isEmpty() && !protocol.isEmpty() ) {
            if( protocol.equals("postgresql") ) {
                port = "5432";
            }
            if( protocol.equals("mysql") ) {
                port = "3306";
            }
        }
        // now if we are still missing information, use the defaults:
        if( protocol.isEmpty() ) {
            protocol = config.getDatabaseProtocol(); 
        }
        if( port.isEmpty() ) {
            port = config.getDatabasePort(); 
        }
        return String.format("jdbc:%s://%s:%s/%s", protocol, config.getDatabaseHost(), port, config.getDatabaseSchema());
    }
}
