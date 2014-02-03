/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.resource;

import com.intel.mtwilson.jersey.Document;
import com.intel.mtwilson.jersey.DocumentCollection;
import com.intel.mtwilson.jersey.FilterCriteria;
import com.intel.mtwilson.jersey.Locator;

/**
 *
 * @author jbuhacoff
 */
public interface SimpleRepository<T extends Document, C extends DocumentCollection<T>, F extends FilterCriteria<T>, L extends Locator<T>> {
    /**
     * Given criteria encapsulated in a POJO, returns a collection of items
     * matching the criteria.
     *
     * @param criteria
     * @return
     */
    C search(F criteria);

    /**
     * Given an item identifier, returns the specified item if it exists or null
     * if the item doesn't exist.
     * 
     * Subclass is responsible for validating the id in whatever manner it needs to.
     * Most will return null if !UUID.isValid(id)  but we don't do it here because
     * a resource might want to allow using something other than uuid as the url
     * key, for example a Host resource might accept uuid OR hostname as {id}
     *
     * @param id
     * @return
     */
    T retrieve(L locator);

    /**
     * Given an item instance, stores the item into permanent storage replacing
     * any existing item with the same id.
     *
     * Pre-Condition: Item should already exist.
     *
     * The item will already have a client-provided or auto-generated UUID.
     *
     * @param id
     * @param item
     */
    void store(T item);

    /**
     * Creates a new instance and stores it in the DB.
     *
     * The item will already have a client-provided or auto-generated UUID.
     *
     * @param id
     * @param item
     */
    void create(T item);

    /**
     * Given an item identifier, delete the corresponding item from permanent
     * storage.
     *
     * If the item does not already exist then no action should be taken - it is
     * not an error to request deletion of an non-existent item.
     *
     * @param id
     */
    void delete(L locator);
//    void delete(String id);

    /**
     * 
     * @param criteria 
     */
    void delete(F criteria);

}
