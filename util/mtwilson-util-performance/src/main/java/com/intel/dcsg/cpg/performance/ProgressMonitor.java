/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors progress of a task using a separate thread, sending periodic
 * updates to an observer.
 * 
 * The assumption is that the task has a fixed number of steps and provides
 * its status by indicating how many steps it has completed. 
 * 
 * @author jbuhacoff
 */
public class ProgressMonitor {
//    private Logger log = LoggerFactory.getLogger(getClass());
    private Monitor<Progress> monitor;
    private Progress task;
    private Observer<Progress> observer;
    public ProgressMonitor(Progress task) {
        this.task = task;
        this.observer = new ProgressLogObserver();
    }
    public ProgressMonitor(Progress task, Observer<Progress> observer) {
        this.task = task;
        this.observer = observer;
    }

    public static class ProgressValue implements Value<Progress> {
        private Logger log = LoggerFactory.getLogger(getClass());
        private Progress progress;
        public ProgressValue(Progress progress) {
            this.progress = progress;
        }
        @Override
        public Progress getValue() { return progress; }
    }
    
    public static class ProgressLogObserver implements Observer<Progress> {
        private Logger log = LoggerFactory.getLogger(getClass());
        @Override
        public void observe(Progress object) {
            log.debug("Progress: {} of {}", object.getCurrent(), object.getMax());
        }
        
    }
    
    public void start() {
        monitor = new Monitor<>(new ProgressValue(task), observer);
        monitor.start();
    }
    
    public void stop() {
        monitor.stop();
    }
    
}
