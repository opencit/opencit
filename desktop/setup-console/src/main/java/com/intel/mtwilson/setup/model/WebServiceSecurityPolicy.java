/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.model;

/**
 *
 * @author jbuhacoff
 */
public class WebServiceSecurityPolicy {
    public boolean isTlsRequired = true;
    public String[] trustedClients = new String[] { }; // do not require authentication;   
}
