/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

/**
 *
 * @author jbuhacoff
 */
public enum WebContainerType {
    GLASSFISH("Glassfish", 8080,8181),
    TOMCAT("Tomcat", 8080,8443),
    OTHER("Other", 80,443);
    
    private String displayName;
    private int defaultHttpPort;
    private int defaultHttpsPort;
    
    WebContainerType(String name, int httpPort, int httpsPort) {
        displayName = name;
        defaultHttpPort = httpPort;
        defaultHttpsPort = httpsPort;
    }
    
    public String displayName() { return displayName; }
    public int defaultHttpPort() { return defaultHttpPort; }
    public int defaultHttpsPort() { return defaultHttpsPort; }

    @Override
    public String toString() { return displayName; }
}
