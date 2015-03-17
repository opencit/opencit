/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A wrapper around Map which implements Copyable and provides 
 * way to exclude specific keys from being added.
 * 
 * When serializing with Jackson, the Attributes object appears as the
 * map directly. 
 * 
 * @author jbuhacoff
 */
public class Attributes implements Copyable {

    /**
     * Stores the attribute name-value pairs, enforcing unique attribute
     * names via the map structure
     */
//    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
    protected final Map<String, Object> attributes = new HashMap<>();

    /**
     * Stores the list of attribute names to exclude from the map
     */
    protected final HashSet<String> exclude = new HashSet<>();
    
    /**
     * 
     * @return unmodifiable map of attributes
     */
    @JsonAnyGetter
    public Map<String, Object> map() {
        return Collections.unmodifiableMap(attributes);
    }

    @JsonAnySetter
    public void set(String key, Object value) {
        if( exclude.contains(key) ) {
            throw new IllegalArgumentException(key);
        }
        attributes.put(key, value);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public void remove(String key) {
        attributes.remove(key);
    }
    
    /**
     * Adds the specified keys to the exclusion list which prevents it from
     * being added to the attributes; also removes the keys from existing
     * attributes
     * 
     * @param keys to exclude from extra attributes 
     */
    public void exclude(String... keys) {
        for(String key : keys) {
            exclude.add(key);
            attributes.remove(key);
        }
    }
    
    /**
     * Adds the specified keys to the exclusion list which prevents it from
     * being added to the attributes; also removes the keys from existing
     * attributes
     * 
     * @param keys to exclude from extra attributes 
     */
    public void exclude(Collection<String> keys) {
        for(String key : keys) {
            exclude.add(key);
            attributes.remove(key);
        }
    }
    
    /**
     * 
     * @param key
     * @return true if the {@code set} method will throw an IllegalArgumentException when trying to set the specified key
     */
    public boolean isExcluded(String key) {
        return exclude.contains(key);
    }
    
    @Override
    public Attributes copy() {
        Attributes newInstance = new Attributes();
        newInstance.copyFrom(this);
        return newInstance;
    }

    public void copyFrom(Attributes source) {
        for (String key : attributes.keySet()) {
            Object value = source.attributes.get(key);
            if (value instanceof Copyable) {
                Object copy = ((Copyable)value).copy();
                this.attributes.put(key, copy);
            } else {
                // since most objects don't implement Copyable there's still
                // a big chance here for havng a shallow copy of a list or
                // map. might be helpful to rely on a tool like xstream to
                // copy by serializing then deserializing into a new instance.
                this.attributes.put(key, value);
            }
        }
    }
}
