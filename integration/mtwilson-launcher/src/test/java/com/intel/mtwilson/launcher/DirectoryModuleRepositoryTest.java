/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.dcsg.cpg.module.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * The Maven pom.xm for this module specifies a few modules to copy into the "target" folder during the build;
 * we try to load classes from those modules in order to make the test repeatable on different developer machines.
 * 
 * @author jbuhacoff
 */
public class DirectoryModuleRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectoryModuleRepositoryTest.class);

    // XXX TODO   this test needs to be rewritten for new DirectoryLauncher
    /*
    @Test
    public void testListModulesInDirectory() throws IOException {
        String dir = System.getProperty("mtwilson.module.dir", "."+File.separator+"target"+File.separator+"jmod");
        log.debug("mtwilson.module.dir={}", dir);
        File directory = new File(dir);
        if( !directory.exists() ) {
            log.debug("Creating module directory: {}", dir);
            if( !directory.mkdirs() ) {
                log.error("Cannot create module directory: {}", dir);
            }
        }
        DirectoryModuleRepository repository = new DirectoryModuleRepository();
        repository.setDirectory(directory);
        List<Module> modules = repository.listModules();
        log.debug("Found {} modules", modules.size());
        for(Module module : modules) {
            log.debug("Module: {}", module.getImplementationTitle()+"-"+module.getImplementationVersion());
            log.debug("Class-Path: {}", module.getClasspath());
            log.debug("Module-Components: {}", module.getComponentNames());
//            printModuleManifest(module);
        }
    }
    */
    
    private void printCollection(String label, Collection<Object> values) {
        log.debug(label+": "+StringUtils.join("|", values));
    }
    public void printModuleManifest(String jarfilePath) throws IOException {
        FileInputStream in = new FileInputStream(new File(jarfilePath));
        JarInputStream jarStream = new JarInputStream(in);
        Manifest mf = jarStream.getManifest();
        if( mf == null ) {
            log.debug("No manifest in {}", jarfilePath);
            return;
        }
        // the main attributes are what manifest.mf files typically have;
        // the named attributes accessible via getEntries and getAttributes(name) are
        // actually SEPARATE SECTIONS  in the manifest.mf file and not the headers/attributes
        // themselves.  so that shows up empty most of the time.
        Attributes main = mf.getMainAttributes();
        String title = main.getValue(Attributes.Name.IMPLEMENTATION_TITLE); // Implementation-Title
        String version = main.getValue(Attributes.Name.IMPLEMENTATION_VERSION); // Implementation-Version
        String vendor = main.getValue(Attributes.Name.IMPLEMENTATION_VENDOR); // Implementation-Vendor
        String classpath = main.getValue(Attributes.Name.CLASS_PATH); // Class-Path
        String moduleComponents = main.getValue(Module.MODULE_COMPONENTS); // Module-Components
        log.debug("title: {}", title);
        log.debug("version: {}", version);
        log.debug("vendor: {}", vendor);
        log.debug("classpath: {}", classpath);
        log.debug("modules: {}", moduleComponents);
                /*
        log.debug("there is a manifest");
        Map<String,Attributes> map = mf.getEntries();
        log.debug("the manifest has {} entry keys", map.keySet().size());
        for(String key : map.keySet()) {
            Attributes attr  = map.get(key);
            printCollection(key+"-keyset", attr.keySet());
            printCollection(key+"-values", attr.values());
        }
        Attributes classpathAttr = mf.getAttributes(Attributes.Name.CLASS_PATH.toString()); // Class-Path
        Attributes versionAttr = mf.getAttributes(Attributes.Name.IMPLEMENTATION_VERSION.toString()); // Implementation-Version
        Attributes titleAttr = mf.getAttributes(Attributes.Name.IMPLEMENTATION_TITLE.toString()); // Implementation-Title
        Attributes vendorAttr = mf.getAttributes(Attributes.Name.IMPLEMENTATION_VENDOR.toString()); // Implementation-Vendor
//        log.debug("classpath name: {}", Attributes.Name.CLASS_PATH.toString()); // "Class-Path"
        if( titleAttr != null ) {
        String title = titleAttr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
        log.debug("title: {}", title);
        }
        if( versionAttr != null ) {
        String version = versionAttr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        log.debug("version: {}", version);
        }
        if( vendorAttr != null ) {
        String vendor = vendorAttr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
        log.debug("vendor: {}", vendor);
        }
        if( classpathAttr != null ) {
        String classpath = classpathAttr.getValue(Attributes.Name.CLASS_PATH);
        log.debug("classpath: {}", classpath);
        }*/
        jarStream.close();
    }
}
