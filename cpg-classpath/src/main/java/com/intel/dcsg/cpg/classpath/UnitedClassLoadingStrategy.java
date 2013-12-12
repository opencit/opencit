/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;

/**
 *
 * @author jbuhacoff
 */
public class UnitedClassLoadingStrategy implements ClassLoadingStrategy {
    // XXX TODO the strategy needs to be initialized with the list of all modules and all libraries so it can
    // make a single multijarfileclassloader with all of them and return the same instance

    private MultiJarFileClassLoader cl = new MultiJarFileClassLoader();
    private HashSet<File> classpath = new HashSet<File>();

    /*
     * XXX  TODO    this clss loader strategy should initialize ONE class loader with ALL jars for the application
     * and always retrun the same classlaoder instance for all modules.
     * 
     * Only files that are not already in the total classpath are added to it. 
     * 
     */
    @Override
    public ClassLoader getClassLoader(File jar, Manifest manifest, FileResolver resolver) throws IOException {
        Set<File> files = resolver.resolveClasspath(manifest);
        // figure out what's not already on our classpath and then add it to the class loader
        files.removeAll(classpath); // remove files that are already in our classpath
        cl.add(jar); // the module jar file itself needs to be in the classpath
        cl.add(files); // the dependencies of the module jar file are added after it
        // update the total classpath with the new files we added
        classpath.add(jar); 
        classpath.addAll(files);
        return cl;
    }
}
