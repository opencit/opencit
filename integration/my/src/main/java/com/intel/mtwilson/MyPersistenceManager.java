/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.ms.common.MSConfig;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example:
 * 
        Properties jdbc = new Properties();
        jdbc.setProperty("mtwilson.db.host", "10.1.71.88");
        jdbc.setProperty("mtwilson.db.schema", "mw_as");
        jdbc.setProperty("mtwilson.db.user", "root");
        jdbc.setProperty("mtwilson.db.password", "password");
        jdbc.setProperty("mtwilson.db.port", "3306");
        jdbc.setProperty("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA=="); // optional;  if you don't set this ,the value you see here is the default
        CustomPersistenceManager pm = new CustomPersistenceManager(jdbc);

 *
 * @author jbuhacoff
 */
public class MyPersistenceManager extends PersistenceManager {
    private transient static Logger log = LoggerFactory.getLogger(MyPersistenceManager.class);
    private Properties jdbcProperties;
    public MyPersistenceManager(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }
    @Override
    public void configure() {
        log.debug("MyPersistenceManager: Database Host: {}", jdbcProperties.getProperty("mtwilson.db.host"));
        MapConfiguration c = new MapConfiguration(jdbcProperties);
        addPersistenceUnit("ASDataPU", getASDataJpaProperties(c));
        addPersistenceUnit("MSDataPU", getMSDataJpaProperties(c));
        addPersistenceUnit("AuditDataPU", getAuditDataJpaProperties(c));
    }
    public byte[] getDek() {
        return Base64.decodeBase64(jdbcProperties.getProperty("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA==")); // arbitrary default dek, since it's a development server it's good to use same as what is configured there, but it doesn't matter as it only affects records we are writing, and hopefully after each test is complete there is zero net effect on the database
    }
    public EntityManagerFactory getASData() {
        return getEntityManagerFactory("ASDataPU");
    }
    public EntityManagerFactory getMSData() {
        return getEntityManagerFactory("MSDataPU");
    }
    public EntityManagerFactory getAuditData() {
        return getEntityManagerFactory("AuditDataPU");
    }
    
    public Properties getASDataJpaProperties(Configuration config) {
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.as.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        if( prop.get("javax.persistence.jdbc.driver").equals("com.mysql.jdbc.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if( prop.get("javax.persistence.jdbc.driver").equals("org.postgresql.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.as.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                    prop.get("javax.persistence.jdbc.scheme"),
                    config.getString("mountwilson.as.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.as.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.as.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.as.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.as.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
    }    
    
    public Properties getMSDataJpaProperties(Configuration config) {
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.ms.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        if( prop.get("javax.persistence.jdbc.driver").equals("com.mysql.jdbc.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if( prop.get("javax.persistence.jdbc.driver").equals("org.postgresql.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }        
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.ms.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                    prop.getProperty("javax.persistence.jdbc.scheme"),
                    config.getString("mountwilson.ms.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.ms.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.ms.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.ms.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.ms.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
        
    }
    
    
    public Properties getAuditDataJpaProperties(Configuration config) {
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.audit.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        if( prop.get("javax.persistence.jdbc.driver").equals("com.mysql.jdbc.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if( prop.get("javax.persistence.jdbc.driver").equals("org.postgresql.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }        
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.audit.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                    prop.getProperty("javax.persistence.jdbc.scheme"),
                    config.getString("mountwilson.audit.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.audit.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.audit.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.audit.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.audit.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
        
    }
 
    public Properties getMCDataJpaProperties(Configuration config) {
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", 
                config.getString("mountwilson.mc.db.driver", 
                config.getString("mtwilson.db.driver",
                "com.mysql.jdbc.Driver")));
        if( prop.get("javax.persistence.jdbc.driver").equals("com.mysql.jdbc.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "mysql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else if( prop.get("javax.persistence.jdbc.driver").equals("org.postgresql.Driver") ) {
            prop.put("javax.persistence.jdbc.scheme", "postgresql"); // NOTE: this is NOT a standard javax.persistence property, we are setting it for our own use
        }
        else {
            prop.put("javax.persistence.jdbc.scheme", "unknown-scheme");
        }        
        prop.put("javax.persistence.jdbc.url" , 
                config.getString("mountwilson.mc.db.url",
                config.getString("mtwilson.db.url",
                String.format("jdbc:%s://%s:%s/%s?autoReconnect=true",
                    prop.getProperty("javax.persistence.jdbc.scheme"),
                    config.getString("mountwilson.mc.db.host", config.getString("mtwilson.db.host","127.0.0.1")),
                    config.getString("mountwilson.mc.db.port", config.getString("mtwilson.db.port","3306")),
                    config.getString("mountwilson.mc.db.schema", config.getString("mtwilson.db.schema","mw_as"))))));
        prop.put("javax.persistence.jdbc.user",
                config.getString("mountwilson.mc.db.user",
                config.getString("mtwilson.db.user",
                "root")));
        prop.put("javax.persistence.jdbc.password", 
                config.getString("mountwilson.mc.db.password", 
                config.getString("mtwilson.db.password", 
                "password")));
        return prop;
        
    }    
    
}
