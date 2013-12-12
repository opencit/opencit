/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.classpath;

import com.intel.dcsg.cpg.util.Filter;

/**
 * The LimitedClassLoader behaves differently than the standard ClassLoader: instead of checking the parent first, the
 * LimitedClassLoader looks at its own resources first, and if it doesn't find the class it checks the parent, only
 * throwing a ClassNotFoundException if the parent also cannot find the class (and typically this never happens as the
 * parent itself would probably throw ClassNotFoundException).
 *
 * The LimitedClassLoader allows the application to set two filters:
 *
 * The exclusion filter, if set, excludes classes from this behavior, which defers their loading to the standard java
 * ClassLoader. Classes in java.* are excluded by default and cannot be removed from the exclusion filter. However more
 * packages can be added to the exclusion filter.
 *
 * The inclusion filter, if set, limits the "look inside first" behavior ONLY to included classes, and automatically
 * looks to the standard class loader for everything else.
 *
 * Setting both filter combines the behavior, so that we only "look down" for classes accepted by the inclusion filter
 * AND not also excluded.
 *
 * @author jbuhacoff
 */
public class LimitedClassLoader extends ClassLoader {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LimitedClassLoader.class);
//    private final ConcurrentHashMap<String,Class> cache = new ConcurrentHashMap<String,Class>();
    // fencing:
    private Filter<String> include = null;
    private Filter<String> exclude = null;

    public LimitedClassLoader() {
        super();
    }

    public LimitedClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return null;
    }

    public void setExcludeFilter(Filter<String> exclude) {
        this.exclude = exclude;
    }

    public void setIncludeFilter(Filter<String> include) {
        this.include = include;
    }

    /**
     * Classes are loaded using this procedure:
     *
     * 1. if the class is in the package java.* the standard java classloader is used
     *
     * 2. if an inclusion filter is not set, or if it's set and the given class is included, and if an exclusion filter
     * is not set, or if the given class is not excluded, then "look down" to find the class.
     *
     * 3. defer to the parent class loader
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.")) {
            return super.loadClass(name, resolve);
        }
        if ((include == null || include.accept(name)) && (exclude == null || !exclude.accept(name))) {
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                log.debug("Class {} already loaded", name);
                return cached;
            }

            Class<?> local = findClass(name);
            if (local != null) {
                log.debug("Class {} found", name);
                if (resolve) {
                    resolveClass(local);
                }
                return local;
            }

        }
        Class<?> higher = getParent().loadClass(name);
        if (higher != null) {
            log.debug("Class {} found in parent", name);
            return higher;
        }
        throw new ClassNotFoundException(name);
    }
}
