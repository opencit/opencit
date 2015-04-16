/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a registry of available commands which maps command name to implementation
 * class name. When a command is chosen based on its command name, the 
 * RegistryCommandFinder attempts to load the corresponding class and create
 * a new instance. Always creates new command instances - they are not cached.
 * 
 * For example:
 * <code>
 * Map<String,String> availableCommands = loadCommandRegistry(); // not provided
 * PluginRegistry<Command> finder = new PluginRegistry<Command>(availableCommands);
 * Command toRun = finder.forName("hello-world"); // will search for the registry for an entry with key "hello-world" 
 * </code>
 * 
 * @author jbuhacoff
 */
public class PluginRegistry<T> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Extension is the interface or abstract class type that plugins will implement
     */
    private final Class<T> extension;
    /**
     * Key is command name, like "hello-world"
     * Value is implementation class fully qualified name, like "com.example.HelloWorld"
     */
    private final Map<String,String> registry;
    
    public PluginRegistry(Class<T> extension, Map<String,String> registry) {
        this.extension = extension;
        this.registry = registry;
    }
    
    /**
     * 
     * @param key like "hello-world" or "com.example:hello-world" to look up the plugin
     * @return the plugin instance, or null if there is no registered plugin or the plugin could not be instantiated
     */
    public T lookup(String key) {
        if( registry == null ) {
            throw new IllegalStateException("Plugin registry is not initialized");
        }
        log.debug("Searching for {} plugin: {}", extension.getName(), key);
        String fqcn = registry.get(key);
        if( fqcn == null || fqcn.isEmpty() ) {
            log.debug("No plugin registered for: {}", key);
            return null;
        }
        try {
            Class pluginClass = Class.forName(fqcn); // ClassNotFoundException
            Object pluginObject = pluginClass.newInstance(); // InstantiationException, IllegalAccessException
            T plugin = (T)pluginObject;
            return plugin;
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.debug("Cannot load {} plugin {} from class {}: {}", extension.getName(), key, fqcn, e.toString());
            return null;
        }
    }
    
}
