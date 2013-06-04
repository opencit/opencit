/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class groups together a number of tasks. When executed, it
 * executes each task and keeps track of how many tasks have been
 * completed.
 * 
 * You can call getCurrent() and getMax() to get the progress as 
 * the task that is being executed or was last completed vs the
 * number of tasks in the collection.
 * 
 * The difference between a TaskCollection and a TaskManager is that
 * the progress of a TaskCollection is just the number of tasks
 * completed out of the number of tasks in the collection, while 
 * the progress of a TaskManager takes into account the progress of
 * sub-tasks. 
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class TaskCollection extends Task implements Progress {
    protected Collection<Task> tasks = null;
    protected Task currentTask = null;
    protected long completed = 0; // completed steps not including the current executing task
    protected long max = 0;
    
    public TaskCollection(Collection<Task> tasks) {
        this.tasks = tasks;
        for(Task task : tasks) {
            max += max(task);
        }
    }
    
    @Override
    public void execute() throws Exception {
        Iterator<Task> it = tasks.iterator();
        while(it.hasNext()) {
            currentTask = it.next();
            beforeExecute(currentTask);
            currentTask.run();
            completed += max(currentTask);
            afterExecute(currentTask);
        }
        currentTask = null;
    }
    
    /**
     * Extension point for subclasses to override as necessary. Is called
     * immediately before executing the current task. The current task that is about
     * to be executed is provided as the parameter.
     * @param task 
     */
    protected void beforeExecute(Task task) { }
    
    /**
     * Extension point for subclasses to override as necessary. Is called
     * immediately after executing the current task. The current task that just
     * completed is provided as the parameter.
     * @param task 
     */
    protected void afterExecute(Task task) { }
    
    public Collection<Task> getCollection() { return tasks; }

    @Override
    public Long getCurrent() {
        return completed + current(currentTask);
    }

    @Override
    public Long getMax() {
        return max;
    }
    
    @Override
    public String getId() { if( currentTask == null ) { return ""; } return currentTask.getId(); }
    
    /**
     * @return the currently running task within the collection, or null if execution either hasn't started or has already ended
     */
    public Task getTask() { return currentTask; }
    
    private long max(Task task) {
        if( task == null ) {
            return 0; // there is no task so it cannot have a max number of steps
        }
        else if( task instanceof Progress ) {
            Progress progress = (Progress)task;
            return progress.getMax();
        }
        else {
            return 1; // the task doesn't provide progress info so count the task as one step
        }
    }
    
    private long current(Task task) {
        if( task == null ) {
            return 0; // there is no task so it doesn't count
        }
        else if( task instanceof Progress ) {
            Progress progress = (Progress)task;
            return progress.getCurrent();
        }
        else {
            return 1; // the task doesn't provide progress info so count the task as one step
        }
    }
    
}
