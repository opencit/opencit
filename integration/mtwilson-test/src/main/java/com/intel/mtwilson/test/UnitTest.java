/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.test;

import com.intel.dcsg.cpg.util.shiro.Login;
import org.junit.BeforeClass;

/**
 * Convenience base class for junit tests that call into Mt Wilson business
 * logic which may be annotated with required permissions. 
 * 
 * The junit test is granted all permissions so you can focus on the
 * business logic without concern for the security layer.
 * 
 * @author jbuhacoff
 */
public class UnitTest {
    
    @BeforeClass
    public static void login() throws Exception {
        Login.superuser();
    }
}
