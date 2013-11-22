/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jmod.cl;

import com.intel.mtwilson.jmod.JarUtil;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The JarFileClassLoader can load classes from a single jar file. If a named class is not found
 * in the jar file, the findClass method returns null.  This is because in this system it's normal
 * not to find a given class because it may be in other jar files which are handled by separate
 * JarFileClassLoaders. So the parent class loader is in charge of trying all available class loaders
 * before delegating to its grand-parent class loader, likely the default class loader
 * (which would throw ClassNotFoundException if the
 * class is not found at that point).
 * 
 * XXX TODO the jarfileclassloader is only being used by classloadertest and reflectionstest... 
 * it's useful to have it inside the container for the reflections test but other than that it should really be in the 
 * launcher where all the class loader magic will be.   OR, since the value added by this class is really 
 * the part about opening the JarFile and using JarEntry to read files and how to convert a fully qualified
 * class name into a path to a class in the jar file,  maybe that little bit of behavior can be refactored into
 * a JarFileClassFinder which accepts the fully qualified class name and returns either null or the byte array,
 * and this can be used by a JarFileClassLoader anywhere and doesn't necessarily change the class loading rules
 * like ModuleClassLoader does. 
 * 
 * @author jbuhacoff
 */
public class JarFileClassLoader extends LimitedClassLoader implements Closeable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JarFileClassLoader.class);
    private final File jar;
    private JarFile jarFile = null;
//    private final ConcurrentHashMap<String,Class> cache = new ConcurrentHashMap<String,Class>();

    public JarFileClassLoader(File jar) throws IOException {
        super();
        this.jar = jar;
        this.jarFile = new JarFile(jar);
    }

    public JarFileClassLoader(File jar, ClassLoader parent) throws IOException {
        super(parent);
        this.jar = jar;
        this.jarFile = new JarFile(jar);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = JarUtil.readClass(jarFile, name);
            if( data == null ) {
                log.debug("Cannot find class {}", name);
                return null;                
            }
            Class<?> local = defineClass(name, data, 0, data.length);
            log.debug("Loaded class {} from file {}", name, jar.getName());
            return local;
        } catch (Exception e) {
            log.error("Cannot load class {}", name, e);
            throw new ClassNotFoundException("Cannot load class: "+name, e);
        }
    }
    
    @Override
    public void close() throws IOException {
        jarFile.close();
    }
    
    

}
