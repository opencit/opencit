/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.console;

import com.intel.dcsg.cpg.configuration.LayeredConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.configuration.ReadonlyConfiguration;
import com.intel.mtwilson.extensions.cache.ExtensionCacheLoader;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 *
 * @author jbuhacoff
 */
public class Main {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Main.class);

    /**
     * @param args comprised of command name followed by arguments for that
     * command
     */
    public static void main(String[] args) {
        // turn off jdk logging because sshj logs to console
        LogManager.getLogManager().reset();
        log.debug("main called with args: {}", (Object[])args);
        Configuration app = loadApplicationProperties();
        // must set mtwilson.application.id before instantiating Filesystem
        System.setProperty("mtwilson.application.id", app.get("id"));
        log.debug("mtwilson.application.id = {}", app.get("id"));
        Filesystem fs = new Filesystem();
        // the extension manager loads the available extensions from the classpath (which must be set by the command line)
        ExtensionCacheLoader loader = new ExtensionCacheLoader(fs.getConfigurationPath()); // reads the files extensions.cache and extensions.prefs 
        loader.run();
        // the dispatcher finds the command specified in arg[0] and runs it
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setArgs(args);
        dispatcher.run();
        System.exit(dispatcher.getExitCode());
    }
    
    /**
     * The application.properties file is not required; all properties
     * have Mt Wilson defaults defined in this method. The application.properties
     * is only required to be on the classpath when reusing the Mt Wilson
     * core libraries to create another application such as Trust Agent or KMS.
     */
    private static Configuration loadApplicationProperties() {
        PropertiesConfiguration defaults = new PropertiesConfiguration();
        defaults.set("id", "mtwilson");
        defaults.set("name", "Mt Wilson");
        InputStream in = Main.class.getResourceAsStream("/com/intel/mtwilson/application.properties");
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
}
