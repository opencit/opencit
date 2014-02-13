/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public interface ApplicationFilesystem {

    String getApplicationPath();  // example: /opt/mtwilson

    String getConfigurationPath();// example: /opt/mtwilson/configuration

    String getEnvironmentExtPath(); // example: /opt/mtwilson/configuration/env.d

//    String getBinPath(); // example:  /opt/mtwilson/bin

//    String getJavaPath(); // core libraries   java/*.jar  like launcher and its dependencies

//    String getJavaExtPath(); // plugin java libraries  java.d/plugin/*.jar

//    String getUtilPath();

//    String getLicensePath(); // should there be a top-level license.d ? or should it be all plugins?   essentially will we repeat at top-level everything that plugins have but for core?  or should core just be organized like a plugin? 
    
    FeatureFilesystem getBootstrapFilesystem();
    FeatureFilesystem getFeatureFilesystem(String featureId);
    
}
