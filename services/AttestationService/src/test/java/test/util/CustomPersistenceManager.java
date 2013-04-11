/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.util;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.audit.helper.AuditConfig;
import com.intel.mtwilson.jpa.PersistenceManager;
import com.intel.mtwilson.ms.common.MSConfig;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.MapConfiguration;

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
public class CustomPersistenceManager extends PersistenceManager {
    private Properties jdbcProperties;
    public CustomPersistenceManager(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }
    @Override
    public void configure() {
        MapConfiguration c = new MapConfiguration(jdbcProperties);
        addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties(c));
        addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties(c));
        addPersistenceUnit("AuditDataPU", AuditConfig.getJpaProperties(c));
    }
    public byte[] getDek() {
        return Base64.decodeBase64(jdbcProperties.getProperty("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA==")); // arbitrary default dek, since it's a development server it's good to use same as what is configured there, but it doesn't matter as it only affects records we are writing, and hopefully after each test is complete there is zero net effect on the database
    }
}
