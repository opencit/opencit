/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This trust policy must be initialized with the server that is being checked
 * and its associated server-specific keystore.
 *
 * This policy trusts all server certificates (insecure) so it never adds new certificates to its empty keystore.
 *
 * @author jbuhacoff
 */
public class InsecureTlsPolicy implements TlsPolicy, ApacheTlsPolicy {

    private Logger log = LoggerFactory.getLogger(getClass());
    private final static AllowAllX509TrustManager nop = new AllowAllX509TrustManager();
    private final static ArrayCertificateRepository emptyRepository = new ArrayCertificateRepository(new X509Certificate[0]);

    public InsecureTlsPolicy() {
    }

    @Override
    public X509TrustManager getTrustManager() {
        return nop;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
    }

    @Override
    public X509HostnameVerifier getApacheHostnameVerifier() {
        return SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
    }

    @Override
    public CertificateRepository getCertificateRepository() {
        return emptyRepository;
    }

    @Override
    public boolean providesConfidentiality() {
        return false;
    }

    @Override
    public boolean providesAuthentication() {
        return false;
    }

    @Override
    public boolean providesIntegrity() {
        return false; // because an attacker can modify and forward the message in a man-in-the-middle attack
    }
}
