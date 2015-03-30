/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.xml;

import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ClasspathResolverTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClasspathResolverTest.class);

    @Test
    public void testPackageToPath() {
        assertEquals("", toPath(""));
        assertEquals("/", toAbsolutePath(""));
        assertEquals("foo", toPath("foo"));
        assertEquals("/foo", toAbsolutePath("foo"));
        assertEquals("foo/bar", toPath("foo.bar"));
        assertEquals("/foo/bar", toAbsolutePath("foo.bar"));
        assertEquals("fooX/barY", toPath("fooX.barY"));
        assertEquals("/fooX/barY", toAbsolutePath("fooX.barY"));
    }
    
    @Test
    public void testResolver() {
        ClasspathResourceResolver resolver = new ClasspathResourceResolver();
        InputStream in = resolver.findResource("http://example.com/path/to/logback-test.xml");
        assertNotNull(in);
    }
    @Test
    public void testResolverWithPrefix() {
        ClasspathResourceResolver resolver = new ClasspathResourceResolver();
        resolver.setResourcePackage("xsd");
        InputStream in = resolver.findResource("http://www.w3.org/TR/2002/REC-xmlenc-core-20021210/xenc-schema.xsd");
        assertNotNull(in);
    }
    @Test
    public void testResolverWithPrefixFilename() {
        ClasspathResourceResolver resolver = new ClasspathResourceResolver();
        resolver.setResourcePackage("xsd");
        InputStream in = resolver.findResource("XMLSchema.xsd");
        assertNotNull(in);
    }
    
    @Test
    public void testResolverInvalidURL() {
        ClasspathResourceResolver resolver = new ClasspathResourceResolver();
        InputStream in = resolver.findResource("http://example.com");
        assertNull(in);
    }

    @Test
    public void testResolverNonExistentPath() {
        ClasspathResourceResolver resolver = new ClasspathResourceResolver();
        InputStream in = resolver.findResource("http://example.com/nonexistent/path/to/file");
        assertNull(in);
    }
    
    private String toPath(String packageName) {
        return packageName.replace(".", "/");
    }
    private String toAbsolutePath(String packageName) {
        return "/"+toPath(packageName);
    }
    
}
