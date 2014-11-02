/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.objectpool;

/**
 *
 * @author jbuhacoff
 */
public interface ObjectPool<T> {
    /**
     * <pre>
     * obj = pool.borrowObject();
     * </pre>
     * 
     * @return an instance from the pool
     */
    T borrowObject();
    
    /**
     * The contract is that after calling returnObject the caller
     * does not reference the object any more. 
     * 
     * <pre>
     * pool.returnObject(obj);
     * obj = null;
     * </pre>
     * 
     * @param object to return to the pool
     */
    void returnObject(T object);
    
    /**
     * Adds an object to the pool, making it available for borrowing.
     * The contract is that after calling addObject the caller
     * does not reference the object anymore except perhaps to 
     * revoke it later. 
     * 
     * @param object 
     */
    void addObject(T object);
    
    /**
     * Revokes an object from the pool, making it unavailable for borrowing.
     * 
     * This is the opposite of addObject but takes into account the possibility
     * that the object may currently be borrowed from the pool. 
     * 
     * If the object is currently borrowed, it will be removed from the
     * pool when it is returned. If the object is currently in the pool, 
     * then it will be removed immediately.
     * 
     * <pre>
     * obj = pool.borrowObject();
     * try {
     *   // do something with obj
     * }
     * catch(Exception e) {
     *   // only revoke the object if the exception was caused by the object
     *   // and it is now in an invalid state... if it could still be used by
     *   // another caller do not revoke it, just return it to the pool
     *   pool.revokeObject(obj);
     * }
     * finally {
     *   // return the object to the pool - if we revoked it in the catch
     *   // block then it will not be available for borrowing again
     *   pool.returnObject(obj);
     * }
     * </pre>
     * 
     * </pre>
     * 
     * @param object 
     */
    void revokeObject(T object);
}
