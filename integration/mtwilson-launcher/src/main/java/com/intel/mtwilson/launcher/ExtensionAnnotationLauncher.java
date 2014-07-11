/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.performance.ProgressIterator;
import com.intel.mtwilson.launcher.ext.annotations.Configure;
import com.intel.mtwilson.launcher.ext.annotations.Initialize;
import com.intel.mtwilson.launcher.ext.annotations.Shutdown;
import com.intel.mtwilson.launcher.ext.annotations.Startup;
import com.intel.mtwilson.launcher.ext.annotations.Validate;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionAnnotationLauncher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionAnnotationLauncher.class);
    private boolean running = false;
    
    public void initialize() {
        addShutdownHook();
        List<Initialize> items = Extensions.findAll(Initialize.class);
        log.debug("Found {} items to initialize", items.size());
        runAnnotation(Initialize.class, items);
    }
    
    public void configure() {
        List<Configure> items = Extensions.findAll(Configure.class);
        log.debug("Found {} items to configure", items.size());
        runAnnotation(Configure.class, items);
    }
    
    public void validate() {
        List<Validate> items = Extensions.findAll(Validate.class);
        log.debug("Found {} items to validate", items.size());
        runAnnotation(Validate.class, items);
    }
    
    public void startup() {
        running = true;
        List<Startup> items = Extensions.findAll(Startup.class);
        log.debug("Found {} items to startup", items.size());
        runAnnotation(Startup.class, items);
    }
    
    public void shutdown() {
        running = false;
        List<Shutdown> items = Extensions.findAll(Shutdown.class);
        log.debug("Found {} items to shutdown", items.size());
        runAnnotation(Shutdown.class, items);
    }
    
    /**
     * Each Runnable in the list is run.  Exceptions are specifically NOT
     * caught by this function - if a task needs to stop either startup or
     * shutdown or whatever phase its on, it can do so by throwing a 
     * RuntimeException.
     * 
     * @param items 
     */
    private void runAnnotation(Class<? extends Annotation> lifecyclePhaseAnnotation, List<? extends Object> items) {
        ProgressIterator<Object> it = new ProgressIterator(items);
        while(it.hasNext()) {
            Object item = it.next();
            Method method = ReflectionUtil.getLauncherMethod(item.getClass(),lifecyclePhaseAnnotation);
            if( method != null ) {
                String verb = lifecyclePhaseAnnotation.getSimpleName().toLowerCase();
                try {
                    log.debug("Calling {} on {}", verb, item.getClass().getName());
                    method.invoke(item); // throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
                }
                catch(Exception e) {
                    throw new RuntimeException("Cannot "+verb+" "+item.getClass().getName(), e);
                }
            }
            else if( item instanceof Runnable ) {
                log.debug("Running {}", item.getClass().getName());
                Runnable task = (Runnable)item;
                task.run();
            }
            else {
                log.debug("Not runnable: {}", item.getClass().getName());
            }
        }
    }
    
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("MtWilson Shutdown Hook") {
            @Override
            public void run() {
                try {
                    log.debug("Caught shutdown event");
                    if( running) {
                        shutdown();
                    }
                } catch (Exception e) {
                    log.error("Shutdown error", e);
                }
            }
        });
    }
    
}
