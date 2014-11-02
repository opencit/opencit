/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.rfc822;

import com.intel.mtwilson.collection.MultivaluedHashMap;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class Headers {
    private MultivaluedHashMap<String,String> headers;
    public Headers() {
        this.headers = new MultivaluedHashMap<String,String>();
    }
    public Headers(MultivaluedHashMap<String,String> headers) {
        this.headers = new MultivaluedHashMap<String,String>();
        for(String key : headers.keySet()) {
            this.headers.addAll(key, headers.getAll(key));
        }
    }
    public Headers(Map<String,String> headers) {
        this.headers = new MultivaluedHashMap<String,String>();
        for(String key : headers.keySet()) {
            this.headers.put(key, headers.get(key));
        }
    }
    /**
     * 
     * @return a list of all the header names; you can then use each one of these names with getFirst, getAll, removeAll, etc.
     */
    public Collection<String> names() {
        return headers.keySet();
    }
    /**
     * 
     * @param name
     * @return the first header matching the given name; null if none found
     */
    public String getFirst(String name) {
        return headers.get(name);
    }
    /**
     * 
     * @param name
     * @return all header values for the given header name; null if no headers with this name; never an empty collection
     */
    public Collection<String> getAll(String name) {
        return headers.getAll(name);
    }
    /**
     * Add a header with the given name and value
     * @param name
     * @param value 
     */
    public void add(String name, String value) {
        headers.add(name, value);
    }
    /**
     * Removes all headers matching the given name
     * @param name 
     */
    public void removeAll(String name) {
        headers.remove(name);
    }
    /**
     * Removes all headers matching the given name and value; other
     * headers with the given name but different value will remain
     * @param name
     * @param value 
     */
    public void remove(String name, String value) {
        headers.removeAll(name, value);
    }
}
