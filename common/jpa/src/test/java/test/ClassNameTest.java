/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test;

import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ClassNameTest {
    @Test
    public void testClassName() {
        System.out.println("getName: "+ClassNameTest.class.getName());
        System.out.println("getSimpleName: "+ClassNameTest.class.getSimpleName());
        System.out.println("getCanonicalName: "+ClassNameTest.class.getCanonicalName());
    }
}
