/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey;

import com.intel.dcsg.cpg.io.UUID;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author jbuhacoff
 */
public class HashSetRepository<T extends Document> implements Repository<T> {
    private final HashSet<T> store = new HashSet<T>();

    @Override
    public T findByUuid(UUID uuid) {
        for(T item : store) {
            if( item.getId().equals(uuid) ) {
                return item;
            }
        }
        return null;
    }

    @Override
    public T findByName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<T> findByFilterCriteria(FilterCriteria<T> criteria) {
        ArrayList<T> results = new ArrayList<T>();
        results.addAll(store); /// XXX FOR TESTING ONLY WE JUST RETURN THE ENTIRE DATA SET
        return results;
    }

    @Override
    public void delete(T deleteItem) {
        for(T item : store) {
            if( item.getId().equals(deleteItem.getId()) ) {
                store.remove(item);
                return;
            }
        }
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        for(T item : store) {
            if( item.getId().equals(uuid) ) {
                store.remove(item);
                return;
            }
        }
    }

    @Override
    public void store(T item) {
        store.add(item);
    }

    @Override
    public void store(Collection<T> item) {
        store.addAll(item);
    }

}
