/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * This utility class aims to make it possible for developers to all use the same JUnit tests and point them at
 * different mt wilson environments without having to modify the JUnit tests. So modifications should only be necessary to change
 * the tests, and not merely to change the IP address of the server or the location of the api client keystore.
 * 
 * THIS CLASS LOADS YOUR PERSONAL CONFIGURATION AUTOMATICALLY FROM ~/.mtwilson/mtwilson.properties
 * 
 * If that file does not exist, it will be automatically created with default values.  
 * 
 * After that, you can change which Mt Wilson server you are testing against, etc.  by editing those properties.
 * All JUnit tests for the api client should use the "My" class (next to this one) in order to automatically pick up your
 * local settings.
 * 
 * NOTE: If you need to change the location of the file from ~/.mtwilson to somewhere else, you can update your 
 * Java Preferences using the testSetMyConfigDir() method in this class -- look at that method's Javadoc for details.
 *
 * Example Junit test:
 *
 * MyConfiguration config = new MyConfiguration(); KeystoreUtil.createUserInDirectory( config.getKeystoreDir(),
 * config.getKeystoreUsername(), config.getKeystorePassword(), config.getMtWilsonURL(), config.getMtWilsonRoleArray());  *
 *
 * 
 * NOTE:  the default directory to store all your settings is  ~/.mtwilson
 * In order to change it, you have to set your Java Preferences using testSetMyPreferences().
 *
 * @author jbuhacoff
 */
public class MyConfiguration {

    private Preferences prefs = Preferences.userRoot().node(getClass().getName());
    private Properties conf = new Properties();
    
    public MyConfiguration() throws IOException {
        File directory = getDirectory();
        if( !directory.exists() && !directory.mkdirs() ) {
            throw new IOException("Cannot create configuration directory: "+directory.getAbsolutePath());
        }
        File file = getConfigFile();
        if( !file.exists() ) {
            FileOutputStream out = new FileOutputStream(file);
            getDefaultProperties().store(out, "Default Mt Wilson Settings... Customize for your environment");
            out.close();        
        }
        FileInputStream in = new FileInputStream(file);
        conf.load(in);
        in.close();
    }
    
    private Properties getDefaultProperties() {
        Properties p = new Properties();
        // api client
        p.setProperty("mtwilson.api.username", "anonymous");
        p.setProperty("mtwilson.api.password", "password");
        p.setProperty("mtwilson.api.url", "https://127.0.0.1:8181");
        p.setProperty("mtwilson.api.roles", "Attestation,Whitelist,Security,Report,Audit");
        // database
        p.setProperty("mtwilson.db.host", "127.0.0.1");
        p.setProperty("mtwilson.db.schema", "mw_as");
        p.setProperty("mtwilson.db.user", "root");
        p.setProperty("mtwilson.db.password", "password");
        p.setProperty("mtwilson.db.port", "3306");        
        p.setProperty("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA==");                
        return p;
    }
    
    /**
     * This is the only method that uses the Java Preferences API to get its value. Everything
     * else uses the mtwilson.properties file located the directory returned by this method.
     * @return ~/.mtwilson  unless you have changed your preferences (see testSetMyConfigDir)
     */
    public final String getDirectoryPath() {
        return prefs.get("mtwilson.config.dir", System.getProperty("user.home") + File.separator + ".mtwilson");
    }
    public final File getDirectory() {
        return new File(getDirectoryPath());
    }

    public final File getConfigFile() {
        return new File(getDirectoryPath() + File.separator + "mtwilson.properties");
    }
    
    public Properties getProperties() {
        return conf;
    }

    public Properties getProperties(String... names) {
        Properties p = new Properties();
        for(String name : names) {
            p.setProperty(name, conf.getProperty(name));
        }
        return p;
    }
    ///////////////////////// api client //////////////////////////////////
    
    public File getKeystoreDir() {
        return getDirectory();
    }

    public File getKeystoreFile() {
        String username = getKeystoreUsername();
        return new File(getDirectoryPath() + File.separator + username + ".jks");
    }

    public String getKeystoreUsername() {
        return conf.getProperty("mtwilson.api.username", System.getProperty("user.name", "anonymous"));
    }

    public String getKeystorePassword() {
        return conf.getProperty("mtwilson.api.password", "password");
    }

    public URL getMtWilsonURL() throws MalformedURLException {
        return new URL(conf.getProperty("mtwilson.api.url", "https://127.0.0.1:8181"));
    }

    public String getMtWilsonRoleString() {
        return conf.getProperty("mtwilson.api.roles", "Attestation,Whitelist,Security,Report,Audit");
    }

    public String[] getMtWilsonRoleArray() {
        return getMtWilsonRoleString().split(",");
    }

    ///////////////////////// database //////////////////////////////////
    
    public String getDatabaseHost() {
        return conf.getProperty("mtwilson.db.host", "127.0.0.1");
    }

    public String getDatabasePort() {
        return conf.getProperty("mtwilson.db.port", "3306");
    }

    public String getDatabaseUsername() {
        return conf.getProperty("mtwilson.db.user", "root");
    }

    public String getDatabasePassword() {
        return conf.getProperty("mtwilson.db.password", "password");
    }

    public String getDatabaseSchema() {
        return conf.getProperty("mtwilson.db.schema", "mw_as");
    }

    public String getDataEncryptionKeyBase64() {
        return conf.getProperty("mtwilson.as.dek", "hPKk/2uvMFRAkpJNJgoBwA==");
    }
    
    /**
     * Use this method to set your personal preferences... just run it from a JUnit test and set the directory
     * where your preferences should be stored, or use null to restore the default.
     */
    public void setDirectoryPath(String path) {
        if( path == null ) {
            prefs.put("mtwilson.config.dir", System.getProperty("user.home") + File.separator + ".mtwilson"); // reset default to: System.getProperty("user.home") + File.separator + ".mtwilson"
        }
        else {
            prefs.put("mtwilson.config.dir", path);
        }
    }

}
