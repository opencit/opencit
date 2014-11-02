/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Throws UnsupportedOperationException for the checkClientTrusted methods
 * and leaves the checkServerTrusted methods for subclasses to define.
 * 
 * @author jbuhacoff
 */
public abstract class X509ExtendedServerTrustManager extends X509ExtendedTrustManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509ExtendedServerTrustManager.class);
    
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        throw new UnsupportedOperationException("checkClientTrusted with Socket");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine ssle) throws CertificateException {
        throw new UnsupportedOperationException("checkClientTrusted with SSLEngine");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        throw new UnsupportedOperationException("checkClientTrusted");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        checkServerTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine ssle) throws CertificateException {
        checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        throw new UnsupportedOperationException("getAcceptedIssuers");
    }
    
    
}
