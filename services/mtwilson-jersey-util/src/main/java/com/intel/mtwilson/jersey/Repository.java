/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey;

import com.intel.dcsg.cpg.io.UUID;
import java.util.Collection;

/**
 *
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
