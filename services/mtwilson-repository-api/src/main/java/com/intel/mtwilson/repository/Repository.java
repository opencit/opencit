/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.repository;

/**
 *
 * @author jbuhacoff
 */
public interface Repository<T, L extends Locator<T>> {
    
    /**
     * Given an item identifier, returns the specified item if it exists or null
     * if the item doesn't exist.
     * 
     * Subclass is responsible for validating the id in whatever manner it needs to.
     * Most will return null if !UUID.isValid(id)  but we don't do it here because
     * a resource might want to allow using something other than uuid as the url
     * key, for example a Host resource might accept uuid OR hostname as {id}
     * 
     * Implementations should return null if the item is not found. 
     *
     * @param id
     * @return the requested item or null if it is not found
     * @throws RepositoryRetrieveException if an error occurs while trying to locate or retrieve the item
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
     * @throws RepositoryStoreException if an error occurs while trying to store the item
     * @throws RepositoryStoreConflictException if the item to be stored conflicts with an existing item; indicating the application should retrieve the existing item, resolve the conflict, and attempt to store again
     */
    void store(T item);

    /**
     * Creates a new instance and stores it in the DB.
     *
     * The item will already have a client-provided or auto-generated UUID.
     *
     * @param id
     * @param item
     * @throws RepositoryCreateException if an error occurs while trying to create the item
     * @throws RepositoryCreateConflictException if the given item has the same primary key as an existing item
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
     * @throws RepositoryDeleteException if an error occurs while trying to delete the item
     */
    void delete(L locator);
//    void delete(String id);

}
