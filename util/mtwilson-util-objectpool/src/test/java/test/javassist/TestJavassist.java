/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.javassist;

import com.intel.dcsg.cpg.objectpool.ObjectPool;
import com.intel.dcsg.cpg.objectpool.AbstractObjectPool;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.junit.Test;

/**
 * Reference:
 * http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial2.html#add
 *
 * @author jbuhacoff
 */
public class TestJavassist {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestJavassist.class);

    // original class to modify
    public static class Example implements Closeable {

        @Override
        public void close() throws IOException {
            log.debug("Example close");
        }
    }

    public static class ExamplePool extends AbstractObjectPool<Example> {

        @Override
        protected Example createObject() {
            return new Example();
        }

        @Override
        protected void trashObject(Example object) {
            // do nothing
        }
        
    }

    @Test
    public void testExample() throws Exception {
        Example example = new Example();
        example.close();
    }

    // interface to set object pool on the proxy
    public static interface ObjectPoolCloseable<T> {
        void close();
    }

    public static class ObjectPoolCloseableProxyMethodHandler<T> implements MethodHandler {

        private T wrapped;
        private ObjectPool<T> objectPool;

        public ObjectPoolCloseableProxyMethodHandler(ObjectPool<T> objectPool, T wrapped) {
            this.objectPool = objectPool;
            this.wrapped = wrapped;
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

    /**
     * Example:
     * <pre>
     * 15:34:17.908 [main] DEBUG c.i.mtwilson.util.dbcp.TestJavassist - Created new class com.intel.mtwilson.util.dbcp.TestJavassist$Example_$$_javassist_0
     * 15:34:17.915 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Creating new object for pool
     * 15:34:17.915 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Borrowing object from pool: com.intel.mtwilson.util.dbcp.TestJavassist$Example@480ae510 / 1208673552
     * 15:34:17.916 [main] DEBUG c.i.mtwilson.util.dbcp.TestJavassist - new instance class is com.intel.mtwilson.util.dbcp.TestJavassist$Example_$$_javassist_0
     * 15:34:17.916 [main] DEBUG c.i.mtwilson.util.dbcp.TestJavassist - overriding close method with custom implementation
     * 15:34:17.916 [main] DEBUG c.i.m.util.dbcp.AbstractObjectPool - Returning object to pool: com.intel.mtwilson.util.dbcp.TestJavassist$Example@480ae510 / 1208673552
     * </pre>
     *     
*
     * @throws Exception
     */
    @Test
    public void testExampleProxy() throws Exception {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(Example.class);
        factory.setInterfaces(new Class[]{ObjectPoolCloseable.class});
        Class aClass = factory.createClass();
        log.debug("Created new class {}", aClass.getName());

        /*
         final Example newInstance = (Example) aClass.newInstance();
         MethodHandler methodHandler = new MethodHandler() {
         @Override
         public Object invoke(Object self, Method overridden, Method proceed, Object[] args) throws Throwable {
         }
         };
         ((ProxyObject)newInstance).setHandler(methodHandler);
         */
        ExamplePool objectPool = new ExamplePool();
        Example pooledObject = objectPool.borrowObject();
        Example newInstance = (Example) aClass.newInstance();
        ((ProxyObject) newInstance).setHandler(new ObjectPoolCloseableProxyMethodHandler(objectPool, pooledObject));

        log.debug("new instance class is {}", newInstance.getClass().getName());
        newInstance.close();
    }
    /*
     @Test
     public void testAddMethod() throws NotFoundException, CannotCompileException {
     CtClass point = ClassPool.getDefault().get("Example");
     CtField f = CtField.make("public ObjectPool<?> __objectPool = null;", point);
     point.addField(f);

     CtMethod m = CtNewMethod.make(
     "public void setObjectPool(ObjectPool<?> objectPool) { this.__objectPool = objectPool; }",
     point);
     point.addMethod(m);        
     }*/
}
