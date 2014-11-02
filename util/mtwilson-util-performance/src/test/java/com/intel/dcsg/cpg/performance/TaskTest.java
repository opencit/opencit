/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.math3.stat.StatUtils;
import com.intel.dcsg.cpg.performance.*;
import com.intel.dcsg.cpg.performance.report.*;
import static com.intel.dcsg.cpg.performance.report.PerformanceUtil.*;
import java.util.ArrayList;
import java.util.List;
import static java.lang.String.format;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TaskTest {
    private static int timeout = 30; // seconds
    private static Logger log = LoggerFactory.getLogger(TaskTest.class);
    
    @Test
    public void testMultithreadedTasks() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        List<HelloWorldTask> tasks = prepareHelloWorldTasks();
        scheduleConcurrentServerTasks(tasks);
    }
    
    private static void printPerformanceInfo(PerformanceInfo info) {
        log.debug("Number of executions: {}", info.getData().length);
        log.debug("Average time: {} ms", info.getAverage());
        log.debug("Min time: {} ms", info.getMin());
        log.debug("Max time: {} ms", info.getMax());
    }
    
    @Test
    public void testMeasureTask() {
        int n = 10;
        PerformanceInfo info = measureSingleTask(new HelloWorldTask("foo"), n);
        printPerformanceInfo(info);
        long[] data = info.getData();
        for(int i=0; i<data.length; i++) {
            System.out.println(String.format("%d\t%d", i, data[i]));
        }
    }
    
    /**
     * the results do not take into account the overhead cost of setting up the multi-threading because each task is measured
     * only immediately before it starts and immediately after --  however the cost of switching while executing is included.
     */
    @Test
    public void testMeasureConcurrentTask() {
        int n = 10;
        ArrayList<HelloWorldTask> tasks = new ArrayList<HelloWorldTask>(n);
        for(int i=0; i<n; i++) {
            tasks.add(new HelloWorldTask(String.format("Task %d", i+1)));
        }
        PerformanceInfo info = measureMultipleConcurrentTasks(tasks, 5); // 5 second timeout
        printPerformanceInfo(info);
    }

    
    private static List<HelloWorldTask> prepareHelloWorldTasks() throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException, IOException {
        ArrayList<HelloWorldTask> tasks = new ArrayList<HelloWorldTask>();
        tasks.add(new HelloWorldTask("foo"));
        tasks.add(new HelloWorldTask("bar"));
        return tasks;
    }
    
    private static void scheduleConcurrentServerTasks(List<? extends Task> tasks) {
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
                log.error("Exception while retrieving the status of the tasks. {}", ex.getMessage());
            }
        }
        // print the status of each task
        for(Task task : tasks) {
            printTaskStatus(task);
        }
    }
    

    
    
    private static void printTaskStatus(Task task) {
        log.debug("Task results: {}", task.getId());
        if( task.isDone() ) {
            log.debug("+ completed [{} ms]", task.getStopTime() - task.getStartTime());
        }
        else if( task.isError() ) {
            log.debug("+ error: {} [{} ms]", task.getCause().toString(), task.getStopTime() - task.getStartTime());
        }
        else {
            log.debug("+ timeout");
        }
    }
            
    
    private static class HelloWorldTask extends Task {
        private final String message;
        public HelloWorldTask(String message) {
            super(message); // or can use the hashcode of the messsage as the id: String.valueOf(message.hashCode())
            this.message = message;
        }
        @Override
        public void execute() throws Exception {
            log.debug(format("HelloWorldTask[%s]: %s", getId(), message));
        }
        public String getMessage() { return message; }
    }
    
}
