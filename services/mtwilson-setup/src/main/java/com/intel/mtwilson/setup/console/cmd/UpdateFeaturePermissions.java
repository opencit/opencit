/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.console.cmd;

import com.intel.dcsg.cpg.classpath.FileURLClassLoader;
import com.intel.dcsg.cpg.classpath.JarClassIterator;
import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.extensions.Registrar;
import com.intel.dcsg.cpg.extensions.Scanner;
import com.intel.dcsg.cpg.io.file.FilenameEndsWithFilter;
import com.intel.dcsg.cpg.performance.CountingIterator;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.collection.ArrayIterator;
import com.intel.mtwilson.My;
import com.intel.mtwilson.feature.model.FeaturePermission;
import com.intel.mtwilson.shiro.PermissionInfo;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * NOTE: this should probably be rewritten as part of "add feature" and
 *       command when more of the plugin architecture is
 *       implemented
 * 
 * Example:
 * <pre>
 * mtwilson setup update-feature-permissions
 * </pre>
 * 
 * @author jbuhacoff
 */
public class UpdateFeaturePermissions implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UpdateFeaturePermissions.class);
    private Configuration options;
    private ClassLoader classLoader;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        // scan all jars in the java classpath and create an entry for each one
        // feature_id, feature_name, permit_domain, permit_action, permit_selection
        // for now we will use a feature_id that is all zero's since we don't have 
        // that mechanism in place yet, and feature_name will be "mtwilson-2.0"
        // (in the future feature_name may be separated from feature_version)
        String javaPath = Folders.application()+File.separator+"java"; //My.filesystem().getBootstrapFilesystem().getJavaPath();
        log.debug("java path: {}", javaPath);
        File javaFolder = new File(javaPath);
        FilenameEndsWithFilter jarfilter = new FilenameEndsWithFilter(".jar");
        File[] jars = javaFolder.listFiles(jarfilter);
        classLoader = new FileURLClassLoader(jars);
        // scan all the jars
        PermissionRegistrar registrar = new PermissionRegistrar();
        scan(jars, registrar);
        // now display what we found
        for(FeaturePermission featurePermission : registrar.getFeaturePermissions()) {
            log.debug("permission: {}", featurePermission);
        }
    }
    
    
    public static interface FileAware {
        void setFile(File file);
    }
    // currently checks only classes with methods directyl annotated, not superclasses
    public static class PermissionRegistrar implements Registrar,FileAware {
        private ArrayList<FeaturePermission> featurePermissions = new ArrayList<>();
        private File file;

        @Override
        public void setFile(File file) {
            this.file = file;
        }

        public ArrayList<FeaturePermission> getFeaturePermissions() {
            return featurePermissions;
        }
        
        
        
        @Override
        public boolean accept(Class<?> clazz) {
            try {
                boolean foundPermission = false;
                Method[] methods = clazz.getDeclaredMethods();
                for(Method method : methods) {
                    if( method.isAnnotationPresent(RequiresPermissions.class)) {
                        RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
                        String[] permissions = annotation.value();   // note that there is also the logical() parameter which is optional, but because we are collecting all known permissions and not really concerned with what is needed for any specific method, it doesn't matter whether the logical is AND/OR
                        for(String permission : permissions) {
                            PermissionInfo permissionInfo = PermissionInfo.parse(permission);
                            FeaturePermission featurePermission = new FeaturePermission();
                            featurePermission.permitDomain = permissionInfo.getDomain();
                            featurePermission.permitAction = permissionInfo.getAction();
                            featurePermission.permitSelection = permissionInfo.getSelection();
                            featurePermission.comment = String.format("File: %s\nClass: %s", file.getName(), clazz.getName());
                            featurePermissions.add(featurePermission);
                            foundPermission = true;
                        }
                    }
                }
                return foundPermission;
            }
            catch(Exception e) {
                log.error("Failed to scan class {}", clazz.getName(), e);
                return false;
            }
        }
        
    }
    
    // code very similar to ExtensionDirectoryLauncher ... should refactor it to a utility class
    public void scan(File[] jars, Registrar registrar) {
        long time0 = System.currentTimeMillis();
        CountingIterator<File> it = new CountingIterator<>(new ArrayIterator<>(jars)); // only scans directory for jar files; does NOT scan subdirectories
        log.debug("Scanning with registrar {}", registrar.getClass().getName());
        while (it.hasNext()) {
            File jar = it.next();
            log.debug("Scanning {}", jar.getAbsolutePath());
            try {
                if( registrar instanceof FileAware ) {
                    ((FileAware)registrar).setFile(jar);
                }
                /*
                for(Registrar registrar : registrars) {
                    ExtensionUtil.scan(registrar, new JarClassIterator(jar, applicationClassLoader));// we use our current classloader which means if any classes are already loaded we'll reuse them
                }*/
//                ExtensionUtil.scan(new JarClassIterator(jar, applicationClassLoader), registrars);
                Scanner scanner = new Scanner(registrar);
                scanner.setThrowExceptions(false);
                scanner.setThrowErrors(false);
                scanner.scan(new JarClassIterator(jar, classLoader));
            }
            catch(Throwable e) { // catch ClassNotFoundException and NoClassDefFoundError 
                log.error("Cannot read jar file {} because {}", jar.getAbsolutePath(), e.getClass().getName() + ": " + e.getMessage());
                log.debug("Caught throwable", e);
                //e.printStackTrace();
                // log.error("Cannot read jar file {}", jar.getAbsolutePath());
            }
        }
        long time1 = System.currentTimeMillis();
        log.info("Scanned {} jars in {}ms", it.getValue(), time1-time0);
    }
    
}
