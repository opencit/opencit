/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Functions in this class should be removed or refactored, they 
 * are here only temporarily
 * 
 * @author jbuhacoff
 */
public class ExtensionUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtensionUtil.class);
    
    public static void scan(Registrar registrar, Class<?>... clazzes) {
        Scanner scanner = new Scanner(registrar);
        scanner.scan(clazzes);
    }

    public static void scan(Registrar registrar, Collection<Class<?>> clazzes) {
        Scanner scanner = new Scanner(registrar);
        scanner.scan(clazzes);
    }
    
    public static void scan(Registrar registrar, Iterator<Class<?>> clazzes) {
        Scanner scanner = new Scanner(registrar);
        scanner.scan(clazzes);
    }

    public static void scan(Collection<Class<?>> clazzes, Registrar... registrars) {
        Scanner scanner = new Scanner(registrars);
        scanner.scan(clazzes);
    }
    
    public static void scan(Iterator<Class<?>> clazzes, Registrar... registrars) {
        Scanner scanner = new Scanner(registrars);
        scanner.scan(clazzes);
    }
    
} 
