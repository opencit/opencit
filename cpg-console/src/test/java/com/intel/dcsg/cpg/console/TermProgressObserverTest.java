package com.intel.dcsg.cpg.console;

/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */


//import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.performance.*;
import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
public class TermProgressObserverTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static class HelloWorldRunnable implements Runnable,Progress {
        private int i=0;
        private int max=10;
        @Override
        public void run() {
            for(i=0;i<max;i++) {
                System.out.println("Hello world!");
                (new AlarmClock()).sleep();
            }
        }

        @Override
        public Long getCurrent() {
            return Long.valueOf(i);
        }

        @Override
        public Long getMax() {
            return Long.valueOf(max);
        }
    }
    
    public static class HelloWorldTask extends Task implements Progress {
        private int i=0;
        private int max=10;
        @Override
        public void execute() throws Exception {
            for(i=0;i<max;i++) {
                System.out.println("Hello world!");
                (new AlarmClock()).sleep();
            }
        }

        @Override
        public Long getCurrent() {
            return Long.valueOf(i);
        }

        @Override
        public Long getMax() {
            return Long.valueOf(max);
        }
    }    
    
    public static class PrintTask extends Task implements Progress {
        private int i=0;
        private int max=3;
        private String text;
        public PrintTask(String text) {
            super(text); // use the text as the name of the task too, such as "Installing files"
            this.text = text;
        }
        @Override
        public void execute() throws Exception {
            for(i=0;i<max;i++) {
                System.out.println(text);
                (new AlarmClock()).sleep();
            }
        }
        @Override
        public Long getCurrent() {
            return Long.valueOf(i);
        }

        @Override
        public Long getMax() {
            return Long.valueOf(max);
        }
        
    }        
    
    @Test
    public void testProgressRunnable() {
        HelloWorldRunnable task = new HelloWorldRunnable();
        ProgressMonitor monitor = new ProgressMonitor(task, new TermProgressObserver());
        monitor.start();
        task.run();
        monitor.stop();
    }

    @Test
    public void testProgressTask() {
        HelloWorldTask task = new HelloWorldTask();
        ProgressMonitor monitor = new ProgressMonitor(task, new TermProgressObserver(task));
        monitor.start();
        task.run();
        monitor.stop();
    }

    @Test
    public void testProgressTaskCollection() {
        ArrayList<Task> list = new ArrayList<Task>();
        list.add(new PrintTask("foo"));
        list.add(new PrintTask("bar"));
        list.add(new PrintTask("baz"));
        list.add(new PrintTask("king"));
        list.add(new PrintTask("queen"));
        list.add(new PrintTask("president"));
        TaskCollection tasks = new TaskCollection(list);
        ProgressMonitor monitor = new ProgressMonitor(tasks, new TermProgressObserver(tasks));
        monitor.start();
        tasks.run();
        monitor.stop();
    }

    
}
