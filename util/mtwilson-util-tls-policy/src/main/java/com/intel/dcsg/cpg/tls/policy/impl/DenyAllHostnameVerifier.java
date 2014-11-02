/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import javax.net.ssl.SSLException;
import org.apache.http.conn.ssl.AbstractVerifier;

/**
 *
 * @author jbuhacoff
 */
public class DenyAllHostnameVerifier extends AbstractVerifier {

    @Override
    public void verify(String string, String[] strings, String[] strings1) throws SSLException {
        throw new IllegalArgumentException("DENY-ALL");
    }
    
}
