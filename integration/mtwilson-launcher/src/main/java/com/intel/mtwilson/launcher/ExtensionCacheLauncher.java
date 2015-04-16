/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.extensions.Scanner;
import com.intel.mtwilson.Folders;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionCacheLauncher extends ExtensionLauncher implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionCacheLauncher.class);
    
    private File cacheFile;
    private Registrar[] registrars;
    
    /**
     * Initializes member variables parentClassLoader, 
     * applicationClassLoader, and javaFolder; 
     * they can be replaced using the
     * available setter methods.
     * 
     * The applicationClassLoader is configured with parentClassLoader as its
     * parent; if you replace with another classloader you will have to set
     * its parent yourself.
     */
    public ExtensionCacheLauncher() {
        // look for java extension cache file
        //Subfolder configuration = new Subfolder("configuration", new Home());
        String cachePath = Folders.configuration() + File.separator + "extensions.cache"; //MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "extensions.cache";
        log.debug("default extension cache file: {}", cachePath);
        cacheFile = new File(cachePath);
        registrars = new Registrar[] { new ImplementationRegistrar() } ;        
        log.debug("thread context class loader: {}", Thread.currentThread().getContextClassLoader().getClass().getName());
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
        log.debug("set extension cache file: {}", cacheFile.getAbsolutePath());
    }


    public File getCacheFile() {
        return cacheFile;
    }
    
    
    public Registrar[] getRegistrars() {
        return registrars;
    }

    
    public void setRegistrars(Registrar[] registrars) {
        this.registrars = registrars;
    }
    
    
    
    
    @Override
    public void run() {
        // add all the jar files to a classloader
        try {
            load(getCacheFile(), getRegistrars());
        }
        catch(IOException e) {
            log.error("Cannot load extension cache file", e);
        }
        
    }
    
    public void load(File cacheFile, Registrar[] registrars) throws IOException {
        if( cacheFile.exists() ) {
            long time0 = System.currentTimeMillis();
            long count = 0;
            Scanner scanner = new Scanner(registrars);
            try(FileInputStream in = new FileInputStream(cacheFile)) {
                String content = IOUtils.toString(in);
                String[] lines = content.split("[\n\r]");
                for(int i=0; i<lines.length; i++) {
                    if( lines[i] == null || lines[i].trim().isEmpty() ) { continue; }
                    String className = lines[i];
                    try {
                        log.debug("Loading extension: {}", className);
                        Class<?> clazz = Class.forName(className);
                        scanner.scan(clazz);
                        count++;
                    }
                    catch(Exception e) {
                        log.error("Cannot load class {}: {}", className, e.getMessage());
                    }
                }
            }
            long time1 = System.currentTimeMillis();
            log.debug("Loaded {} extensions in {}ms", count, time1-time0);
        }
        else {
            log.warn("Extension cache file does not exist: {}", cacheFile.getAbsolutePath());
        }
    }
    
}
