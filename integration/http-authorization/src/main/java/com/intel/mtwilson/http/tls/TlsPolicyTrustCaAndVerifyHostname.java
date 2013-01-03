/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.crypto.DenyAllTrustManager;
import com.intel.mtwilson.crypto.SslUtil;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyTrustCaAndVerifyHostname implements TlsPolicy {
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateRepository keystore;

    public void setKeystore(CertificateRepository keystore) {
        this.keystore = keystore;
    }
    
    @Override
    public X509TrustManager getTrustManager() {
        try {
            return SslUtil.createX509TrustManagerWithCertificates(keystore.getCertificateAuthorities());  // we either need to make a getCertificateAuthorities():X509Certificate[] function in the CertificateRepository interface, or write our own path-builder using the existing function getCertificateForSubjetByIssuer
        }
        catch(Exception e) {
            log.error("Cannot create X509 Trust Manager with Keystore: "+e.toString());
            return new DenyAllTrustManager();
        }
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
    }
    
}
