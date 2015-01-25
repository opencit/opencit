/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.text.transform;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class CamelCaseToHyphenatedTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CamelCaseToHyphenatedTest.class);

    @Test
    public void testTransformation() {
        CamelCaseToHyphenated transformer = new CamelCaseToHyphenated();
        assertEquals("hello-world", transformer.toHyphenated("HelloWorld"));
        assertEquals("hello-world", transformer.toHyphenated("helloWorld"));
        assertEquals("hello-world", transformer.toHyphenated("Hello-World"));
        assertEquals("hello-world", transformer.toHyphenated("hello-world"));
    }
    
    @Test
    public void testTransformationIgnoreDots() {
        CamelCaseToHyphenated transformer = new CamelCaseToHyphenated();
        assertEquals("path.to-name.hello-world", transformer.toHyphenated("path.toName.HelloWorld"));
        assertEquals("path.to-name.hello-world", transformer.toHyphenated("path.toName.helloWorld"));
        assertEquals("path.to-name.hello-world", transformer.toHyphenated("path.toName.Hello-World"));
        assertEquals("path.to-name.hello-world", transformer.toHyphenated("path.toName.hello-world"));
    }
    
    @Test
    public void testAbbreviation() {
        CamelCaseToHyphenated transformer = new CamelCaseToHyphenated();
        assertEquals("foo-bar-quux", transformer.toHyphenated("FooBARQuux"));
        assertEquals("foo-bar-quux", transformer.toHyphenated("FOOBarQuux"));
        assertEquals("foo-bar-quux", transformer.toHyphenated("FooBarQUUX"));
    }
    
    @Test
    public void testMtWilson() {
        CamelCaseToHyphenated transformer = new CamelCaseToHyphenated();
        assertEquals("mt-wilson-test", transformer.transform("MtWilsonTest"));
        assertEquals("test-mt-wilson", transformer.transform("TestMtWilson"));
        
    }

    @Test
    public void testCommandNames() {
        CamelCaseToHyphenated transformer = new CamelCaseToHyphenated();
        assertEquals("create-tls-certificate", transformer.transform("CreateTlsCertificate"));
        assertEquals("create-key-management-certificate", transformer.transform("CreateKeyManagementCertificate"));
        assertEquals("export-config", transformer.transform("ExportConfig"));
        
    }

}
