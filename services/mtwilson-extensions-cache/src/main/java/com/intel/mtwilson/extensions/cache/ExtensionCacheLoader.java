/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.extensions.Scanner;
import com.intel.mtwilson.launcher.ext.annotations.Background;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionCacheLoader implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionCacheLoader.class);
    
    private File cacheFile;
    private Registrar[] registrars;
    
    /**
     */
    public ExtensionCacheLoader(String configurationPath) {
        this(new File(configurationPath + File.separator + "extensions.cache"));
    }

    public ExtensionCacheLoader(File extensionsCacheFile) {
        log.debug("extension cache file: {}", extensionsCacheFile.getAbsolutePath());
        cacheFile = extensionsCacheFile;
        registrars = new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class), new AnnotationRegistrar(V1.class), new AnnotationRegistrar(RPC.class), new AnnotationRegistrar(Background.class), new AnnotationRegistrar(Provider.class) } ;        
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
            if( cacheFile.exists() ) {
                load(cacheFile, registrars);
            }
            else {
                log.debug("Cache file does not exist: {}", cacheFile.getAbsolutePath());
            }
        }
        catch(IOException e) {
            log.error("Cannot load extension cache file", e);
        }
        
    }
    
    /**
     * It is an error to call this method when the cache file does not exist.
     * 
     * @param cacheFile
     * @param registrars
     * @throws IOException 
     */
    public void load(File cacheFile, Registrar[] registrars) throws IOException {
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
    
}
