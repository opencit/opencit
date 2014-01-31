/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey;

import com.intel.dcsg.cpg.io.UUID;
import java.util.Collection;

/**
 * XXX TODO   compare this Repository  to SimpleRepository in the resource 
 * package and pick one or combine them;  since all the v2 resources implemented
 * so far are written like the SimpleRepository maybe we can just extend it
 * to have some of the methods here? if they are useful.  
 * @author jbuhacoff
 */
public interface Repository<T> {
    T findByUuid(UUID uuid);
    T findByName(String name);
    Collection<T> findByFilterCriteria(FilterCriteria<T> criteria);
    void delete(T item);
    void deleteByUuid(UUID uuid);
    void store(T item);
    void store(Collection<T> item);
}
