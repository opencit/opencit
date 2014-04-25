/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author jbuhacoff
 */
public abstract class AbstractObjectPool<T> implements ObjectPool<T> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractObjectPool.class);
    private ConcurrentLinkedQueue<T> pool = new ConcurrentLinkedQueue<>();
    private Set<T> borrowed = Collections.newSetFromMap(new ConcurrentHashMap<T,Boolean>());
    private Set<T> revoked = Collections.newSetFromMap(new ConcurrentHashMap<T,Boolean>());
    
    @Override
    public T borrowObject() {
        T object = pool.poll();
        if( object == null ) {
            log.debug("Creating new object for pool");
            object = createObject();
        }
        log.debug("Borrowing object from pool: {} / {}", object, object.hashCode());
        borrowed.add(object);
        return object;
    }
    
    protected abstract T createObject();

    @Override
    public void returnObject(T object) {
        if( borrowed.remove(object) ) {
            if( revoked.remove(object) ) {
                log.debug("Removed revoked object from pool: {} / {}", object, object.hashCode());
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
