/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Resolves entries in the Class-Path attribute of a MANIFEST.MF file to
 * files relative to a given directory.
 * @author jbuhacoff
 */
public class DirectoryResolver implements FileResolver {
    private File directory;
    public DirectoryResolver(File directory) {
        this.directory = directory;
    }
    
    @Override
    public Set<File> resolveClasspath(Manifest manifest) {
        HashSet<File> files = new HashSet<>();
        String classpath[] = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH).split(" ");
        for(String artifactName : classpath) {
            files.add(directory.toPath().resolve(artifactName).toFile());
        }
        return files;
    }
    
}
