/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * Uses the system class loader.
 * 
 * @author jbuhacoff
 */
public class SystemClassLoadingStrategy implements ClassLoadingStrategy {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemClassLoadingStrategy.class);

    /**
     * Returns a MultiJarFileClassLoader for the module's entire classpath (including the module jar itself)
     * 
     * If you call this multiple times for the same module/manifest you will get a new instance of the classloader
     * each time.
     * 
     * @param module
     * @return
     * @throws IOException 
     */
    @Override
    public ClassLoader getClassLoader(File jar, Manifest manifest, FileResolver resolver) throws IOException {
        Set<File> dependencies = resolver.resolveClasspath(manifest);
        HashSet<URL> urls = new HashSet<URL>();
        urls.add(jar.toURI().toURL());
        for(File dependency : dependencies) {
            urls.add(dependency.toURI().toURL());
        }
        return new URLClassLoader(urls.toArray(new URL[0]));
    }

    
}
