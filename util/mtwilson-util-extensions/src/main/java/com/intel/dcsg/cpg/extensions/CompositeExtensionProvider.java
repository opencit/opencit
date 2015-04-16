/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import com.intel.mtwilson.collection.ChainedIterator;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Enables combination of ServiceLoaderExtensionProvider and other 
 * implementations of ExtensionProvider with configurable priority order.
 * 
 * @author jbuhacoff
 */
public class CompositeExtensionProvider implements ExtensionProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompositeExtensionProvider.class);
    private ArrayList<ExtensionProvider> providers = new ArrayList<>();
    private HashMap<String,ExtensionProvider> cache = new HashMap<>();
    
    public void addProvider(ExtensionProvider provider) {
        providers.add(provider);
    }

    @Override
    public void reload() {
        for(ExtensionProvider provider : providers) {
            try {
                log.debug("Reloading extension provider: {}", provider.getClass().getName());
                provider.reload();
            }
            catch(Exception e) {
                log.error("Cannot reload extension provider: {}", provider.getClass().getName(), e);
            }
        }
    }

    @Override
    public Iterator<String> find(Class<?> extension) {
        ArrayList<Iterator<String>> iterators = new ArrayList<>();
        for(ExtensionProvider provider : providers) {
            try {
                Iterator<String> it = provider.find(extension);
                iterators.add(it);
            }
            catch(Exception e) {
                log.error("Cannot find extensions at provider: {}", provider.getClass().getName(), e);
            }
        }
        ChainedIterator<String> composite = new ChainedIterator<>(iterators);
        return composite;
    }

    @Override
    public Iterator<String> findAnnotated(Class<? extends Annotation> annotation) {
        ArrayList<Iterator<String>> iterators = new ArrayList<>();
        for(ExtensionProvider provider : providers) {
            try {
                Iterator<String> it = provider.findAnnotated(annotation);
                iterators.add(it);
            }
            catch(Exception e) {
                log.error("Cannot find annotated extensions at provider: {}", provider.getClass().getName(), e);
            }
        }
        ChainedIterator<String> composite = new ChainedIterator<>(iterators);
        return composite;
    }

    @Override
    public <T> T create(Class<T> extension, String name) throws ExtensionNotFoundException {
        ExtensionProvider cachedProvider = cache.get(extension.getName());
        if( cachedProvider == null ) {
            for(ExtensionProvider provider : providers) {
                try {
                    T instance = provider.create(extension, name);
                    if( instance != null ) {
                        cache.put(extension.getName(), provider);
                        return instance;
                    }
                }
                catch(Exception e) {
                    log.debug("Extension {} not found in provider {}", extension.getName(), provider.getClass().getName());
                }
            }
            throw new ExtensionNotFoundException(extension.getName());
        }
        return cachedProvider.create(extension, name);
    }

    @Override
    public Object createAnnotated(Class<? extends Annotation> annotation, String name) throws ExtensionNotFoundException {
        ExtensionProvider cachedProvider = cache.get(annotation.getName());
        if( cachedProvider == null ) {
            for(ExtensionProvider provider : providers) {
                try {
                    Object instance = provider.createAnnotated(annotation, name);
                    if( instance != null ) {
                        cache.put(annotation.getName(), provider);
                        return instance;
                    }
                }
                catch(Exception e) {
                    log.debug("Annotated extension {} not found in provider {}", annotation.getName(), provider.getClass().getName());
                }
            }
            throw new ExtensionNotFoundException(annotation.getName());
        }
        return cachedProvider.create(annotation, name);
    }
    
    
}
