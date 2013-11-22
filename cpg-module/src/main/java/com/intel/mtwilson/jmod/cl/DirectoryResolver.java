/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod.cl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author jbuhacoff
 */
public class DirectoryResolver implements FileResolver {
    private File directory;
    public DirectoryResolver(File directory) {
        this.directory = directory;
    }
    
    @Override
    public Set<File> resolveClasspath(Manifest manifest) {
        HashSet<File> files = new HashSet<File>();
        String classpath[] = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH).split(" ");
        for(String artifactName : classpath) {
            files.add(directory.toPath().resolve(artifactName).toFile());
        }
        return files;
    }
    
}
