/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This class scans the classpath to find Factory classes and automatically adds
 * them to the whiteboard; a Factory class is any class whose name ends with "Factory"
 * and is not an interface and has a no-arg constructor.
 *
 * @author jbuhacoff
 */
public class FactoryRegistrar implements Registrar {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FactoryRegistrar.class);

    // scans given set of classes for classes that are named *Factory and automatically registers them
    @Override
    public boolean accept(Class<?> clazz) {
        boolean accepted = false;
        if (isFactoryClass(clazz)) {
            // register under each interface that it implements directly
            Class<?>[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                WhiteboardExtensionProvider.register(interfaces[i], clazz);
                accepted = true;
            }
            // register under each interface that it inherits from parent / that parent implements
            Class<?> parent = clazz.getSuperclass();
            while (parent != null && parent != Object.class && parent != clazz) {
                log.debug("Scanning parent {}", parent.getName());
                interfaces = parent.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    WhiteboardExtensionProvider.register(interfaces[i], clazz);
                    accepted = true;
                }
                parent = parent.getSuperclass();
            }
        }
        return accepted;
    }

    // like in ReflectionUtil in cpg-module
    public static boolean hasNoArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFactoryClass(Class<?> clazz) {
//        boolean annotated = method.isAnnotationPresent(Plugin.class);
        boolean notInterface = !clazz.isInterface();
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean conventional = clazz.getName().endsWith("Factory");
        boolean noArgs = hasNoArgConstructor(clazz);
        return notInterface && notAbstract && (/*annotated ||*/conventional) && noArgs;
    }
}
