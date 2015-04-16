/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wraps any Configuration instance with a Map so it can be passed
 * to existing functions that accept Map as input and edits will be passed
 * through to the wrapped Configuration instance.
 * 
 * Contrast with toMap() in AbstractConfiguration which creates a copy
 * of the configuration that can be edited but those edits are not passed
 * through to the original instance.
 * 
 * @author jbuhacoff
 */
public class MapDecorator implements Map<String,String> {
    private Configuration configuration;

    public MapDecorator(Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public int size() {
        return configuration.keys().size();
    }

    @Override
    public boolean isEmpty() {
        return configuration.keys().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if( key instanceof String ) {
            return configuration.keys().contains((String)key);
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if( value == null ) { return false; }
        if( value instanceof String ) {
            String str = (String)value;
            for(String key : configuration.keys()) {
                if( str.equals(configuration.get(key, null)) ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String get(Object key) {
        if( key instanceof String ) {
            return configuration.get((String)key, null);
        }
        return null;
    }

    @Override
    public String put(String key, String value) {
        String previous = configuration.get(key, null);
        configuration.set(key, value);
        return previous;
    }

    @Override
    public String remove(Object key) {
        if( key instanceof String ) { 
            String previous = configuration.get((String)key, null);
            configuration.set((String)key, null);
            return previous;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for(Entry<? extends String,? extends String> entry : m.entrySet()) {
            configuration.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for(String key : configuration.keys()) {
            configuration.set(key, null);
        }
    }

    @Override
    public Set<String> keySet() {
        return configuration.keys();
    }

    @Override
    public Collection<String> values() {
        ArrayList<String> values = new ArrayList<>();
        for(String key : configuration.keys()) {
            String value = configuration.get(key, null);
            if( value != null ) {
                values.add(value);
            }
        }
        return values;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        HashSet<Entry<String, String>> values = new HashSet<>();
        for(String key : configuration.keys()) {
            String value = configuration.get(key, null);
            
            if( value != null ) {
                values.add(new MapEntry(key));
            }
        }
        return values;
    }


    public class MapEntry implements Map.Entry<String,String> {
        private String key;

        public MapEntry(String key) {
            this.key = key;
        }
        
        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return configuration.get(key, null);
        }

        @Override
        public String setValue(String value) {
            String existingValue = configuration.get(key, null);
            configuration.set(key, value);
            return existingValue;
        }
        
    }
}
