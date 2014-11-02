/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author jbuhacoff
 */
public class CertificateStoringX509TrustManager implements X509TrustManager {
    private X509Certificate[] certificates = null;
    
    public X509Certificate[] getStoredCertificates() {
        return certificates;
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        certificates = xcs;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
    
}
