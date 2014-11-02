/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * 
 * @author jbuhacoff
 */
public class Module {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Module.class);

    public static final String MODULE_COMPONENTS = "Module-Components";
    public static final String MODULE_COMPONENT_SEARCH = "Module-Component-Search";
    
    private File jarFile;
    private Manifest manifest;
    private ClassLoader classLoader;
    private final HashSet<ComponentHolder> componentHolders = new HashSet<ComponentHolder>();
    
    public Module(File jarFile, Manifest manifest, ClassLoader classLoader) {
        this.jarFile = jarFile;
        this.manifest = manifest;
        this.classLoader = classLoader;
    }
    
    public File getJarFile() { return jarFile; }
    public String getImplementationTitle() { return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE); }
    public String getImplementationVersion() { return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION); }
    public String getImplementationVendor() { return manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VENDOR); }
    public String[] getComponentSearch() { 
        String searchMethods = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VENDOR); 
        if( searchMethods == null ) { return new String[] { "manifest", "annotation", "convention" }; }
        return searchMethods.split(" ");        
    }

    public Manifest getManifest() {
        return manifest;
    }

    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * 
     * @return true if the module has at least one active component
     */
    public boolean isActive() {
        boolean active = false;
        for( ComponentHolder componentHolder : componentHolders ) {
            if( componentHolder.isActive() ) {
                active = true;
            }
        }
        return active;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public HashSet<ComponentHolder> getComponentHolders() {
        return componentHolders;
    }
    
    public String[] getClasspath() {
        // find all components
        String artifacts = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        String[] artifactArray = artifacts.split(" ");
        return artifactArray;
    }    

    public String[] getComponentNames() {
        // find all components ... XXX TODO  we should use the JarComponentFinder here
        String componentNames = manifest.getMainAttributes().getValue(MODULE_COMPONENTS);
        String[] componentArray = componentNames.split(" ");
        return componentArray;
    }
    
    public void loadComponents() throws IOException {
        String[] searchMethods = getComponentSearch();
        if( searchMethods.length == 1 && "none".equals(searchMethods[0]) ) { return; }
        ComponentSearchConfiguration search = new ComponentSearchConfiguration();
        for(String searchMethod : searchMethods) {
            if( "manifest".equals(searchMethod) ) { search.withNames(getComponentNames()); }
            if( "annotation".equals(searchMethod) ) { search.withAnnotations(); }
            if( "convention".equals(searchMethod) ) { search.withConventions(); }
        }
        JarComponentIterator it = new JarComponentIterator(jarFile, classLoader, search);
        while(it.hasNext()) {
            Class<?> componentClass = it.next();
            log.debug("Loaded component class: {}", componentClass.getName());
            // instantiate
            try {
                Object component = componentClass.newInstance(); // throws InstantiationException, IllegalAccessException
                log.debug("Instantiated component: {}", component.getClass().getName());
                ComponentHolder componentHolder = new ComponentHolder(component, this);
                componentHolder.setActivateMethod(ReflectionUtil.getActivateMethod(componentClass));
                componentHolder.setDeactivateMethod(ReflectionUtil.getDeactivateMethod(componentClass));
                componentHolder.setNoticeTypes(ReflectionUtil.getNoticeTypes(componentClass));
                componentHolder.setConnectTypes(ReflectionUtil.getConnectTypes(componentClass));
                componentHolders.add(componentHolder);
            }
            catch(Exception e) {
                log.error("Cannot instantiate component: {}", e);
            }
        }
        
    }
}
