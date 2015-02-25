/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import com.intel.mtwilson.pipe.Transformer;
import com.intel.mtwilson.text.transform.CamelCaseToHyphenated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses the extensions mechanism to locate available implementations for
 * an interface and populate
 * a Map that has user-input keys and fully-qualified class name values.  
 * 
 * For example, a command implementation com.example.HelloWorld is registered as both
 * "hello-world" and "com.example:hello-world"
 * 
 * @author jbuhacoff
 */
public class PluginRegistryFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PluginRegistryFactory.class);
    
    /**
     * 
     * @param <T> the extension's type (used internally)
     * @param extension the interface class or abstract class that plugins will implement
     * @return map of user-input keys like "hello-world" or "com.example:hello-world" to class names like "com.example.HelloWorld"
     */
    private static <T> Map<String,String> createRegistryMap(Class<T> extension, Transformer<String> keyTransformer) {
        HashMap<String,String> map = new HashMap<>();
        List<T> commands = Extensions.findAll(extension);
        for( T command : commands ) {
            String fqcn = command.getClass().getName();
            // separate into class name and package name
            String className = command.getClass().getSimpleName();
            String packageName = command.getClass().getPackage().getName();
            // put each command in the map twice - first with just the 
            // class name, second with with package name prefixed
            String transformedName = keyTransformer.transform(className);
            
            // if the name was already registered by an earlier extension
            // in the list, we warn the user and override it
            if( map.containsKey(transformedName) && !fqcn.equals(map.get(transformedName)) ) {
                log.warn("Registry already contains a key {} to class {}, replacing with {}", transformedName, map.get(transformedName), fqcn);
            }
            map.put(transformedName, fqcn);
            
            String qualifiedName = String.format("%s:%s", packageName, transformedName);
            if( map.containsKey(qualifiedName) && !fqcn.equals(map.get(qualifiedName))) {
                log.warn("Registry already contains a key {} to class {}, replacing with {}", qualifiedName, map.get(qualifiedName), fqcn);
            }
            map.put(qualifiedName, fqcn);
            
            log.debug("Registered command {} to class {}", transformedName, fqcn);
        }
        return map;
    }
    
    /**
     * 
     * @param <T> the extension's type
     * @param extension the interface class or abstract class that plugins will implement
     * @return a new PluginRegistry instance that can retrieve plugin instances using a user-input key like "hello-world" or "com.example:hello-world"
     */
    public static <T> PluginRegistry<T> createRegistry(Class<T> extension) {
        Map<String,String> map = createRegistryMap(extension, new CamelCaseToHyphenated());
        PluginRegistry<T> registry = new PluginRegistry<>(extension, map);
        return registry;
    }

    public static <T> PluginRegistry<T> createRegistry(Class<T> extension, Transformer<String> keyTransformer) {
        Map<String,String> map = createRegistryMap(extension, keyTransformer);
        PluginRegistry<T> registry = new PluginRegistry<>(extension, map);
        return registry;
    }
    
}
