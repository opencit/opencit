/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 *
 * @author jbuhacoff
 */
public class NopX509HostnameVerifierApache implements X509HostnameVerifier {

    @Override
    public void verify(String string, SSLSocket ssls) throws IOException {
    }

    @Override
    public void verify(String string, X509Certificate xc) throws SSLException {
    }

    @Override
    public void verify(String string, String[] strings, String[] strings1) throws SSLException {
    }

    @Override
    public boolean verify(String string, SSLSession ssls) {
        return true;
    }
    
    
}
