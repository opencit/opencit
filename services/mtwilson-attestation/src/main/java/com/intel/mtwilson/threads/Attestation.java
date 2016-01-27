/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.threads;

import com.intel.dcsg.cpg.classpath.MultiJarFileClassLoader;
import com.intel.dcsg.cpg.classpath.StringWildcardFilter;
import com.intel.dcsg.cpg.io.file.FilenameEndsWithFilter;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.My;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * The fixed thread pool means there are still references to the loaded classes.
 *
 * @author jbuhacoff
 */
@WebListener
public class Attestation implements ServletContextListener {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Attestation.class);
    private static ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.debug("Initializing ASDataCipher...");
        My.initDataEncryptionKey();
        int maxThreads = ASConfig.getConfiguration().getInt("mtwilson.bulktrust.threads.max", 32);
        log.debug("Creating fixed thread pool with n={}", maxThreads);
        executor = Executors.newFixedThreadPool(maxThreads, new AttestationThreadFactory());
    }

    /**
     * Uses example code from Java documentation to shut down the executor:
     * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.debug("Shutdown thread pool");
        if (executor != null) {
            executor.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public static Callable<Object> wrap(Runnable task) {
        return new DiscardableClassLoaderRunnable(task);
    }
    
    public static Collection<Callable<Object>> wrap(Collection<? extends Runnable> tasks) {
        ArrayList<Callable<Object>> callables = new ArrayList<>();
        for(Runnable task : tasks) {
            callables.add(new DiscardableClassLoaderRunnable(task));
        }
        return callables;
    }
    /*
    public static <T> Collection<? extends Callable<T>> wrap(Collection<? extends Runnable> tasks) {
        ArrayList<? extends Callable<T>> callables = new ArrayList<>();
        for (Runnable task : tasks) {
            callables.add(new DiscardableClassLoaderRunnable<T>(task));
        }
        return callables;
    }*/

    public static class AttestationThreadFactory implements ThreadFactory {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttestationThreadFactory.class);
        private static final AtomicLong sequence = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            log.debug("Creating thread for runnable: {}", r.getClass().getName());
            Thread newThread = new Thread(r, "Attestation-" + sequence.incrementAndGet());
            return newThread;
        }
    }

    public static class DiscardableClassLoaderRunnable implements Runnable, Callable<Object> {

        private static final File[] jars = getApplicationJars();
        private Runnable task;

        public DiscardableClassLoaderRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            log.debug("Replacing context class loader: {}", parent.getClass().getName());
            try {
                MultiJarFileClassLoader classLoader = new MultiJarFileClassLoader(jars, parent);
                classLoader.setIncludeFilter(new StringWildcardFilter("com.intel.*"));
                Thread.currentThread().setContextClassLoader(classLoader);
                log.debug("Running task: {}", task.getClass().getName());
                task.run();
                log.debug("Completed task: {}", task.getClass().getName());
            } catch (Throwable e) {
                log.error("Failed to run task: {}", task.getClass().getName(), e);
            } finally {
                log.debug("Finally after task: {}", task.getClass().getName());
                Thread.currentThread().setContextClassLoader(parent);
                log.debug("Restored context class loader: {}", parent.getClass().getName());
            }
        }

        @Override
        public Object call() throws Exception {
            run();
            return null;
        }

        private static File[] getApplicationJars() {
            String javaPath = Folders.application() + File.separator + "java";
            log.debug("Application java path: {}", javaPath);
            File javaFolder = new File(javaPath);
            FilenameEndsWithFilter jarfilter = new FilenameEndsWithFilter(".jar");
            File[] jars = javaFolder.listFiles(jarfilter);
            if (jars == null) {
                return new File[0];
            }
            return jars;
        }
    }
}
