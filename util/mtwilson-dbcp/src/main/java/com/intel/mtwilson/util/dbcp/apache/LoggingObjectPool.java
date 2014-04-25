/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.dbcp.apache;

import java.util.NoSuchElementException;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 *
 * @author jbuhacoff
 */
public class LoggingObjectPool implements ObjectPool {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingObjectPool.class);
    private ObjectPool delegate;
    
    public LoggingObjectPool(ObjectPool delegate) {
        this.delegate = delegate;
        
    }
    
    @Override
    public Object borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
        Object obj = delegate.borrowObject();
        log.debug("borrowObject {}", obj);
        return obj;
    }

    @Override
    public void returnObject(Object obj) throws Exception {
        log.debug("returnObject {}", obj);
        delegate.returnObject(obj);
    }

    @Override
    public void invalidateObject(Object obj) throws Exception {
        log.debug("invalidateObject {}", obj);
        delegate.invalidateObject(obj);
    }

    @Override
    public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
        log.debug("addObject");
        delegate.addObject();
    }

    @Override
    public int getNumIdle() throws UnsupportedOperationException {
        log.debug("getNumIdle");
        return delegate.getNumIdle();
    }

    @Override
    public int getNumActive() throws UnsupportedOperationException {
        log.debug("getNumActive");
        return delegate.getNumActive();
    }

    @Override
    public void clear() throws Exception, UnsupportedOperationException {
        log.debug("clear");
        delegate.clear();
    }

    @Override
    public void close() throws Exception {
        log.debug("close");
        delegate.close();
    }

    @Override
    public void setFactory(PoolableObjectFactory factory) throws IllegalStateException, UnsupportedOperationException {
        log.debug("setFactory {}", factory);
        delegate.setFactory(factory);
    }
    
}
