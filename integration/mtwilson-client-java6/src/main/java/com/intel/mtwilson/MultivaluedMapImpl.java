/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public class MultivaluedMapImpl<K,V> extends HashMap<K,List<V>> implements MultivaluedMap<K,V> {

    
    @Override
    public void putSingle(K k, V v) {
        ArrayList<V> list = new ArrayList<V>();
        list.add(v);
        put(k, list);
    }

    @Override
    public void add(K k, V v) {
        List<V> list = get(k);
        if( list == null ) {
            list = new ArrayList<V>();
        }
        list.add(v);
        put(k, list);
    }

    @Override
    public V getFirst(K k) {
        List<V> list = get(k);
        if(list != null) {
            if( list.isEmpty() ) {
                return null;
            }
        }else{
            return null;
        }
        return list.get(0);
    }

    @Override
    public void addAll(K k, V... vs) {
        List<V> list = get(k);
        if( list == null ) {
            list = new ArrayList<V>();
        }
        for(V v : vs) {
            list.add(v);
        }
        put(k, list);
    }

    @Override
    public void addAll(K k, List<V> vs) {
        List<V> list = get(k);
        if( list == null ) {
            list = new ArrayList<V>();
        }
        list.addAll(vs);
        put(k, list);
    }

    @Override
    public void addFirst(K k, V v) {
        List<V> list = get(k);
        if( list == null ) {
            list = new ArrayList<V>();
        }
        list.add(v);
        put(k, list);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> mm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
