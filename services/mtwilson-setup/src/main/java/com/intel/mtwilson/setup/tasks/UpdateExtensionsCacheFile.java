/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.launcher.ExtensionDirectoryLauncher;
import com.intel.mtwilson.launcher.ext.annotations.Background;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.setup.LocalSetupTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Create or update the extension cache file by scanning available jar files
 * for extensions 
 * 
 * You can execute this task from command line with something like this:
 * mtwilson setup setup-manager update-extensions-cache-file
 * 
 * @author jbuhacoff
 */
public class UpdateExtensionsCacheFile extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateExtensionsCacheFile.class);
    
    private String getCacheFilePath() {
        return MyFilesystem.getApplicationFilesystem().getConfigurationPath()+File.separator+"extensions.cache";
    }
    private File getCacheFile() {
        return new File(getCacheFilePath());
    }
    
    @Override
    protected void configure() throws Exception {
        checkFileExists("Mt Wilson configuration folder", MyFilesystem.getApplicationFilesystem().getConfigurationPath());
    }

    @Override
    protected void validate() throws Exception {
        if( checkFileExists("Extension cache file", getCacheFilePath()) ) {
            // load the cache
            Set<String> cache = loadCache();
            // initialize the whiteboard
            Set<String> extensions = getWhiteboardExtensions();
            // compare the cache to the loaded extensions
            HashSet<String> inCacheButNotPresent = new HashSet<>();
            HashSet<String> presentButNotInCache = new HashSet<>();
            inCacheButNotPresent.addAll(cache);
            inCacheButNotPresent.removeAll(extensions);
            presentButNotInCache.addAll(extensions);
            presentButNotInCache.removeAll(cache);
            if( !inCacheButNotPresent.isEmpty() ) {
                validation("Extensions cache contains removed extensions: %s", StringUtils.join(", ", inCacheButNotPresent));
            }
            if( !inCacheButNotPresent.isEmpty() ) {
                validation("Extensions cache missing added extensions: %s", StringUtils.join(", ", presentButNotInCache));
            }
        }
        
    }
    
    private void scanExtensions() {
        ExtensionDirectoryLauncher launcher = new ExtensionDirectoryLauncher();
        launcher.setRegistrars(new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class), new AnnotationRegistrar(V1.class), new AnnotationRegistrar(RPC.class), new AnnotationRegistrar(Background.class) });
        launcher.run(); // loads and scans the jars
    }
    
    private Set<String> getWhiteboardExtensions() {
        // first use the directory launcher to scan and register all available extensions
        scanExtensions();
        // second make a list of loaded classes
        Map<String,List<Class<?>>> map = Extensions.getWhiteboard();
        Collection<List<Class<?>>> collection = map.values();
        // consolidate the whiteboard datastructure into a set of unique extension class names
        HashSet<Class<?>> set = new HashSet<>();
        for(List<Class<?>> list : collection) {
            set.addAll(list);
        }
        HashSet<String> extensions = new HashSet<>();
        for(Class<?> clazz : set) {
//            log.debug("Caching extension: {}", clazz.getName());
            extensions.add(clazz.getName());
        }
        return extensions;
    }
    
    private void storeCache(Set<String> extensions) throws IOException {
        String text = StringUtils.join(extensions, "\n");
        try(FileOutputStream out = new FileOutputStream(new File(getCacheFilePath()))) {
            IOUtils.write(text, out);
        }        
    }
    
    private Set<String> loadCache() throws IOException {
            HashSet<String> cache = new HashSet<>();
            try(FileInputStream in = new FileInputStream(getCacheFile())) {
                String text = IOUtils.toString(in);
                String[] lines = text.split("\n");
                for(String line : lines) {
                    if( !line.trim().isEmpty() ) {
                        cache.add(line.trim());
                    }
                }
            }        
        return cache;
    }

    @Override
    protected void execute() throws Exception {
        if( checkFileExists("Extension cache file", getCacheFilePath()) ) { 
            getCacheFile().delete(); 
        }
        Set<String> extensions = getWhiteboardExtensions();
        storeCache(extensions);
    }
    
}
