/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.extensions.cache;

import com.intel.dcsg.cpg.classpath.JarClassIterator;
import com.intel.dcsg.cpg.classpath.JarFileClassLoader;
import com.intel.dcsg.cpg.extensions.AnnotationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.extensions.Scanner;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.launcher.ext.annotations.Background;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.launcher.ws.ext.V1;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.setup.LocalSetupTask;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ext.Provider;
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
    
    private File cacheFile;
    private List<File> jarFiles;
    private List<String> includePackages = null;
    private List<String> excludePackages = null;
    private FileFilter fileIncludeFilter = null;
    
    private String getCacheFilePath() {
        return Folders.configuration() + File.separator + "extensions.cache";
        /*
        Home home = new Home();
        Subfolder configuration = new Subfolder("configuration", home);
//        return MyFilesystem.getApplicationFilesystem().getConfigurationPath()+File.separator+"extensions.cache";
        return configuration.getPath() + File.separator + "extensions.cache";
        * */
    }
    
    public File getCacheFile() {
        if( cacheFile == null ) {
            cacheFile = new File(getCacheFilePath());
        }
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public void setJarFiles(List<File> jarFiles) {
        this.jarFiles = jarFiles;
    }

    /**
     * Returns a list of File objects corresponding to each entry in the 
     * classpath; skips directories such as "classes".
     * @return 
     */
    public List<File> getJarFiles() {
        if( jarFiles == null ) {
            String classpath = System.getProperty("java.class.path");
            String[] entries = StringUtils.split(classpath, System.getProperty("path.separator"));
            jarFiles = new ArrayList<>();
            for(String entry : entries) {
                File file = new File(entry);
                if( file.isFile() ) {
                    if( fileIncludeFilter == null || fileIncludeFilter.accept(file) ) {
                        jarFiles.add(file);
                    }
                }
            }
        }
        return jarFiles;
    }

    public void setIncludePackages(List<String> includePackages) {
        this.includePackages = includePackages;
    }

 // as a default, we include only plugins from com.intel.*  
            // if user needs to override, they can define includePackages 
    public List<String> getIncludePackages() {
        if( includePackages == null ) {
            includePackages = Arrays.asList(new String[] {"com.intel"});

        }
        return includePackages;
    }

    public void setExcludePackages(List<String> excludePackages) {
        if( excludePackages == null ) {
            excludePackages = Arrays.asList(new String[] {"java", "javax"});

        }
        this.excludePackages = excludePackages;
    }

    public List<String> getExcludePackages() {
        return excludePackages;
    }

    public void setFileIncludeFilter(FileFilter fileIncludeFilter) {
        this.fileIncludeFilter = fileIncludeFilter;
    }

    /**
     * Filter used to limit which jar files are scanned. If the filter
     * is null, then all jar files in the classpath are scanned. 
     * If the filter is set, then only files accepted by the filter are scanned.
     * @return 
     */
    public FileFilter getFileIncludeFilter() {
        return fileIncludeFilter;
    }
    
    
    
    
    @Override
    protected void configure() throws Exception {
        File extensionsCacheFile = getCacheFile();  // either set via setCacheFile() or determined automatically by getCacheFile()
        log.debug("Extensions cache file: {}", extensionsCacheFile.getAbsolutePath());
        // it's ok if the file itself doesn't exist yet, but the configuration folder it's supposed to be in should exist
        File configurationFolder = extensionsCacheFile.getParentFile();
        checkFileExists("Configuration folder", configurationFolder.getAbsolutePath());
        
        // option to load any FileFilter instance by class name (must have no-arg constructor) -  specifically not using extensions here since they might not be loaded if it's the first time around.
        String fileIncludeFilterClassName = getConfiguration().get("mtwilson.extensions.fileIncludeFilter.class");
        if( fileIncludeFilterClassName != null ) {
            try {
                log.debug("Loading file include filter class: {}", fileIncludeFilterClassName);
                Class fileIncludeFilterClass = Class.forName(fileIncludeFilterClassName);
                fileIncludeFilter = (FileFilter)fileIncludeFilterClass.newInstance();
            }
            catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error("Cannot load file include filter: {}", fileIncludeFilterClassName, e);
            }
        }
        
        // option to use our built-in keyword filter, which simply looks for the filename to contain any one of a number of keywords
        String fileIncludeFilterContains = getConfiguration().get("mtwilson.extensions.fileIncludeFilter.contains");
        if( fileIncludeFilterContains != null ) {
            log.debug("Using FileNameContains filter with keywords: {}", fileIncludeFilterContains);
            String[] containsKeywords = StringUtils.split(fileIncludeFilterContains, ", ");
            fileIncludeFilter = new FileNameContains(containsKeywords);
            getConfiguration().set("mtwilson.extensions.fileIncludeFilter.contains", fileIncludeFilterContains); // set it again in configuration to ensure it gets saved in the config file during initial setup
        }
    }

    @Override
    protected void validate() throws Exception {
        if( checkFileExists("Extension cache file", getCacheFile().getAbsolutePath()) ) {
            
            // first implementation was not good because it caused all the
            // jars to be scanned 3 times: pre-validate, execute, post-validate.
            
            /*
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
            */
            
            // second implementation compares date of extensions.cache file
            // to date of all jars that will be scanned - so it's much quicker
            // to determine if it's out of date;
            // not concerned about jar files being deleted because if an 
            // extension listed in cache isn't available it will be automatically
            // ignored. 
            if( !isUpdated(getCacheFile(), getJarFiles())) {
                validation("Extension cache is out of date");
            }
        }
        else {
            validation("Extensions cache file does not exist");
        }
        
    }
    
    /**
     * 
     * @param target that should be modified whenever any source file changes
     * @param sources to check against
     * @return true if target is already updated (modified date more recent than any source) and false if target needs to be updated
     */
    private boolean isUpdated(File target, List<File> sources) {
        boolean updated = true;
        for(File source : sources) {
            if( source.lastModified() > target.lastModified()) {
                log.debug("Source {} is updated", source.getAbsolutePath());
                updated = false;
            }
        }
        return updated;
    }
    
    
    private void scanExtensions() {
        // scan each jar in the classpath
        Scanner scanner = new Scanner(new Registrar[] { new ImplementationRegistrar(), new AnnotationRegistrar(V2.class), new AnnotationRegistrar(V1.class), new AnnotationRegistrar(RPC.class), new AnnotationRegistrar(Background.class), new AnnotationRegistrar(Provider.class) });
        scanner.setIncludePackages(getIncludePackages());
        scanner.setExcludePackages(getExcludePackages());
        for(File jarFile : getJarFiles()) {
            try {
                JarClassIterator it = new JarClassIterator(jarFile, new JarFileClassLoader(jarFile)); // throws IOException
                scanner.scan(it);
            }
            catch(IOException e) {
                log.debug("Failed to scan file: {}", jarFile.getAbsolutePath(), e);
            }
        }
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
        try(FileOutputStream out = new FileOutputStream(getCacheFile())) {
            IOUtils.write(text, out);
        }        
    }
    
    // Commenting out as it is not being used
    /*
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
    }*/

    @Override
    protected void execute() throws Exception {
        if( checkFileExists("Extension cache file", getCacheFile().getAbsolutePath()) ) { 
            getCacheFile().delete(); 
        }
        Set<String> extensions = getWhiteboardExtensions();
        storeCache(extensions);
    }
    
}
