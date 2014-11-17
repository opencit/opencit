/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import java.lang.annotation.Annotation;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public interface ExtensionProvider {
    /**
     * If the extension provider has a source that it can check for
     * available extensions, calling this method should cause the
     * provider to check its source again in case the available
     * extensions have been updated.
     */
    void reload();
    
    /**
     * Calling this method should cause the provider to check its
     * source for extensions that implement the given interface.
     * The provider isn't required to load these extensions, only
     * report on which extensions (fully-qualified class names) are
     * available.
     * @param extension
     * @return an iterator for extension class names; never null
     */
    Iterator<String> find(Class<?> extension);
    
    /**
     * Calling this method should cause the provider to check its
     * source for extensions that have the given annotation.
     * Providers that do not support looking up classes by annotation
     * should return an empty iterator. 
     * @param annotation
     * @return an iterator for annotated extensions; never null
     */
    Iterator<String> findAnnotated(Class<? extends Annotation> annotation);
    
    /**
     * Calling this method should cause the provider to load
     * a specific named class implementing the given extension.
     * 
     * @param <T>
     * @param extension interface which is requested; can be any Java interface
     * @param name of the class to create; the class must implement the extension interface
     * @return 
     * @throws ExtensionNotFoundException if the named class is not found or does not implement the given extension interface; should not be the case if the name was obtained from the find method, unless the code was removed between the time it was scanned and the time it was requested
     */
    <T> T create(Class<T> extension, String name) throws ExtensionNotFoundException;
    
    /**
     * Calling this method should cause the provider to load
     * a specific named class having the given annotation.
     * 
     * @param annotation which should be present on the extension
     * @param name of the class to create; the class must have the given annotation
     * @return
     * @throws ExtensionNotFoundException if the named class is not found or does not have the given annotation
     */
    Object createAnnotated(Class<? extends Annotation> annotation, String name) throws ExtensionNotFoundException;
}
