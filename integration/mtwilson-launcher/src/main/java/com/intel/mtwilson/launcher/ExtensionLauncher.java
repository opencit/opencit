/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.performance.ProgressIterator;
import com.intel.mtwilson.launcher.ext.Configure;
import com.intel.mtwilson.launcher.ext.Initialize;
import com.intel.mtwilson.launcher.ext.Stop;
import com.intel.mtwilson.launcher.ext.Start;
import com.intel.mtwilson.launcher.ext.Validate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionLauncher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionLauncher.class);
    private boolean running = false;
    
    public void initialize() {
        addShutdownHook();
        List<Initialize> items = Extensions.findAll(Initialize.class);
        log.debug("Found {} items to initialize", items.size());
        ProgressIterator<Initialize> it = new ProgressIterator(items);
        while(it.hasNext()) {
            Initialize item = it.next();
            log.debug("Initialize: {}", item.getClass().getName());
            item.initialize();
        }
    }
    
    public void configure() {
        List<Configure> items = Extensions.findAll(Configure.class);
        log.debug("Found {} items to configure", items.size());
         ProgressIterator<Configure> it = new ProgressIterator(items);
        while(it.hasNext()) {
            Configure item = it.next();
            log.debug("Configure: {}", item.getClass().getName());
            item.configure();
        }
   }
    
    public void validate() {
        List<Validate> items = Extensions.findAll(Validate.class);
        log.debug("Found {} items to validate", items.size());
        ProgressIterator<Validate> it = new ProgressIterator(items);
        while(it.hasNext()) {
            Validate item = it.next();
            log.debug("Validate: {}", item.getClass().getName());
            item.validate();
        }
    }
    
    public void start() {
        running = true;
        List<Start> items = Extensions.findAll(Start.class);
        log.debug("Found {} items to start", items.size());
        ProgressIterator<Start> it = new ProgressIterator(items);
        while(it.hasNext()) {
            Start item = it.next();
            log.debug("Start: {}", item.getClass().getName());
            item.start();
        }
    }
    
    public void stop() {
        running = false;
        List<Stop> items = Extensions.findAll(Stop.class);
        log.debug("Found {} items to stop", items.size());
        ProgressIterator<Stop> it = new ProgressIterator(items);
        while(it.hasNext()) {
            Stop item = it.next();
            log.debug("Stop: {}", item.getClass().getName());
            item.stop();
        }
    }
    
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("MtWilson Shutdown Hook") {
            @Override
            public void run() {
                try {
                    log.debug("Caught shutdown event");
                    if( running) {
                        ExtensionLauncher.this.stop();
                    }
                } catch (Exception e) {
                    log.error("Shutdown error", e);
                }
            }
        });
    }
    
}
