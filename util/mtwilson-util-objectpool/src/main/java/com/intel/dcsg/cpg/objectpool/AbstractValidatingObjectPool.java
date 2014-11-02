/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.objectpool;

/**
 * A decorator adding object validation to any object pool.
 * 
 * @author jbuhacoff
 */
public abstract class AbstractValidatingObjectPool<T> implements ObjectPool<T> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractValidatingObjectPool.class);
    private final ObjectPool<T> objectPool;
    private boolean validateOnBorrow = false;
    private boolean validateOnReturn = false;

    public AbstractValidatingObjectPool(ObjectPool<T> objectPool) {
        this.objectPool = objectPool;
    }
    
    public boolean isValidateOnBorrow() {
        return validateOnBorrow;
    }

    public void setValidateOnBorrow(boolean validateOnBorrow) {
        this.validateOnBorrow = validateOnBorrow;
    }

    public boolean isValidateOnReturn() {
        return validateOnReturn;
    }

    
    public void setValidateOnReturn(boolean validateOnReturn) {
        this.validateOnReturn = validateOnReturn;
    }

    @Override
    public void addObject(T object) {
        objectPool.addObject(object);
    }

    @Override
    public void revokeObject(T object) {
        objectPool.revokeObject(object);
    }

    @Override
    public T borrowObject() {
        T object = objectPool.borrowObject();
        if( isValidateOnBorrow() ) {
            while( !isValid(object) ) {
                log.debug("Revoking invalid object on borrow and trying again");
                revokeObject(object);
                returnObject(object);
                object = objectPool.borrowObject();
            }
        }
        return object;
    }

    @Override
    public void returnObject(T object) {
        if( isValidateOnReturn() ) {
            if( !isValid(object) ) {
                log.debug("Revoking invalid object on return");
                revokeObject(object);
            }
        }
        objectPool.returnObject(object);
    }
    
    /**
     * Implementations of isValid must not throw an exception - if an error
     * occurs during evaluation they must return false. 
     * 
     * @param object
     * @return true if the object is still in a valid state and can be borrowed again, false if it cannot be reused
     */
    protected abstract boolean isValid(T object);
}
