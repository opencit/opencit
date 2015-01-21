/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.util.filesystem.Home;
import com.intel.mtwilson.util.filesystem.Subfolder;
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
 * mtwilson setup update-extensions-cache-file
 * 
 * Or with an earlier version:  mtwilson setup setup-manager update-extensions-cache-file
 * 
 * @author jbuhacoff
 */
public class UpdateExtensionsCacheFile extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateExtensionsCacheFile.class);
    
    private String getCacheFilePath() {
        Home home = new Home();
        Subfolder configuration = new Subfolder("configuration", home);
//        return MyFilesystem.getApplicationFilesystem().getConfigurationPath()+File.separator+"extensions.cache";
        return configuration.getPath() + File.separator + "extensions.cache";
    }
    private File getCacheFile() {
        return new File(getCacheFilePath());
    }
    
    @Override
    protected void configure() throws Exception {
        File extensionsCacheFile = new File(getCacheFilePath());
        File configurationFolder = extensionsCacheFile.getParentFile();
        checkFileExists("Mt Wilson configuration folder", configurationFolder.getAbsolutePath());
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
//        ExtensionDirectoryLauncher launcher = new ExtensionDirectoryLauncher();
//        launcher.setRegistrars(new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class), new AnnotationRegistrar(V1.class), new AnnotationRegistrar(RPC.class), new AnnotationRegistrar(Background.class) });
//        launcher.run(); // loads and scans the jars
        // **** WHAT'S NEEDED HERE IS REFERENCE TO SOMETHING IN MTWILSON-UTIL-EXTENSIONS THAT WILL SCAN ALL AVAILABLE EXTENSIONS.... SINCE IT CAN BE IN /OPT/MTWILSON/JAVA/* OR IN /OPT/MTWSILON/FEATURES/*/JAVA/*  NEED TOD ECIDE IF THAT'S ONE IMPLEMENTATION OR TWO, AND IF IT'S IMPLEMENTED IN MTWILSON-EXTENSIONS-FEATURES (WHICH WOULD ALSO COVER ANY OTHER FEATURE'S /OPT/MTWILSON/FEATURES/*/{WHATEVER} SCANNING) OR IN A COMBINATION OF MTWILSON-UTIL-EXTENSIONS AND MTWILSON-*SOMETHING* 
        // **** (this is NOT the ExtensionCacheLoader which loads it from the extesnions.cache file,  it's the scanner that reads all the *.jar files to find extensions)
        // **** AND it's also NOT a "launcher" because that specific task is going to be in the mtwilson-launcher project and will DEPEND on this and the other projects
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
