/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.objectpool.util;

import com.intel.dcsg.cpg.objectpool.ObjectPool;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**
 * This decorator wraps all borrowed objects with a proxy that automatically
 * returns the object to the pool when close() is called on the object.
 * This decorator is limited to pools of objects that implement Closeable.
 * The underlying object is not closed, instead it is returned to the pool
 * and is available for continued use. 
 * When the object is revoked and removed from the pool then the close() method
 * is called on the underlying object (this is the responsibility of the pool
 * itself - if the pool extends from AbstractObjectPool it would do this in 
 * the trashObject method)
 * 
 * The pooled objects being wrapped must have a no-arg constructor in order
 * for this to work.
 * 
 * Also, this decorator must be used on the object AFTER it has been borrowed
 * from the pool. If you decorate the object before adding it to the pool then
 * when the decorator tries to return the wrapped object to the pool the 
 * pool may throw an exception because it will not recognize the decorator.
 * 
 * Example:
 * <pre>
 * ConnectionPool connectionPool = new ConnectionPool(dataSource);
 * Connection connection = connectionPool.borrowObject();
 * Connection pooledConnection = ReturnOnCloseObjectFactory.wrap(connection, connectionPool);
 * // do something with the pooled connection
 * pooledConnection.close(); // does NOT close connection - returns underlying open connection to pool
 * connectionPool.revokeObject(connection); // removes it from pool and closes it
 * </pre>
 * 
 * Use of this factory requires javassist (optional dependency)
 * 
 * @author jbuhacoff
 */
public class ReturnOnCloseObjectFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReturnOnCloseObjectFactory.class);
    private static final ConcurrentHashMap<Class,Class> proxyMap = new ConcurrentHashMap<>();

    public static <T extends Closeable> T wrap(T object, ObjectPool<T> objectPool) throws InstantiationException, IllegalAccessException {
        Class proxyClass = proxyMap.get(object.getClass());
        if( proxyClass == null ) {
            proxyClass = createProxy(object.getClass());
            proxyMap.put(object.getClass(), proxyClass);
        }
        T newInstance = (T)proxyClass.newInstance();
        ((ProxyObject) newInstance).setHandler(new ObjectPoolCloseableProxyMethodHandler(object, objectPool));
        log.debug("new instance class is {}", newInstance.getClass().getName());
        return newInstance;
    }
    
    /**
     * Given a class that implements Closeable, this method creates a new 
     * proxy class for it. This only needs to be done once per class so 
     * we keep track of already-created proxy classes using a map.
     * 
     * @param clazz
     * @return 
     */
    protected static Class<? extends Closeable> createProxy(Class<? extends Closeable> clazz) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        factory.setInterfaces(new Class[]{Closeable.class});
        Class proxyClass = factory.createClass();
        log.debug("Created new class {}", proxyClass.getName());
        return proxyClass;
    }
    
    public static class ObjectPoolCloseableProxyMethodHandler<T extends Closeable> implements MethodHandler {

        private T wrapped;
        private ObjectPool<T> objectPool;

        public ObjectPoolCloseableProxyMethodHandler(T wrapped, ObjectPool<T> objectPool) {
            this.wrapped = wrapped;
            this.objectPool = objectPool;
        }

        @Override
        public Object invoke(Object self, Method override, Method proceed, Object[] args) throws Throwable {
            if (override.getName().equals("close") && override.getParameterTypes().length == 0) {
                log.debug("overriding close method with custom implementation");
                objectPool.returnObject(wrapped); // it's the wrapped object that is managed by the pool so that it can call any method on it including "close" 
                return null;
            } else {
                log.debug("invoking method");
                return proceed.invoke(wrapped, args);
            }
        }
    }
    
}
