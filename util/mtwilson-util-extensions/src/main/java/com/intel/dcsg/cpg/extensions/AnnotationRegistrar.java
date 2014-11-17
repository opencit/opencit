/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;

/**
 * This class scans the classpath to find possible plugin classes and automatically adds
 * them to the whiteboard
 * 
 * A plugin class can be anything that has a no-arg constructor and is not 
 * itself an interface and, in this case,  has a specific annotation that is
 * being sought out. Implementations will be registered under the annotation
 * class name as the interface/registry-key.
 *
 * @author jbuhacoff
 */
public class AnnotationRegistrar implements Registrar {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnnotationRegistrar.class);

    private Class<? extends Annotation> annotation;
    private String annotationName;
    public AnnotationRegistrar(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
        this.annotationName = annotation.getName();
    }
    
    // scans given set of classes for classes that are named *Factory and automatically registers them
    @Override
    public boolean accept(Class<?> clazz) {
        boolean accepted = false;
        if (ReflectionUtil.isAnnotatedClass(clazz, annotation)) {
            log.debug("Found extension {} with annotation {}", clazz.getSimpleName(), annotationName);
            // register under the specified annotation
                WhiteboardExtensionProvider.registerAnnotated(annotation, clazz);
                accepted = true;
        }
        else {
            // if it's not annotated directly check its parents
            Class<?> parent = clazz.getSuperclass();
            if( parent.getName().startsWith("java.") || parent.getName().startsWith("javax.") ) { log.debug("Skipping java. or javax. parent"); return false; }
            log.debug("Scanning parent2 {}", parent.getName());
            while (!accepted && parent != null && parent != Object.class && parent != clazz) {
//                log.debug("Scanning parent {}", parent.getName());
                if( ReflectionUtil.isAnnotatedClass(parent, annotation)) {
                     log.debug("Found extension {} with annotation {} in parent {}", clazz.getSimpleName(), annotationName, parent.getClass().getName());
                    WhiteboardExtensionProvider.registerAnnotated(annotation, clazz);
                    accepted = true;
                }
                parent = parent.getSuperclass();
            }
        }
        return accepted;
    }

}
