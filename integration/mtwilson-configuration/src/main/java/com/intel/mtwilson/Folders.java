/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.io.Platform;
import java.io.File;

/**
 * The Folders must be determined BEFORE reading any configuration file
 * because the configuration file itself may be located by looking in
 * {@code Folders.configuration()} for {@code System.getProperty("mtwilson.configuration.file", "mtwilson")}
 * 
 * @author jbuhacoff
 */
public class Folders {
    private static final String DEFAULT_APPLICATION_ID = "mtwilson";
    
    private static String application;
    private static String configuration;
    private static String features;
    private static String log;
    private static String repository;

    public static String application() {
        if (application == null) {
            application = locateApplicationFolder();
        }
        return application;
    }

    public static String configuration() {
        if (configuration == null) {
            configuration = locateConfigurationFolder();
        }
        return configuration;
    }
    
    public static String log() {
        if (log == null) {
            log = locateLogFolder();
        }
        return log;
    }

    public static String features() {
        if (features == null) {
            features = locateFeaturesFolder();
        }
        return features;
    }
    
    public static String features(String featureId) {
        if (features == null) {
            features = locateFeaturesFolder();
        }
        return features + File.separator + featureId;
    }
    
    public static String repository() {
        if (repository == null) {
            repository = locateRepositoryFolder();
        }
        return repository;
    }
    
    public static String repository(String featureId) {
        if (repository == null) {
            repository = locateRepositoryFolder();
        }
        return repository + File.separator + featureId;
    }
    
    /**
     * Represents the application folder, for example /opt/mtwilson or
     * C:\mtwilson, which can be set using the MTWILSON_HOME environment
     * variable (or for other applications, KMS_HOME, TRUSTAGENT_HOME, etc)
     *
     * Reads the system property mtwilson.application.id (default value
     * "mtwilson") if the MTWILSON_HOME variable is not set in order to derive
     * /opt/mtwilson etc. as as default location.
     *
     * @return path to home directory, even if it does not exist
     */
    private static String locateApplicationFolder() {
        String applicationId = System.getProperty("mtwilson.application.id", DEFAULT_APPLICATION_ID);
        
        // check if there's a specific system property for our application home directory
        String path = System.getProperty(applicationId+".home");
        if( path != null ) {
            return path;
        }
        
        // get value of environment variable MTWILSON_HOME, or KMS_HOME, etc.
        path = Environment.get("HOME");
        if (path != null) {
            return path;
        }

        

        // if we're running as a non-root user whose
        // home directory includes the application id, for example /home/mtwilson
        // this check is not case sensitive
        path = System.getProperty("user.home");
        if (path != null && path.toLowerCase().contains(File.separator + applicationId.toLowerCase())) {
            return path;
        }

        // with no *_HOME environment variable and no matching home directory,
        // use default platform-specific locations
        if (Platform.isUnix()) {
            return "/opt/" + applicationId; // like /opt/mtwilson
        }
        if (Platform.isWindows()) {
            return "C:" + File.separator + applicationId; // like C:/mtwilson 
        }

        // any other platform, we don't have a default so choose a directory
        // under the user directory or current directory if no home directory is
        // defined
        path = System.getProperty("user.home", ".");
        if (path != null) {
            return path + File.separator + applicationId;
        }

        // finally, use current directory
        return ".";
    }

    private static String locateConfigurationFolder() {
        String applicationId = System.getProperty("mtwilson.application.id", DEFAULT_APPLICATION_ID);

        // check if there's a specific system property for our application log directory
        String path = System.getProperty(applicationId+".configuration");
        if( path != null ) {
            return path;
        }
        
        // new mtwilson 3.0 setting, environment variable like MTWILSON_CONFIGURATION
        path = Environment.get("CONFIGURATION");
        if (path != null) {
            return path;
        }

        // compatibility with mtwilson 2.0 
        // get value of environment variable MTWILSON_CONF, or KMS_CONF, etc.
        path = Environment.get("CONF");
        if (path != null) {
            return path;
        }

        // use a subfolder "configuration" under the application folder
        path = application() + File.separator + "configuration"; //initConfigurationFolder(applicationId, application/*, configurationFile*/);        
        return path;
    }

    private static String locateFeaturesFolder() {
        String applicationId = System.getProperty("mtwilson.application.id", DEFAULT_APPLICATION_ID);

        // check if there's a specific system property for our application log directory
        String path = System.getProperty(applicationId+".features");
        if( path != null ) {
            return path;
        }
        
        // new mtwilson 3.0 setting, environment variable like MTWILSON_FEATURES
        path = Environment.get("FEATURES");
        if (path != null) {
            return path;
        }
        
        // use a subfolder "features" under the application folder
        path = application() + File.separator + "features"; //initConfigurationFolder(applicationId, application/*, configurationFile*/);        
        return path;
    }
    
    private static String locateLogFolder() {
        String applicationId = System.getProperty("mtwilson.application.id", DEFAULT_APPLICATION_ID);

        // check if there's a specific system property for our application log directory
        String path = System.getProperty(applicationId+".logs");
        if( path != null ) {
            return path;
        }
        
        // get value of environment variable MTWILSON_LOGS, or KMS_LOGS, etc.
        path = Environment.get("LOGS");
        if (path != null) {
            return path;
        }

        

        // with no *_LOGS environment variable and no matching log directory,
        // use default platform-specific locations
        if (Platform.isUnix()) {
            return "/var/log/" + applicationId; // like /var/log/mtwilson
        }
        if (Platform.isWindows()) {
            return application() + File.separator + "logs"; // like C:/mtwilson/logs
        }

        // any other platform, we don't have a default so choose a directory
        // under the user directory or current directory if no log directory is
        // defined
        path = System.getProperty("user.home", ".");
        if (path != null) {
            return path + File.separator + applicationId;
        }

        // finally, use current directory
        return ".";
    }

    private static String locateRepositoryFolder() {
        String applicationId = System.getProperty("mtwilson.application.id", DEFAULT_APPLICATION_ID);

        // check if there's a specific system property for our application log directory
        String path = System.getProperty(applicationId+".repository");
        if( path != null ) {
            return path;
        }
        
        // new mtwilson 3.0 setting, environment variable like MTWILSON_REPOSITORY
        path = Environment.get("REPOSITORY");
        if (path != null) {
            return path;
        }

        // compatibility with mtwilson 2.0 
        // get value of environment variable MTWILSON_VAR, or KMS_VAR, etc.
        path = Environment.get("VAR");
        if (path != null) {
            return path;
        }

        // use a subfolder "repository" under the application folder
        path = application() + File.separator + "repository"; //initConfigurationFolder(applicationId, application/*, configurationFile*/);        
        return path;
    }
}
