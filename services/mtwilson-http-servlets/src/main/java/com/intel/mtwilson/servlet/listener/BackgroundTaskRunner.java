/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.servlet.listener;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.performance.BackgroundThread;
import com.intel.dcsg.cpg.performance.RunnableCollection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.intel.mtwilson.launcher.ext.annotations.Background;

/**
 * A simple first implementation which runs all @Background Runnable tasks
 * one at a time with a set interval between each sequence. 
 * Eligible Runnables must also have a no-arg constructor.
 * 
 * Currently a single thread is started to execute all runnables ; 
 * in the future we might start one background thread per runnable -
 * need to evaluate performance of each approach.
 * The sequence approach has the drawback that if one task on the list
 * should be executing every 100ms, and another task on the list takes
 * 1min to run each time, it's going to be delaying the 100ms task by 
 * a lot.
 * 
 * @author jbuhacoff
 */
public class BackgroundTaskRunner implements ServletContextListener, Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BackgroundTaskRunner.class);

    private static BackgroundThread thread = null;
    private ArrayList<Runnable> tasks = new ArrayList<Runnable>();
    
    @Override
    public void contextInitialized(ServletContextEvent context) {
        log.debug("contextInitialized");
//        Util.scanJars(Util.findAllJars(),getRegistrars());
        List<Runnable> runnables = Extensions.findAll(Runnable.class);
        // now filter this for runnables annotated with @Background
        for(Runnable runnable : runnables) {
            if( runnable.getClass().isAnnotationPresent(Background.class) ) {
                log.debug("Found background task: {}", runnable.getClass().getName());
                tasks.add(runnable);
            }
        }
        log.debug("Found {} background tasks", tasks.size());
        if( thread == null ) {
            log.debug("Starting background thread");
            thread = new BackgroundThread(new RunnableCollection(tasks));
            thread.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent context) {
        log.debug("contextDestroyed");
        if( thread != null ) {
            log.debug("Stopping background thread");
            thread.stop();
        }
    }

    @Override
    public void run() {
        for(Runnable task : tasks) {
            try {
                task.run();
            }
            catch(Exception e) {
                log.error("Execution of task {} resulted in error: {}", task.getClass().getName(), e.getMessage());
            }
        }
    }
    
    /*
    protected Registrar[] getRegistrars() {
        ImplementationRegistrar runnables = new ImplementationRegistrar(); //  backgroudn tasks
        return new Registrar[] { runnables };
    }
    */
}

