/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions;

import com.intel.dcsg.cpg.extensions.Extensions.Factory;
import com.intel.mtwilson.pipe.AttributeEqualsFilter;
import com.intel.mtwilson.pipe.Filter;
import com.intel.mtwilson.pipe.FilterUtil;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A plugin is an extension that is used in a specific context. Only 
 * one plugin should match a context. The {@code find} methods allow
 * you to provide a filter to choose the appropriate plugin for the
 * context. A context could be user input like the name of a command to
 * run, or the scheme portion of a URL, for example.
 * 
 * @author jbuhacoff
 */
public class Plugins {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Plugins.class);
    
    /**
     * Given a collection which is expected to contain just one item, this 
     * function returns that item, or null if the collection was empty,
     * or throws an exception if the collection contained multiple items.
     * @param <T>
     * @param collection must not be null
     * @return the only item in the collection, null if the collection was empty
     */    
    private static <T> T identity(Collection<T> collection) {
        return identity(collection.iterator());
    }
    
    /**
     * Given an iterator which is expected to return just one item, this 
     * function returns that item, or null if the iterator was empty,
     * or throws an exception if the iterator returned multiple items.
     * @param <T>
     * @param iterator must not be null
     * @return the only item in the iterator, null if the iterator was empty
     */    
    private static <T> T identity(Iterator<T> iterator) {
        if( iterator.hasNext() ) {
            T item = iterator.next(); // identity, only if it's the only one
            if( iterator.hasNext() ) {
                throw new IllegalStateException("Multiple plugins match");
            }
            return item;
        }
        return null; // no items in iterator
    }

    /**
     * Find the right plugin using the given filter. If the filter accepts
     * just one plugin, it will be returned. If the filter does not accept
     * any plugins, then null will be returned. If the filter accepts multiple
     * plugins (indicating there are multiple alternataives available) then
     * an exception is thrown because there should only be one matching plugin.
     * 
     * If the application requires multiple matches, use {@code Extensions.findAll}
     * to get all extensions for that interface instead of this method.
     * 
     * @param <T>
     * @param pluginInterface
     * @param pluginFilter
     * @return 
     */
    public static <T> T find(Class<T> pluginInterface, Filter<T> pluginFilter) {
        // 1. make a list of all extensions for this interface
        List<T> plugins = Extensions.findAll(pluginInterface);
        // 2. filter the list using the given context and default filter
        Collection<T> matches = FilterUtil.filterCollection(plugins, pluginFilter);
        log.debug("Find interface {} with filter {} resulted in {} matches", pluginInterface.getName(), pluginFilter.getClass().getName(), matches.size());
        // default filter can be:  1. annotation-based    @Plugin("ssh") ...   2. interface-based   class Pojo implements WidgetPlugin { STring getWidgetType(); }  3. reflection-based for getter  class Pojo {  C getContext() } ...
        // make a factory??  so maybe find a plugin factory that knows how to 
        // process the context???  that means there can only be ONE plugin factory
        // per interface (a driver). you can replace it but you can only have
        // one active. 
        // so how will we find the factory for this? annotation? interface?
        // or just let the caller specify the factory? that would avoid a lookup
        // and inflexibility... they could do whatever.  question is whether
        // they want to pass just  (interface, context) or (interface, context, factory)
        return identity(matches);
    }

    /**
     * A convenience function to find the right plugin using the value of one of
     * its properties. Calling this function is equivalent to calling
     * {@code find(pluginInterface, new AttributeEqualsFilter(attributeName, attributeValue))}.
     * 
     * @param <T>
     * @param pluginInterface
     * @param attributeName
     * @param attributeValue
     * @return 
     */
    public static <T> T findByAttribute(Class<T> pluginInterface, String attributeName, Object attributeValue) {
        // 1. make a list of all extensions for this interface
        List<T> plugins = Extensions.findAll(pluginInterface);
        // 2. filter the list using the given filter & context
        Collection<T> matches = FilterUtil.filterCollection(plugins, new AttributeEqualsFilter(attributeName, attributeValue));
        log.debug("findByAttribute interface {} with attribute {} resulted in {} matches", pluginInterface.getName(), attributeName, matches.size());
        // make a factory??  so maybe find a plugin factory that knows how to 
        // process the context???  that means there can only be ONE plugin factory
        // per interface (a driver). you can replace it but you can only have
        // one active. 
        // so how will we find the factory for this? annotation? interface?
        // or just let the caller specify the factory? that would avoid a lookup
        // and inflexibility... they could do whatever.  question is whether
        // they want to pass just  (interface, context) or (interface, context, factory)
        return identity(matches);
    }
    
    /**
     * A convenience function to find the right plugin which has a 1-arg
     * constructor based on the given argument (which should match the type
     * of the argument in the constructor).
     * 
     * @param <T>
     * @param pluginInterface
     * @param arg
     * @return 
     */
    public static <T> T findByConstructor(Class<T> pluginInterface, Object arg) {
        Filter<Class<?>> filter = new Extensions.OneArgFilter(arg);
        Factory<T> factory = new Extensions.OneArgFactory(arg);
        List<T> plugins = Extensions.findAll(pluginInterface, filter, factory);
        // there should only be one match for a plugin with the necessary constructor
        return identity(plugins);
    }
}
