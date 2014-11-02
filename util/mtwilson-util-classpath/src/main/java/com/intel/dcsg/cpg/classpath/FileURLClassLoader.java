/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Wrapper for URLClassLoader for applications that want to load jars from disk
 * and don't want to convert File to URL all the time.
 * 
 * Note that this class does NOT change classloading behavior - so it looks up
 * first and then down, same as URLClassLoader.
 * 
 * @author jbuhacoff
 */
public class FileURLClassLoader extends URLClassLoader {
    public FileURLClassLoader() {
        super(new URL[0]);
    }
    public FileURLClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }
    public FileURLClassLoader(File[] jars) throws MalformedURLException {
        super(toURLs(jars));
    }
    public FileURLClassLoader(File[] jars, ClassLoader parent) throws MalformedURLException {
        super(toURLs(jars), parent);
    }
    
    public void add(File[] jars) throws MalformedURLException {
        for(int i=0; i<jars.length; i++) {
            addURL(jars[i].toURI().toURL());
        }
    }
    
    private static URL[] toURLs(File[] files) throws MalformedURLException {
        URL[] urls = new URL[files.length];
        for(int i=0; i<files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }
}
