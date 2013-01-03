/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author jbuhacoff
 */
public class DenyAllTrustManager implements X509TrustManager {
    
    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        throw new CertificateException("Client certificate denied; check configuration");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        throw new CertificateException("Server certificate denied; check configuration");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
    
}
