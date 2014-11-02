/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 *
 * @author jbuhacoff
 */
public class ReflectionUtil {
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
    public static boolean hasOneArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 1) {
                return true;
            }
        }
        return false;
    }
    
    public static Constructor getNoArgConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return constructor;
            }
        }
        return null;
    }
    
    public static Constructor getOneArgConstructor(Class<?> clazz, Class<?> argument) {
        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 1) {
                Class<?>[] parameters = constructor.getParameterTypes();
                if( parameters[0].isAssignableFrom(argument) ) {
                    return constructor;
                }
            }
        }
        return null;
    }

    public static boolean isPluginClass(Class<?> clazz) {
//        boolean annotated = method.isAnnotationPresent(Plugin.class);
        boolean notInterface = !clazz.isInterface();
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean noArgs = hasNoArgConstructor(clazz);
        return notInterface && notAbstract /* && annotated */ && noArgs;
    }
    public static boolean isContextPluginClass(Class<?> clazz) {
//        boolean annotated = method.isAnnotationPresent(Plugin.class);
        boolean notInterface = !clazz.isInterface();
        boolean notAbstract = !Modifier.isAbstract(clazz.getModifiers());
        boolean oneArg = hasOneArgConstructor(clazz);
        return notInterface && notAbstract /* && annotated */ && oneArg;
    }

    public static boolean isAnnotatedClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        boolean notInterface = !clazz.isInterface();
        boolean annotated = clazz.isAnnotationPresent(annotation);
        boolean noArgs = hasNoArgConstructor(clazz);
        return notInterface && annotated && noArgs;
    }
    
}
