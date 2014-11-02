/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author jbuhacoff
 */
public class JarClassIterator implements Iterator<Class<?>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JarClassIterator.class);

    private final File jar;
    private final ClassLoader classLoader;
    private final JarFile jarFile;
    private final Enumeration<JarEntry> enumeration;
    private Class<?> nextClass = null;

    /**
     * 
     * @param jar the file to scan for classes
     * @param classLoader to use for loading classes from the Jar; you can use new JarFileCLassLoader(jar)
     * @throws IOException 
     */
    public JarClassIterator(File jar, ClassLoader classLoader) throws IOException {
        this.jar = jar;
        this.classLoader = classLoader;
        this.jarFile = new JarFile(jar);
        this.enumeration = jarFile.entries();
    }

    protected boolean accept(String name) {
        return true;
    }

    protected boolean accept(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean hasNext() {
        if (nextClass != null) {
            return true;
        }
        // look for matching jar entry
        while (nextClass == null && enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement();
            if (!jarEntry.getName().endsWith(".class")) {
                continue;
            }
            String className = jarEntry.getName().replace("/", ".").substring(0, jarEntry.getName().length() - 6); // the fully qualified name of a matching class such as "org.foo.Bar"
            if (accept(className)) {
                try {
                    Class<?> candidate = classLoader.loadClass(className); // throws ClassNotFoundException and NoClassDefFoundError (caught below) if the class is not in the jar file;  this happens one one class references another class which may be from another dependency that is not on the current classpath;  also may throw "IncompatibleClassChangeError: Implementing class"  if we try to load an abstract class
                    if (accept(candidate)) {
                        nextClass = candidate;
                        return true;
                    }
                } catch (Throwable e) {
                    log.debug("Failed to load class {} from archive {}", nextClass, jar.getAbsolutePath(), e);
                }
            }
        }
        // did not find any more matching entries, we're done
        try {
            jarFile.close();
        } catch (IOException e) {
            log.debug("Cannot close archive {}", jar.getAbsolutePath(), e);
        }
        return false;
    }

    @Override
    public Class<?> next() {
        // caller is NOT required to call hasNext() before calling next() ... we do it if necessary
        if (nextClass == null && !hasNext()) {
            throw new NoSuchElementException("Archive does not have any more matching classes");
        }
        assert nextClass != null;
        Class<?> found = nextClass;
        nextClass = null;
        return found;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove classes from an archive");
    }
}
