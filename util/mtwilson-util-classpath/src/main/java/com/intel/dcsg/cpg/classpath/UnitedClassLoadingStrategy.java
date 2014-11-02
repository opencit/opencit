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
 * Uses a single class loader that creates a union of jar files that are
 * on the class path of different modules. 
 * 
 * The effect of this is that a single class loader has a reference to all the 
 * jar files used by the application (without duplicates).
 * 
 * @author jbuhacoff
 */
public class UnitedClassLoadingStrategy implements ClassLoadingStrategy {

    private MultiJarFileClassLoader cl = new MultiJarFileClassLoader();
    private HashSet<File> classpath = new HashSet<File>();

    /*
     * Always returns the same ClassLoader instance representing the union of
     * all jar files. However, any new jar files listed in the specified 
     * manifest that are not already on the union class path are added to it.
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
