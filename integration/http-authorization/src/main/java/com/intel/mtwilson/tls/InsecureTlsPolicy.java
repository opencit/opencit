/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import com.intel.mtwilson.crypto.NopX509TrustManager;
//import com.intel.mtwilson.model.InternetAddress;
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
 * The keystore must already have a trusted certificate for this server. 
 * This policy never adds new certificates to the keystore.
 * 
 * @author jbuhacoff
 */
public class InsecureTlsPolicy implements TlsPolicy, ApacheTlsPolicy {
    private Logger log = LoggerFactory.getLogger(getClass());
//    private final InternetAddress server;
    private final static NopX509TrustManager nop = new NopX509TrustManager();
    private final static ArrayCertificateRepository emptyRepository = new ArrayCertificateRepository(new X509Certificate[0]);
    public InsecureTlsPolicy() { }
    /*
    public InsecureTlsPolicy(InternetAddress server) {
//        this.server = server;
    }
    */
    
    @Override
    public X509TrustManager getTrustManager() { return nop; }

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
    
}
