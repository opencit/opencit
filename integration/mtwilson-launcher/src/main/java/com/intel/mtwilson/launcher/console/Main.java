/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.console;

import com.intel.mtwilson.extensions.cache.ExtensionCacheLoader;
import com.intel.dcsg.cpg.configuration.LayeredConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.configuration.ReadonlyConfiguration;
import com.intel.mtwilson.Folders;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 *
 * @author jbuhacoff
 */
public class Main {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Main.class);
    public static final String APPLICATION_PROPERTIES = "/com/intel/mtwilson/application.properties";
    /**
     * @param args comprised of command name followed by arguments for that
     * command
     */
    public static void main(String[] args) {
        // turn off jdk logging because sshj logs to console
        LogManager.getLogManager().reset();
        log.debug("main called with args: {}", (Object[])args);
        
        // the application properties are an interface to communicate basic
        // settings to key components used during startup such as filesystem,
        // configuration, and extensions
        Configuration applicationProperties = loadApplicationProperties();
        copyToSystemProperties(applicationProperties);
        
        // TODO:  the ExtensionCacheLoader is in the mtwilson-extensions-cache
        //        project (good) and next step is to make an extension point
        //        right here for @Init / @Startup and add that annotation to
        //        ExtensionCacheLoader so the launcher doesn't have a direct
        //        dependency to it.  Might need to make that an interface instead
        //        of an annotation so it can be declared via Java's Service Loader
        //        (since extension cache wouldn't be loaded at that point!)
        // the extension manager loads the available extensions from the classpath (which must be set by the command line)
        ExtensionCacheLoader loader = new ExtensionCacheLoader(Folders.configuration()); // reads the files extensions.cache and extensions.prefs 
        loader.run();
        
        // the dispatcher finds the command specified in arg[0] and runs it
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setArgs(args);
        dispatcher.run();
        System.exit(dispatcher.getExitCode());
    }
    
    /**
     * Copies settings from the given configuration to system properties.
     * If any keys already exist, they are not replaced.
     * @param conf to copy to system properties
     */
    private static void copyToSystemProperties(Configuration conf) {
        for(String key : conf.keys()) {
            String existingValue = System.getProperty(key);
            if( existingValue == null ) {
                String newValue = conf.get(key);
                System.setProperty(key, newValue);
                log.debug("Added property {} = {}", key, newValue);
            }
            else {
                log.debug("Existing property {} = {}", key, existingValue);
            }
        }
    }
    
    /**
     * The application.properties file is not required; all properties
     * have Mt Wilson defaults defined in this method. The application.properties
     * is only required to be on the classpath when reusing the Mt Wilson
     * core libraries to create another application such as Trust Agent or KMS.
     */
    private static Configuration loadApplicationProperties() {
        Configuration defaults = getApplicationDefaultProperties();
        InputStream in = Main.class.getResourceAsStream(APPLICATION_PROPERTIES);
        if( in == null ) {
            return new ReadonlyConfiguration(defaults);
        }
        try {
            Properties properties = new Properties();
            properties.load(in);
            return new ReadonlyConfiguration(new LayeredConfiguration(new PropertiesConfiguration(properties), defaults));
        }
        catch(Exception e) {
            log.error("Cannot load application.properties", e);
            return defaults;
        }
    }
    
    private static Configuration getApplicationDefaultProperties() {
        PropertiesConfiguration defaults = new PropertiesConfiguration();
        defaults.set("mtwilson.application.id", "mtwilson");
        defaults.set("mtwilson.application.name", "Mt Wilson");
        defaults.set("mtwilson.configuation.file", "mtwilson.properties");
        defaults.set("mtwilson.environment.prefix", "MTWILSON_");
        return defaults;
    }
    
}
