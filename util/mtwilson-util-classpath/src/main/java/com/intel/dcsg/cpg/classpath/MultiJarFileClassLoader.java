/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarFile;

/**
 * The MultiJarFileClassLoader can load classes from a set of jar files. It behaves just like the JarFileClassLoader but
 * with multiple jars instead of one jar.
 *
 * TODO  should rename it to LimitedMultiJarFileClassLoader  to indicate it looks down first and then up
 * (opposite order from URLClassLoader)
 *
 * @author jbuhacoff
 */
public class MultiJarFileClassLoader extends LimitedClassLoader implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MultiJarFileClassLoader.class);
//    private final File[] classpath;
//    private final JarFile[] jarFiles;
//    private final ConcurrentHashMap<File,JarFile> files = new ConcurrentHashMap<File,JarFile>();
//    private final List<ClasspathEntry> classpath = Collections.synchronizedList(new ArrayList<ClasspathEntry>());
    private final ArrayList<ClasspathEntry> classpath = new ArrayList<ClasspathEntry>();

    /**
     * Create a MultiJarFileClassLoader with an empty classpath; you need to call add(File[] classpath) in order to have
     * a usable classloader.
     *
     * @throws IOException
     */
    public MultiJarFileClassLoader() {
        super();
    }

    /**
     * Create a MultiJarFileClassLoader with an empty classpath; you need to call add(File[] classpath) in order to have
     * a usable classloader.
     *
     * @param parent
     * @throws IOException
     */
    public MultiJarFileClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Create a MultiJarFileClassLoader with the given classpath. You can still call add(File[] classpath) later to add
     * more files to the classpath.
     *
     * @param classpath
     * @throws IOException
     */
    public MultiJarFileClassLoader(File[] classpath) throws IOException {
        super();
        /*
         this.classpath = classpath;
         this.jarFiles = new JarFile[classpath.length];
         for(int i=0; i<classpath.length; i++) {
         jarFiles[i] = new JarFile(classpath[i]); // throws IOException
         }*/
        /*
         for(File file : classpath) {
         files.put(file, new JarFile(file)); // throws IOException
         }*/
        add(classpath);
    }

    /**
     * Create a MultiJarFileClassLoader with the given classpath. You can still call add(File[] classpath) later to add
     * more files to the classpath.
     *
     * @param classpath
     * @param parent
     * @throws IOException
     */
    public MultiJarFileClassLoader(File[] classpath, ClassLoader parent) throws IOException {
        super(parent);
        /*
         this.classpath = classpath;
         this.jarFiles = new JarFile[classpath.length];
         for(int i=0; i<classpath.length; i++) {
         jarFiles[i] = new JarFile(classpath[i]); // throws IOException
         }*/
        /*
         for(File file : classpath) {
         files.put(file, new JarFile(file)); // throws IOException
         }*/
        add(classpath);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
//            synchronized (classpath) {
                for (ClasspathEntry entry : classpath) { //for(Entry<File,JarFile> entry : files.entrySet()) { //for(int i=0; i<classpath.length; i++) {
                    byte[] data = JarUtil.readClass(entry.jarFile /*entry.getValue()*/ /*jarFiles[i]*/, name);
                    if (data == null) {
                        continue;
                    }
                    Class<?> local = defineClass(name, data, 0, data.length);
                    log.debug("Loaded class {} from file {}", name, entry.file/*entry.getKey()*/ /*classpath[i]*/.getName());
                    return local;
                }
//            }
            log.debug("Cannot find class {}", name);
            return null;
        } catch (Exception e) {
            log.debug("Cannot load class {}", name, e);
            throw new ClassNotFoundException("Cannot load class: " + name, e);
        }
    }

    /**
     * You can add jars to the classpath. Jar files are scanned for classes in the order they appear on the classpath.
     *
     * @param classpath
     */
    public final void add(File[] classpath) throws IOException {
        for (File file : classpath) {
//            this.classpath.add(new ClasspathEntry(file, new JarFile(file)));
            add(file);
        }
    }

    public final void add(Collection<File> classpath) throws IOException {
        for (File file : classpath) {
            /*
            if( !file.exists() ) {
                log.warn("Cannot add missing file to classpath: {}", file.getAbsolutePath());
                continue;
            }
            if( file.isDirectory() ) {
                log.warn("Cannot add directory to classpath: {}", file.getAbsolutePath());
                continue;
            }
            if( file.exists() && file.isFile() ) {
                this.classpath.add(new ClasspathEntry(file, new JarFile(file)));
            }
            */
            add(file);
        }
    }

    public final void add(File file) throws IOException {
//        this.classpath.add(new ClasspathEntry(file, new JarFile(file)));
        if( !file.exists() ) {
            log.warn("Cannot add missing file to classpath: {}", file.getAbsolutePath());
            return;
        }
        if( file.isDirectory() ) {
            log.warn("Cannot add directory to classpath: {}", file.getAbsolutePath());
            return;
        }
        if( !file.isFile() ) {
            log.warn("Cannot add non-file to classpath: {}", file.getAbsolutePath());
            return;
        }
        this.classpath.add(new ClasspathEntry(file, new JarFile(file)));
    }

    @Override
    public void close() throws IOException {
        /*
         for(int i=0; i<jarFiles.length; i++) {
         jarFiles[i].close();
         }*/
        /*
         for(JarFile jarFile : files.values()) {
         jarFile.close();
         }*/
        for (ClasspathEntry entry : classpath) {
            entry.jarFile.close();
        }
    }

    private static class ClasspathEntry {

        File file;
        JarFile jarFile;

        ClasspathEntry(File file, JarFile jarFile) {
            this.file = file;
            this.jarFile = jarFile;
        }
    }
}
