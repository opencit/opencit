/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance.report;

import com.intel.dcsg.cpg.performance.Task;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PerformanceUtil {
    private static Logger log = LoggerFactory.getLogger(PerformanceUtil.class);

    /**
     * Assumes the task is repeatable -- do not use with non-repeatable tasks, or tasks
     * that keep state which would cause them to do something different on each execution,
     * for example deleting a file.
     * 
     * @param task
     * @param howManyTimes
     * @return 
     */
    public static PerformanceInfo measureSingleTask(Task task, int howManyTimes) {
        ArrayList<Long> elapsedTimes = new ArrayList<Long>(howManyTimes);        
        for(int i=0; i<howManyTimes; i++) {
            task.run();
            elapsedTimes.add(task.getStopTime() - task.getStartTime());
            if( task.isDone() ) {
                log.debug("Completed task {}", task.getId());
            }
            else if( task.isError() ) {
                log.debug("Error executing task {}: {}", task.getId(), task.getCause().toString());
            }
            else {
                log.debug("Timeout while executing task {}", task.getId());
            }
        }
        PerformanceInfo info = new PerformanceInfo(elapsedTimes);
        return info;
    }
    
    public static PerformanceInfo measureSingleTaskConcurrent(Runnable task, int howManyTimes, long timeout) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        for(int i=0; i<howManyTimes; i++) {
            tasks.add(new Task(task)); // each one just wraps the same runnable task 
        }
        return measureMultipleConcurrentTasks(tasks, timeout);
    }
    
    /**
     * 
     * @param tasks
     * @param timeout in seconds
     */
    public static PerformanceInfo measureMultipleConcurrentTasks(List<? extends Task> tasks, long timeout) {
        ArrayList<Long> elapsedTimes = new ArrayList<Long>();        
        ExecutorService scheduler = Executors.newFixedThreadPool(tasks.size()); 
        ArrayList<Future<?>> taskStatus = new ArrayList<Future<?>>();
        for(Task task : tasks) {
            Future<?> status = scheduler.submit(task);
            taskStatus.add(status);
        }
        // wait for all tasks to complete
        for (Future<?> status : taskStatus) {
            try {
                status.get(timeout, TimeUnit.SECONDS); // return value will always be null because we submitted "Runnable" tasks
            } catch (Exception ex) {
                // we will log the exception and ignore the error.
                log.error("Exception while retrieving the status of the tasks: {}", ex.toString());
            }
        }
        // get the status of each task
        for(Task task : tasks) {
            if( task.isDone() ) {
                elapsedTimes.add(task.getStopTime() - task.getStartTime());
            }
        }
        PerformanceInfo info = new PerformanceInfo(elapsedTimes);
        return info;
    }
    
    /**
     * 
     * @param timeout in seconds
     * @param runnables one or more tasks to run implementing the Runnable interface
     * @return 
     */
    public static PerformanceInfo measureMultipleConcurrentTasks(long timeout, Runnable... runnables) {
        Task[] tasks = new Task[runnables.length];
        for(int i=0; i<runnables.length; i++) {
            tasks[i] = new Task(runnables[i]);
        }
        return measureMultipleConcurrentTasks(Arrays.asList(tasks), timeout);
    }
    
}
