/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.test;

import com.intel.dcsg.cpg.util.shiro.Login;
import com.intel.mtwilson.My;
import com.intel.mtwilson.shiro.env.JunitEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.junit.BeforeClass;

/**
 * Convenience base class for junit tests that call into Mt Wilson business
 * logic which may be annotated with required permissions. 
 * 
 * Use this base class when you are testing against a remote database with
 * existing users. 
 * 
 * See also mtwilson-shiro-util test/resources for example shiro-junit.ini and
 * example test.properties.
 * 
 * @author jbuhacoff
 */
public class IntegrationTest {
    
    @BeforeClass
    public static void login() throws Exception {
        String filename = My.filesystem().getConfigurationPath()+File.separator+"test.properties";
        File file = new File(filename);
        try(FileInputStream in = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(in);
            String username = properties.getProperty("login.username");
            String password = properties.getProperty("login.password");
            File ini = new File(My.filesystem().getConfigurationPath()+File.separator+"shiro-junit.ini");
            Login.existingUser(ini, username, password);
        }
        
    }
}
