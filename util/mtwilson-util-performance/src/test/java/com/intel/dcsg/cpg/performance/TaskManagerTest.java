/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.performance;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TaskManagerTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static AlarmClock delay = new AlarmClock(1000); // 1 second delay between tasks
    
    public static class HelloWorld implements Runnable {
        private Logger log = LoggerFactory.getLogger(getClass());
        @Override
        public void run() {
            log.debug("Hello world");
            delay.sleep();
        }
    }
    
    public List<Task> createTasks(int howMany) {
        ArrayList<Task> list = new ArrayList<Task>();
        for(int i=0; i<howMany; i++) {
            list.add(new Task(new HelloWorld()));
        }
        return list;
    }
    
    // should see for each task "Starting task X" then a bunch of "Observed: X"  for each one
    @Test
    public void testTaskProgressIterator() {
        ProgressIterator<Task> iterator = new ProgressIterator<Task>(createTasks(10)); // default observer is debug log
        while(iterator.hasNext()) {
            Task task = iterator.next();
            log.debug("Starting task {}", iterator.getValue());
            task.run();
        }
    }
    
    /**
     * Should see progress 1/10, 2/10, ... 10/10
     */
    @Test
    public void testTaskCollectionProgress() {
        TaskCollection tasks = new TaskCollection(createTasks(10));
        ProgressMonitor monitor = new ProgressMonitor(tasks);
        monitor.start();
        tasks.run();
        monitor.stop();
    }

    
    /**
     * Should see progress 1/25, 2/25, ... 25/25   because the top-level TaskCollection asks subordinate
     * TaskCollections about their progress and considers it when reporting overall progress.
     */
    @Test
    public void testTaskManagerCollectionProgress() {
        TaskCollection list1 = new TaskCollection(createTasks(5));
        TaskCollection list2 = new TaskCollection(createTasks(5));
        TaskCollection list3 = new TaskCollection(createTasks(10));
        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(list1);
        tasks.addAll(createTasks(2));
        tasks.add(list2);
        tasks.addAll(createTasks(3));
        tasks.add(list3);
        TaskCollection alltasks = new TaskCollection(tasks);
        ProgressMonitor monitor = new ProgressMonitor(alltasks);
        monitor.start();
        alltasks.run();
        monitor.stop();
    }
    


}
