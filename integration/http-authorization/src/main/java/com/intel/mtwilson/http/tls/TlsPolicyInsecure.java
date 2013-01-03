/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.crypto.DenyAllTrustManager;
import com.intel.mtwilson.crypto.NopX509TrustManager;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.SslUtil;
import com.intel.mtwilson.datatypes.InternetAddress;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
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
public class TlsPolicyInsecure implements TlsPolicy {
    private Logger log = LoggerFactory.getLogger(getClass());
//    private final InternetAddress server;
    private final static NopX509TrustManager nop = new NopX509TrustManager();

    public TlsPolicyInsecure(InternetAddress server) {
//        this.server = server;
    }
    
    @Override
    public X509TrustManager getTrustManager() { return nop; }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
    }

}
