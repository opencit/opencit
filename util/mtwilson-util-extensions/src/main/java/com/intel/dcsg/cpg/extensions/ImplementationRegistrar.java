/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * This class scans the classpath to find possible plugin classes and
 * automatically adds them to the whiteboard
 *
 * A plugin class can be any class that has a no-arg constructor (or a one-arg
 * constructor for context-sensitive plugins) and is not itself an interface or
 * an abstract class, but implements one or more interfaces.
 *
 * Support for registering implementations of specific interfaces or any
 * interface. Support for specifying packages to include and exclude from
 * registration (for example typically exclude java.* and javax.*) Note that
 * includes/excludes operate on specific implementations, not on interfaces so
 * you can request registration of all implementations of java.lang.Runnable
 * within the package com.example.
 *
 * @author jbuhacoff
 */
public class ImplementationRegistrar implements Registrar {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImplementationRegistrar.class);
    private final List<String> acceptable; // when null, will register ANY implementation of ANY interface; when set, will only register specific interfaces
    private List<String> includePackages = null; // when null, will accept implementations in any package; when set, will only accept implementations in specified packages
    private List<String> excludePackages = null; // when null, will accept implementations in any package; when set, will exclude implementations in specified packages (overrides includePackages so can be used to exclude specific portion of included package)

    public ImplementationRegistrar() {
        this.acceptable = null;
    }

    public ImplementationRegistrar(Class<?>... interfaces) {
        this.acceptable = new ArrayList<>();
        for (Class<?> clazz : interfaces) {
            acceptable.add(clazz.getName());
        }
    }

    public void setIncludePackages(List<String> includePackages) {
        this.includePackages = includePackages;
    }

    public void setExcludePackages(List<String> excludePackages) {
        this.excludePackages = excludePackages;
    }

    @Override
    public boolean accept(Class<?> clazz) {
        boolean accepted = false;
        if (ReflectionUtil.isPluginClass(clazz) || ReflectionUtil.isContextPluginClass(clazz)) {
            log.debug("Class might be an extension: {}", clazz.getSimpleName());
            // register under each interface that it implements directly
            Class<?>[] interfaces = clazz.getInterfaces();
            Class<?> abstraction;
            accepted = register(clazz, interfaces);
            // register under each interface that it inherits from parent / that parent implements or under the parent itself if the parent is abstract
            Class<?> parent = clazz.getSuperclass();
            while (parent != null && parent != Object.class && parent != clazz) {
                log.debug("Scanning parent {}", parent.getName());
                interfaces = parent.getInterfaces();
                if (register(clazz, interfaces)) {
                    accepted = true;
                }
                abstraction = Modifier.isAbstract(parent.getModifiers()) ? parent : null;
                if (abstraction != null && register(clazz, abstraction)) {
                    accepted = true;
                }
                parent = parent.getSuperclass();
            }
        } else {
            log.debug("Class is definitely not an extension {}", clazz.getSimpleName());
        }
        return accepted;
    }

    private boolean register(Class<?> clazz, Class<?>... interfaces) {
        boolean accepted = false;
        if (includePackages == null || startsWithAny(clazz.getName(), toPackagePrefixes(includePackages))) {
            if (excludePackages == null || !startsWithAny(clazz.getName(), toPackagePrefixes(excludePackages))) {
                for (int i = 0; i < interfaces.length; i++) {
                    if (acceptable == null || acceptable.contains(interfaces[i].getName())) {
                        WhiteboardExtensionProvider.register(interfaces[i], clazz);
                        accepted = true;
                    }
                }
            }
        }
        return accepted;
    }

    private boolean startsWithAny(String test, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (test.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
    
    // the prefixes are like "java", "javax", "com.intel", etc.
    // we return the same prefixes with a "." at the end so they become
    // "java.", "javax.", "com.intel.", etc. 
    private List<String> toPackagePrefixes(List<String> prefixes) {
        ArrayList<String> packagePrefixes = new ArrayList<>();
        for(String prefix : prefixes) {
            packagePrefixes.add(prefix+".");
        }
        return packagePrefixes;
    }
}
