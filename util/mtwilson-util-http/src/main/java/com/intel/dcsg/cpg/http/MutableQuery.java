/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class MutableQuery extends Query {
    
    public MutableQuery() {
        super();
    }
    /*
    public MutableQuery(LinkedHashMap<String,ArrayList<String>> map) {
        this.map = map;
    }*/
    
    public MutableQuery(Map<String,String[]> map) {
        super(map);
    }
    
    public void clear() {
        map.clear();
    }
    
    public void add(String key, String value) {
        ArrayList<String> existingValues = map.get(key);
        if( existingValues == null ) {
            existingValues = new ArrayList<String>();
            map.put(key, existingValues);
        }
        existingValues.add(value);
    }
    
    public void add(String key, Collection<String> values) {
        ArrayList<String> existingValues = map.get(key);
        if( existingValues == null ) {
            existingValues = new ArrayList<String>();
            map.put(key, existingValues);
        }
        existingValues.addAll(values);
    }
    
    public void remove(String key, String value) {
        ArrayList<String> values = map.get(key);
        if( values != null ) {
            while(values.contains(value)) {
                values.remove(value);
            }
        }
    }
    
    public void removeAll(String key) {
        map.remove(key);
    }
}
