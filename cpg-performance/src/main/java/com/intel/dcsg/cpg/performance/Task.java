/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

/**
 * You can use Task to wrap any Runnable in order to provide timing information, or
 * you can extend Task and override the execute() method. 
 * 
 * If your task can provide progress information, you should wrap it with TaskProgress
 * or extend TaskProgress.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class Task implements Runnable {

    private final String id;
    private Runnable runnable = null;
    private Throwable error = null;
    private boolean done = false;
    private long startTime = 0;
    private long stopTime = 0;
    
    public Task() {
        this.id = getClass().getName();
    }
    
    /**
     * 
     * @param id arbitrary identifier provided by the caller to identify this task
     */
    public Task(String id) {
        this.id = id;
    }
    
    public Task(Runnable runnable) {
        this.id = runnable.getClass().getName();
        this.runnable = runnable;
    }
    
    public Task(String id, Runnable runnable) {
        this.id = id;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            startTime = System.currentTimeMillis();
            execute();
            stopTime = System.currentTimeMillis();
            done = true;
        } catch (Throwable e) {
            stopTime = System.currentTimeMillis();
            error = e;
        }
    }

    /**
     * The default implementation does nothing. Subclasses of Task should override execute() in order to
     * do something useful.
     * @throws Exception 
     */
    protected void execute() throws Exception {
        if( runnable != null ) {
            runnable.run();
        }
    }

    /**
     * 
     * @return the id of the task as passed into its constructor
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @return true if the task was interrupted due to an error
     */
    public boolean isError() {
        return error != null;
    }

    /**
     * 
     * @return the cause of the error, or null if there is no error or no known cause
     */
    public Throwable getCause() {
        return error;
    }

    /**
     * 
     * @return true if the task finished running, false if it has not finished yet or if it was interrupted due to an error
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * 
     * @return system clock (ms) when the task started running, or zero if it hasn't started yet
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * 
     * @return system clock (ms) when the task finished running, or zero if it hasn't completed yet (or if it was interrupted due to an error)
     */
    public long getStopTime() {
        return stopTime;
    }
    
}
