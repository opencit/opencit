/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.module;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 *
 * The name pattern applies to the fully qualified class name, for example org.foo.Bar 
 * 
 * 
 * @author jbuhacoff
 */
public class JarClassPatternIterator extends JarClassIterator implements Iterator<Class<?>> {
    private final Pattern namePattern;

    /**
     * 
     * @param jar the file to scan for classes
     * @param classLoader to use for loading classes from the Jar; you can use new JarFileCLassLoader(jar)
     * @param namePattern to apply to the fully qualified class name, for example ".*Bar$" would match "org.foo.Bar" and "org.FooBar"
     * @throws IOException 
     */
    public JarClassPatternIterator(File jar, ClassLoader classLoader, Pattern namePattern) throws IOException {
        super(jar, classLoader);
        this.namePattern = namePattern;
    }

    @Override
    protected boolean accept(String name) { 
        return namePattern == null || namePattern.matcher(name).matches();
    }
    
}
