/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the X509TrustManager interface that accepts all
 * certificates. 
 * @author jbuhacoff
 */
public class AllowAllX509TrustManager implements X509TrustManager {
    private Logger log = LoggerFactory.getLogger(getClass());

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
}
