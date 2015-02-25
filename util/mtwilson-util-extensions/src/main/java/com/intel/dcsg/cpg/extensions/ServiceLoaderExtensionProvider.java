/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 *
 * @author jbuhacoff
 */
public class ServiceLoaderExtensionProvider implements ExtensionProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServiceLoaderExtensionProvider.class);

    private HashMap<String,ServiceLoader<?>> map = new HashMap<>();
    
    @Override
    public void reload() {
        for(ServiceLoader loader : map.values()) {
            loader.reload();
        }
    }

    /**
     * Uses Java's ServiceLoader to find implementations for the given
     * extension interface. The ServiceLoader is cached for subsequent
     * invocations.
     * 
     * @param extension
     * @return 
     */
    @Override
    public Iterator<String> find(Class<?> extension) {
        log.debug("ServiceLoaderExtensionProvider {} find ServiceLoader: {}", this.hashCode(), extension.getName());
        ServiceLoader<?> loader = map.get(extension.getName());
        if( loader == null ) {
            loader = ServiceLoader.load(extension);
            map.put(extension.getName(), loader);
        }
        ClassNameIterator it = new ClassNameIterator(loader.iterator());
        return it;
    }

    @Override
    public <T> T create(Class<T> extension, String name) throws ExtensionNotFoundException {
        // we could look through the output of find(extension) but 
        // since java's service loader only loads class available
        // on the classpath (or from a specific classloader) we can
        // just go straight there
        try {
            Class<?> clazz = Class.forName(name);
            Constructor constructor = ReflectionUtil.getNoArgConstructor(clazz);
            if( constructor == null ) {
                log.debug("Implementation class {} does not have no-arg constructor", clazz.getName());
                throw new ExtensionNotFoundException(name);
            }
            Object instance = constructor.newInstance();
            T castInstance = (T)instance;
            return castInstance;
        }
        catch(ClassNotFoundException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.debug("Cannot instantiate implementation class {}", name, e);
            throw new ExtensionNotFoundException(name, e);
        }
    }

    @Override
    public Iterator<String> findAnnotated(Class<? extends Annotation> annotation) {
        Iterator<String> it = Collections.emptyIterator();
        return it;
    }

    @Override
    public Object createAnnotated(Class<? extends Annotation> annotation, String name) throws ExtensionNotFoundException {
        throw new ExtensionNotFoundException(name);
    }
    
    /**
     * Wraps any iterator and returns the class name of each item instead of
     * the item itself.
     * 
     * The {@code hasNext()}, {@code next()}, and {@code remove()} methods are
     * all delegated to the wrapped iterator, with the {@code next()} method
     * modified to return the class name of the item.
     * 
     */
    public static class ClassNameIterator implements Iterator<String> {
        private Iterator<?> source;
        public ClassNameIterator(Iterator<?> source) {
            this.source = source;
        }
        @Override
        public boolean hasNext() {
            return source.hasNext();
        }

        @Override
        public String next() {
            return source.next().getClass().getName();
        }

        @Override
        public void remove() {
            source.remove();
        }
        
    }
}
