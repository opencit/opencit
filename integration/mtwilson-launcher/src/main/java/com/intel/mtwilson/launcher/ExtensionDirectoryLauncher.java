/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.classpath.FileURLClassLoader;
import com.intel.dcsg.cpg.classpath.JarClassIterator;
import com.intel.dcsg.cpg.classpath.MultiJarFileClassLoader;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.io.file.FilenameContainsFilter;
import com.intel.dcsg.cpg.io.file.FilenameEndsWithFilter;
import com.intel.dcsg.cpg.performance.CountingIterator;
import com.intel.dcsg.cpg.util.ArrayIterator;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author jbuhacoff
 */
public class ExtensionDirectoryLauncher extends ExtensionLauncher implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionDirectoryLauncher.class);
    
    private File javaFolder;
    private ClassLoader parentClassLoader;
//    private MultiJarFileClassLoader applicationClassLoader;
    private ClassLoader applicationClassLoader;
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
    public ExtensionDirectoryLauncher() {
        // determine the parent classloader to use
        parentClassLoader = Thread.currentThread().getContextClassLoader();
        if(parentClassLoader==null) { parentClassLoader = ExtensionDirectoryLauncher.class.getClassLoader(); }
        // look for java extension directory
//        String javaPath = My.filesystem().getBootstrapFilesystem().getJavaPath(); // for example, /opt/mtwilson/java 
        String javaPath = MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getJavaPath(); // for example, /opt/mtwilson/java
        log.info("Application Java path: {}", javaPath);
//        if( My.configuration().getmtwj)
        javaFolder = new File(javaPath);
//        applicationClassLoader = new MultiJarFileClassLoader(parentClassLoader); 
        registrars = new Registrar[] { new ImplementationRegistrar() } ;
        
        log.debug("thread context class loader: {}", Thread.currentThread().getContextClassLoader().getClass().getName());
    }

    public ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }
    
    
    
    public ClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

//    public void setApplicationClassLoader(MultiJarFileClassLoader applicationClassLoader) {
    public void setApplicationClassLoader(ClassLoader applicationClassLoader) {
        this.applicationClassLoader = applicationClassLoader;
    }
    
    

    public File getJavaFolder() {
        return javaFolder;
    }

    public void setJavaFolder(File javaFolder) {
        this.javaFolder = javaFolder;
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
        load(getApplicationJars());
        }
        catch(IOException e) {
            log.error("Cannot load jars", e);
            return;
        }
        // TODO:  list all the jar files in feature folders...
//        String featurePath = My.filesystem().getApplicationPath() + File.separator + "features";
//        File featureFolder = new File(featurePath);
        // TODO:  list all the directories in featureFolder,  each directory is a featureId
        // TODO:  for each featureId:
//        My.filesystem().getFeatureFilesystem(featureId).getJavaPath(); 
        // TODO: create a classloader for that feature, using the multijar cl above as the parent
        
        
        // scan mtwilson jars for plugins,  in the main classloader and also in all feature classloaders / java ext dirs
        scan(getApplicationExtensionJars(), getRegistrars());
        
    }
    
    public File[] getApplicationJars() {
        // list all the jar files in the java directory
        FilenameEndsWithFilter jarfilter = new FilenameEndsWithFilter(".jar");
        File[] jars = javaFolder.listFiles(jarfilter);
        return jars;
    }
    
    /**
     * Default behavior is to filter the application jars to select only 
     * jars that have "mtwilson" in the name, for example mtwilson-attestation-ws-v2.jar
     * or othercompany-mtwilson-plugin.jar 
     * 
     * @return 
     */
    public File[] getApplicationExtensionJars() {
        FilenameContainsFilter jarfilter = new FilenameContainsFilter("mtwilson");
        File[] jars = getApplicationJars();
        ArrayList<File> extensionJars = new ArrayList<>();
        for(int i=0; i<jars.length; i++) {
            if( jarfilter.accept(jars[i]) ) {
                extensionJars.add(jars[i]);
            }
        }
        return extensionJars.toArray(new File[extensionJars.size()]);
    }
    
    public void load(File[] jars) throws IOException {
        if( applicationClassLoader == null ) {
            applicationClassLoader = new FileURLClassLoader(jars, parentClassLoader);
        }
        else {
            // TODO:  we can't predict what classloader it would be so does it even
            // make sense to have a load(File[]) ?? 
        }
    }
    
    // TODO :   Extensions find* functions need to record the *results* that
    //  they provide to callers in an in-memory log;  then at application shtudown
    //  we can record the classes that were used during that run (maybe combine
    // them with previous results if those were cached) 
    //  so at startup we can read that log/cache and we can immediately register
    //  all those classes and then schedule a background task to scan the jars
    //  complete with this function.  that will makea pplication startup much
    //  faster (with the assumption that any new plugins wont' be requested 
    //  within the first 5-10 seconds of the app starting up) 
    // jars need to be already filtered to the jars you want to scan 
    public void scan(File[] jars, Registrar[] registrars) {
        long time0 = System.currentTimeMillis();
        CountingIterator<File> it = new CountingIterator<>(new ArrayIterator<>(jars)); // only scans directory for jar files; does NOT scan subdirectories
        for(int i=0; i<registrars.length; i++) { log.debug("Scanning with registrar {}", registrars[i].getClass().getName()); }
        while (it.hasNext()) {
            File jar = it.next();
            log.debug("Scanning {}", jar.getAbsolutePath());
            try {/*
                for(Registrar registrar : registrars) {
                    ExtensionUtil.scan(registrar, new JarClassIterator(jar, applicationClassLoader));// we use our current classloader which means if any classes are already loaded we'll reuse them
                }*/
                ExtensionUtil.scan(new JarClassIterator(jar, applicationClassLoader), registrars);
            }
            catch(Throwable e) { // catch ClassNotFoundException and NoClassDefFoundError 
                log.error("Cannot read jar file {} because {}", jar.getAbsolutePath(), e.getClass().getName() + ": " + e.getMessage());
                //e.printStackTrace();
                // log.error("Cannot read jar file {}", jar.getAbsolutePath());
            }
        }
        long time1 = System.currentTimeMillis();
        log.info("Scanned {} jars in {}ms", it.getValue(), time1-time0);
    }
    
}
