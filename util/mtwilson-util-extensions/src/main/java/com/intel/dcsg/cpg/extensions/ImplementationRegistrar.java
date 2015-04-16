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

    public ImplementationRegistrar() {
        this.acceptable = null;
    }

    public ImplementationRegistrar(Class<?>... interfaces) {
        this.acceptable = new ArrayList<>();
        for (Class<?> clazz : interfaces) {
            acceptable.add(clazz.getName());
        }
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
                for (int i = 0; i < interfaces.length; i++) {
                    if (acceptable == null || acceptable.contains(interfaces[i].getName())) {
                        WhiteboardExtensionProvider.register(interfaces[i], clazz);
                        accepted = true;
                    }
                }
        return accepted;
    }

}
