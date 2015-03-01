/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

import java.util.Arrays;
import java.util.List;

/**
 * A filter that encapsulates one or more other filters in sequence,
 * accepting the input only if all filters in the sequence accepted the input.
 * 
 * If the list of filters is empty, then all inputs will be accepted 
 * (because there are no filters).
 * 
 * If one filter in the pipe does not accept the input, the subsequent
 * filters in that pipe are not called (fail early).
 * 
 * @author jbuhacoff
 */
public class FilterPipe<T> implements Filter<T> {
    private List<Filter<T>> filters;

    public FilterPipe(List<Filter<T>> filters) {
        this.filters = filters;
    }
    
    public FilterPipe(Filter<T>... filters) {
        this(Arrays.asList(filters));
    }

    @Override
    public boolean accept(T input) {
        boolean result = true;
        for(Filter<T> filter : filters) {
            result = result && filter.accept(input);
            if( !result ) { break; }
        }
        return result;
    }

    public List<Filter<T>> getFilters() {
        return filters;
    }

    
}
