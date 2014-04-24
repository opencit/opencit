/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.test;

import com.intel.mtwilson.shiro.env.JunitEnvironment;
import org.junit.BeforeClass;

/**
 * Convenience base class for junit tests that call into Mt Wilson business
 * logic which may be annotated with required permissions. 
 * 
 * Use this base class when you are testing against a remote database with
 * existing users. 
 * 
 * @author jbuhacoff
 */
public class IntegrationTest {
    
    @BeforeClass
    public static void login() throws Exception {
        JunitEnvironment.login();
    }
}
