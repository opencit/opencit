/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.objectpool;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * TODO: add an automatic-return-on-close option using cglib so if the programmer is creating
 * a pool of objects that implement Closeable or AutoCloseable and enables
 * the automatic-return-on-close option, the pool will maintain dynamically
 * generated wrappers for the pooled objects which provide an extra setObjectPool(...)
 * setter and override the close() method so instead of closing the wrapped
 * object they would return themselves to the pool and keep the wrapped object
 * open. 
 * 
 * The TODO item is an alternative to having a PooledObject interface.
 * 
 * @author jbuhacoff
 */
public abstract class AbstractObjectPool<T> implements ObjectPool<T> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractObjectPool.class);
    protected final ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<>();
    protected final Set<T> borrowed = Collections.newSetFromMap(new ConcurrentHashMap<T,Boolean>());
    protected final Set<T> revoked = Collections.newSetFromMap(new ConcurrentHashMap<T,Boolean>());
    
    @Override
    public T borrowObject() {
        T object = pool.poll();
        if( object == null ) {
            log.debug("Creating new object for pool, currently borrowed {}", borrowed.size());
            object = createObject();
        }
        log.debug("Borrowing object from pool: {} / {}", object, object.hashCode());
        borrowed.add(object);
        return object;
    }
    
    /**
     * Called by borrowObject when the pool needs a new instance to lend out.
     * 
     * @return 
     */
    protected abstract T createObject();
    
    /**
     * Called by returnObject when the pool is removing the object (because
     * it was revoked). Subclasses can clean up objects by removing references,
     * closing connections, etc.
     * 
     */
    protected abstract void trashObject(T object);

    @Override
    public void returnObject(T object) {
        if( borrowed.remove(object) ) {
            if( revoked.remove(object) ) {
                log.debug("Removed revoked object from pool: {} / {}", object, object.hashCode());
                trashObject(object);
                return;
            }
            log.debug("Returning object to pool: {} / {}", object, object.hashCode());
            pool.add(object);
        }
        else {
            log.error("Object was not borrowed: {} / {}", object, object.hashCode());
            throw new IllegalStateException("Object was not borrowed");
        }
    }

    @Override
    public void addObject(T object) {
        log.debug("Adding object to pool: {} / {}", object, object.hashCode());
        pool.add(object);
    }

    @Override
    public void revokeObject(T object) {
        if( pool.remove(object) ) {
            log.debug("Removed revoked object from pool: {} / {}", object, object.hashCode());
            trashObject(object);
            return;
        }
        if( borrowed.contains(object) ) {
            log.debug("Marking revoked object to remove from pool: {} / {}", object, object.hashCode());
            revoked.add(object);
        }
        else {
            log.error("Object was not in pool: {} / {}", object, object.hashCode());
            throw new IllegalStateException("Object was not in pool");
        }
    }
    
}
