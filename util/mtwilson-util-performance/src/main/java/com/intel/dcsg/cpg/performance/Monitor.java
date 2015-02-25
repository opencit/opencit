/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor in a separate thread an object of type T , sending period
 * updates to an observer of your choice. One application of this class
 * is to make a progress monitor.
 * 
 * You can specify any Observer of type T to accept the progress updates.
 * Default update interval is 1 second, you can override this by providing
 * your own AlarmClock instance configured with a different duration.
 * 
 * After configuring a Monitor, call start() to start monitoring and stop()
 * to stop monitoring.
 * 
 * @author jbuhacoff
 */
public class Monitor<T> {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Value<T> value = null;
    private Observer<T> observer = null;
    private BackgroundThread thread = null;

    public Monitor() {
        
    }
    
    public Monitor(Value<T> value, Observer<T> observer) {
        this.value = value;
        this.observer = observer;
    }
    
    public void setValue(Value<T> value) {
        this.value = value;
    }
    
    public void setObserver(Observer<T> observer) {
        this.observer = observer;
    }

    public void monitor() {
        observer.observe(value.getValue());        
    }
    
    
    /**
     * Call this before starting the long-running task, in order to start
     * the progress updates.
     */
    public void start() {
        if( observer == null ) { throw new IllegalArgumentException("No observer is set"); }
        if( value == null ) { throw new IllegalArgumentException("No value to monitor"); }
        thread = new BackgroundThread(new Runnable() { @Override public void run() { monitor(); } });
        thread.start();
    }
    
    public void stop() {
        if( thread == null ) {
            throw new IllegalStateException("Monitor has not been started");
        }
        thread.stop();
    }

}
