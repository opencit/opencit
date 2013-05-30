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
    private AlarmClock alarm = new AlarmClock(1000);
    private Value<T> value = null;
    private Observer<T> observer = null;
    private Boolean isDone = false;
    private Thread monitorThread = null;
    
    /**
     * Using this constructor you also need to call setObserver before
     * you start()
     */
    public Monitor() {
        this(null,null,new AlarmClock());
    }
    
    public Monitor(AlarmClock clock) {
        this(null,null,clock);
    }
    
    public Monitor(Value<T> value, Observer<T> observer) {
        this(value,observer,new AlarmClock());
    }
    
    public Monitor(Value<T> value, Observer<T> observer, AlarmClock clock) {
        this.value = value;
        this.observer = observer;
        this.alarm = clock;
    }
    
    public void setValue(Value<T> value) {
        this.value = value;
    }
    
    public void setObserver(Observer<T> observer) {
        this.observer = observer;
    }
    
    public void setAlarmClock(AlarmClock alarm) {
        if( alarm == null ) {
            this.alarm = new AlarmClock();
        }
        else {
            this.alarm = alarm;
        }
    }
    
    /**
     * Call this before starting the long-running task, in order to start
     * the progress updates.
     */
    public void start() {
        if( observer == null ) { throw new IllegalArgumentException("No observer is set"); }
        if( value == null ) { throw new IllegalArgumentException("No value to monitor"); }
        if( alarm == null ) { throw new IllegalArgumentException("No delay is set"); }
        monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    alarm.sleep();
                    observer.observe(value.getValue());
                    if(isDone) {
                        break;
                    }
                }
            }
        }, getName());
        monitorThread.start();
    }

    /**
     * Call this after you are finished, to stop the progress updates.
     */
    public void stop() {
        isDone = true;
        waitfor(monitorThread);
    }
    
    private void waitfor(Thread thread) {
        boolean done = false;
        log.debug("Waiting for thread {} to finish", thread.getName());
        while(!done) {
            try {
                thread.join();
                done = true;
                log.debug("Thread {} finished", thread.getName());
            }
            catch(InterruptedException ignored) {
                log.debug("Ignoring interrupt while waiting for {}", thread.getName());
            }
        }
    }
    
    protected String getName() { return "monitor"; }
    
    
}
