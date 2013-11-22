/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * XXX TODO  presence of jmod-components no longer indicates a module ...   need to update documentation
 * 
 * @author jbuhacoff
 */
public class ModuleUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleUtil.class);
    
    public static Manifest readManifest(File jar) throws IOException {
        FileInputStream in = new FileInputStream(jar); // throws FileNotFoundException
        JarInputStream jarStream = new JarInputStream(in); // throws IOException
        Manifest mf = jarStream.getManifest();
        jarStream.close();
        return mf;
    }
    
    public static boolean isModule(File jar) throws IOException {
        Manifest mf = readManifest(jar);
        return isModule(jar, mf);
    }
    
    public static boolean isModule(File jar, Manifest mf) {
        if( mf == null ) {
            log.debug("No manifest in {}", jar.getName());
            return false;
        }
        Attributes main = mf.getMainAttributes();
        String jmodComponents = main.getValue(Module.JMOD_COMPONENTS);
        if( jmodComponents == null ) {
            log.warn("No Jmod-Components in {}", jar.getName());
        }
        String title = main.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        if( title == null || title.isEmpty() ) {
            log.warn("No Implementation-Title in {}", jar.getName());
        }
        String version = main.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if( version == null || version.isEmpty() ) {
            log.warn("No Implementation-Version in {}", jar.getName());
        }
        String vendor = main.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        if( vendor == null || vendor.isEmpty() ) {
            log.warn("No Implementation-Vendor in {}", jar.getName());
        }
        String classpath = main.getValue(Attributes.Name.CLASS_PATH);
        if( classpath == null || classpath.isEmpty() ) {
            log.warn("No Class-Path in {}", jar.getName());
        }
        String mavenClasspath = main.getValue("Maven-Classpath");
        if( mavenClasspath == null || mavenClasspath.isEmpty() ) {
            log.warn("No Maven-Classpath in {} (useful for junit testing)", jar.getName());
        }
        String mavenGroupId = main.getValue("Maven-GroupId");
        if( mavenGroupId == null || mavenGroupId.isEmpty() ) {
            log.warn("No Maven-GroupId in {} (useful for junit testing)", jar.getName());
        }
        String mavenArtifactId = main.getValue("Maven-ArtifactId");
        if( mavenArtifactId == null || mavenArtifactId.isEmpty() ) {
            log.warn("No Maven-ArtifactId in {} (useful for junit testing)", jar.getName());
        }
        String mavenVersion = main.getValue("Maven-Version");
        if( mavenVersion == null || mavenVersion.isEmpty() ) {
            log.warn("No Maven-Version in {} (useful for junit testing)", jar.getName());
        }
        // XXX TODO  should use JarComponentIterator with with the manifest to find components ...  and if there are any components create the module and return it, otherwise return null;  that way if it's a module the caller doesn't have to repeat the work to get the components out of it.
        return title != null && !title.isEmpty() && version != null && !version.isEmpty() && vendor != null && !vendor.isEmpty() && classpath != null; // XXX TODO this is probably not enough to distinguish a module jar from some other jar ... maybe require "class-path" to be present even if it's empty ? 
    }
    
    public String[] getComponentSearch(Manifest manifest) { 
        String searchMethods = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VENDOR); 
        if( searchMethods == null ) { return new String[] { "manifest", "annotation", "convention" }; }
        return searchMethods.split(" ");        
    }
    
    
}
