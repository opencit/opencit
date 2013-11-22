/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod.cl;

import java.io.File;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * Given a jar file manifest,  a file resolver returns a set of files corresponding to the classpath declared
 * in that manifest.  
 * 
 * This abstraction is necessary because DirectoryLauncher uses the Class-Path attribute while the MavenLauncher
 * uses the Maven-Classpath attribute. The contents of these attributes are different ways to express the path
 * for required dependencies. Class-Path assumes all files are in the same directory, while Maven-Classpath contains
 * paths to files relative to a maven repository root.
 * 
 * @author jbuhacoff
 */
public interface FileResolver {
    Set<File> resolveClasspath(Manifest manifest);
}
