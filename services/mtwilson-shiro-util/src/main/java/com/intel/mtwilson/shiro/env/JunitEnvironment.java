/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.env;

import com.intel.mtwilson.My;
import java.io.File;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;

/**
 *
 * @author jbuhacoff
 */
public class JunitEnvironment {
    
    public static void init() {
        // first look for shiro.ini in the local mtwilson install
        String filename = "file:///"+(My.filesystem().getConfigurationPath()+File.separator+"shiro.ini").replace(File.separator,"/");
        // filename = "classpath:shiro.ini"
        // initialize shiro ... should be in mtwilson-launcher  (to intialize for stand-alone app, or for an app hosted on a java web server)
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(filename);
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager); // sets a single shiro security manager to be used for entire jvm... fine for a stand-alone app but when running inside a web app container or in a multi-user env. it needs to be maintained by some container and set on every thread that will do work ...         
    }
}
