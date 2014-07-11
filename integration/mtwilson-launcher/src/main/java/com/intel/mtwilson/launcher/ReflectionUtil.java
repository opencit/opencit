/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.launcher;

import com.intel.mtwilson.launcher.ext.annotations.Configure;
import com.intel.mtwilson.launcher.ext.annotations.Initialize;
import com.intel.mtwilson.launcher.ext.annotations.Validate;
import com.intel.mtwilson.launcher.ext.annotations.Startup;
import com.intel.mtwilson.launcher.ext.annotations.Shutdown;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author jbuhacoff
 */
public class ReflectionUtil {

    public static boolean isLauncherMethod(Method method, Class<? extends Annotation> lifecyclePhaseAnnotation) {
        boolean annotated = method.isAnnotationPresent(lifecyclePhaseAnnotation); // for example: Initialize.class, Configure.class, Validate.class, Startup.class, Shutdown.class
        boolean conventional = method.getName().equals(lifecyclePhaseAnnotation.getSimpleName().toLowerCase()); // for example: "initialize", "configure", "validate", "startup", "shutdown"
        boolean noArgs = method.getParameterTypes().length == 0;
        boolean noReturn = method.getReturnType().getName().equals("void");
        return (annotated || conventional) && noArgs && noReturn;
    }
    
    public static Method getLauncherMethod(Class<?> clazz, Class<? extends Annotation> lifecyclePhaseAnnotation) {
        Method[] methods =  clazz.getDeclaredMethods();                
        for(Method method : methods) {
            if( isLauncherMethod(method, lifecyclePhaseAnnotation) ) {
                return method;
            }
        }
        return null;
    }
    
}
