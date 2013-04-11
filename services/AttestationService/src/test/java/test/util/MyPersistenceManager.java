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
import java.util.prefs.Preferences;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;

/**
 * This persistence manager gets the jdbc information from your Java Preferences - 
 * so it is automatically customized for every developer, and you can use it from
 * Junit tests and it will "Just work" from anyone's machine (as long as they have
 * the configuration) , which is nice for avoiding having to change junit tests
 * just to run them in your environment, which then breaks them for other people.
 * 
 * HOW TO GET STARTED:
 * 
 * 1. Using JUnit, run the method testCheckMyPreferences() as a JUnit test. It will
 * show you your current preferences (using defaults for any preferences you haven't set).
 * 
 * 2. Then, edit the contents of the method testSetMyPreferences() to be what you want
 * to use for JUnit tests.  I usually just change the IP address to the VM I'm currently
 * using for development/testing.  Run testSetMyPreferences() as a JUnit test. 
 * 
 * 3. Using JUnit, run the method testCheckMyPreferences() as a JUnit test (again). It will
 * show you your current preferences (using defaults for any preferences you haven't set).
 * You should see the changes you made in step 2. 
 * 
 * 4. In other JUnit tests where you need to access the database, create a MyPersistenceManager
 * and use it to create the JPA Controllers. For example:
 * 
 * Example:
 * 
        Properties jdbc = new Properties();
        MyPersistenceManager pm = new MyPersistenceManager();

 *
 * @author jbuhacoff
 */
public class MyPersistenceManager extends PersistenceManager {
    private Preferences prefs = Preferences.userRoot().node(getClass().getName());
    private Properties jdbcProperties = new Properties();
    public MyPersistenceManager() {
        jdbcProperties.setProperty("mtwilson.db.host", prefs.get("mtwilson.db.host", "127.0.0.1"));
        jdbcProperties.setProperty("mtwilson.db.schema", prefs.get("mtwilson.db.schema", "mw_as"));
        jdbcProperties.setProperty("mtwilson.db.user", prefs.get("mtwilson.db.user", "root"));
        jdbcProperties.setProperty("mtwilson.db.password", prefs.get("mtwilson.db.password", "password"));
        jdbcProperties.setProperty("mtwilson.db.port", prefs.get("mtwilson.db.port", "3306"));
        jdbcProperties.setProperty("mtwilson.as.dek", prefs.get("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA=="));
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
    
    
    /**
     * Use this method to set your personal preferences... just customize the settings and run it as a junit test.
     * After you run it to set YOUR LOCAL JAVA PREFERENCES, please restore the settings IN THIS FILE to what they were,
     * you can simply "undo" all the changes until it's back to original form.
     */
    @Test
    public void testSetMyPreferences() {
        prefs.put("mtwilson.db.host", "127.0.0.1");
        prefs.put("mtwilson.db.schema", "mw_as");
        prefs.put("mtwilson.db.user", "root");
        prefs.put("mtwilson.db.password", "password");
        prefs.put("mtwilson.db.port", "3306");        
        prefs.put("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA==");        
    }
    
    /**
     * Use this method to check what is stored in your personal preferences... just run it as a junit test to see the console output
     * 
     * Sample output:
mtwilson.db.host: 127.0.0.1
mtwilson.db.schema: mw_as
mtwilson.db.user: root
mtwilson.db.password: password
mtwilson.db.port: 3306
     * 
     * 
     */
    @Test
    public void testCheckMyPreferences() {       
        System.out.println(String.format("%s: %s", "mtwilson.db.host", prefs.get("mtwilson.db.host", "127.0.0.1")));
        System.out.println(String.format("%s: %s", "mtwilson.db.schema", prefs.get("mtwilson.db.schema", "mw_as")));
        System.out.println(String.format("%s: %s", "mtwilson.db.user", prefs.get("mtwilson.db.user", "root")));
        System.out.println(String.format("%s: %s", "mtwilson.db.password", prefs.get("mtwilson.db.password", "password")));
        System.out.println(String.format("%s: %s", "mtwilson.db.port", prefs.get("mtwilson.db.port", "3306")));        
        System.out.println(String.format("%s: %s", "mtwilson.as.dek", prefs.get("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA==")));        
    }
}
