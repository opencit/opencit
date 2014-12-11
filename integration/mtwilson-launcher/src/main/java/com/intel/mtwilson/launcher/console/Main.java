/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher.console;

import com.intel.dcsg.cpg.extensions.ExtensionCacheLoader;
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
        // the extension manager loads the available extensions from the classpath (which must be set by the command line)
        Filesystem fs = new Filesystem();
        ExtensionCacheLoader loader = new ExtensionCacheLoader(fs.getConfigurationPath()); // reads the files extensions.cache and extensions.prefs 
        loader.run();
        // the dispatcher finds the command specified in arg[0] and runs it
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setArgs(args);
        dispatcher.run();
        System.exit(dispatcher.getExitCode());
    }
}
