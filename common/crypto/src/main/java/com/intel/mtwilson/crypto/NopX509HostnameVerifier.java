/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 *
 * @author jbuhacoff
 */
public class NopX509HostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String string, SSLSession ssls) {
        return true;
    }
    
    
}
