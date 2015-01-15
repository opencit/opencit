/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author jbuhacoff
 */
public class FilterUtil {
    
    /**
     * Creates a new collection containing only the items from the 
     * input collection that were accepted by the filter.
     * 
     * @param <T>
     * @param collection
     * @param filter
     * @return 
     */
    public static <T> Collection<T> filterCollection(Collection<T> collection, Filter<T> filter) {
        ArrayList<T> result = new ArrayList<>();
        for(T item : collection) {
            if( filter.accept(item) ) {
                result.add(item);
            }
        }
        return result;
    }
}
