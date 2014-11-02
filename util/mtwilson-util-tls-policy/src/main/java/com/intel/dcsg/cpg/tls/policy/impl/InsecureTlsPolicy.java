/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import java.security.cert.X509Certificate;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This trust policy must be initialized with the server that is being checked
 * and its associated server-specific keystore.
 *
 * This policy trusts all server certificates (insecure) so it never adds new certificates to its empty keystore.
 *
 * Reference:
 * http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509ExtendedTrustManager.html
 * 
 * @author jbuhacoff
 */
public class InsecureTlsPolicy implements TlsPolicy {

    private Logger log = LoggerFactory.getLogger(getClass());
    private final static AllowAllHostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
    private final static AllowAllX509TrustManager nop = new AllowAllX509TrustManager();
    private final static ArrayCertificateRepository emptyRepository = new ArrayCertificateRepository(new X509Certificate[0]);
    private final static AnyProtocolSelector selector = new AnyProtocolSelector();
    
    public InsecureTlsPolicy() {
        log.warn("Insecure: accepting all server certificates and skipping hostname verification");
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return nop;
    }

    @Override
    public X509HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
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

    @Override
    public ProtocolSelector getProtocolSelector() {
        return selector;
    }
}
