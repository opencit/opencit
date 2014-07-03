/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.repository;

/**
 * Tentative TODO:  add the following methods:
 * 
 * create(C items);
 * store(C items);
 * 
 * which would then possibly take advantage of database-specific features,
 * such as a multi-row insert/update, or it might use multithreading, etc.
 * which might be better than the client having to loop through a collection
 * or do multithreading code at the business layer for something that might become
 * common.
 *
 * @author jbuhacoff
 */
public interface SearchableRepository<T, L extends Locator<T>, C, F extends FilterCriteria<T>> extends Repository<T,L> {
    /**
     * Given criteria encapsulated in a POJO, returns a collection of items
     * matching the criteria.
     *
     * @param criteria
     * @return
     */
    C search(F criteria);
    
    /**
     * Given criteria encapsulated in a POJO, deletes all items in repository
     * matching the criteria.
     * @param criteria 
     */
    void delete(F criteria);
}
