/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class Query {
    protected LinkedHashMap<String,ArrayList<String>> map;
    
    public Query() {
        map = new LinkedHashMap<String,ArrayList<String>>();
    }
    
    public Query(Map<String,String[]> map) {
        this();
        for(String key : map.keySet()) {
            ArrayList<String> copy = new ArrayList<String>();
            copy.addAll(Arrays.asList(map.get(key)));
            this.map.put(key, copy);
        }
    }
    
    public Set<String> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }
    
    public List<String> getAll(String name) {
        ArrayList<String> values = map.get(name);
        if( values == null ) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(values);
    }
    
    public String getFirst(String name) {
        List<String> values = getAll(name);
        if( values.isEmpty() ) {
            return null;
        }
        return values.get(0);
    }
    
    @Override
    public String toString() {
        ArrayList<String> pairs = new ArrayList<String>();
        for(String key : map.keySet()) {
            List<String> values = map.get(key);
            if( values == null || values.isEmpty() ) { continue; }
            for(String value : values) {
                pairs.add(String.format("%s=%s", escape(key), escape(value)));
            }
        }
        return StringUtils.join(pairs, "&");
    }
    
    protected String escape(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e); // java runtime guarantees availability of utf-8 so this will never happen
        }
    }
}
