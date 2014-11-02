/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * A simple implementation of the X509TrustManager interface that accepts all
 * certificates. 
 * @author jbuhacoff
 */
public class AllowAllX509TrustManager extends X509ExtendedTrustManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AllowAllX509TrustManager.class);
    

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        log.warn("Insecure: accepting all client certificates");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        log.warn("Insecure: accepting all server certificates");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
        log.warn("Insecure: accepting all client certificates with Socket");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
        log.warn("Insecure: accepting all server certificates with Socket");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
        log.warn("Insecure: accepting all client certificates with SSLEngine");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
        log.warn("Insecure: accepting all server certificates with SSLEngine");
    }
}
