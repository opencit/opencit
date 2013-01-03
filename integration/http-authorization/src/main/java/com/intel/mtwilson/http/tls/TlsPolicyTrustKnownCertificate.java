/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.http.tls;

import com.intel.mtwilson.crypto.DenyAllTrustManager;
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
 * It checks the repository for the trusted certificate every time it is used - 
 * if you want to cache the repository information (eg. to avoid a database hit on 
 * each SSL connection), then you need to provide
 * an in-memory repository that contains just the cached certificates (query 
 * the database once and put the results in an ArrayCertificateRepository and
 * provide that to the policy object).
 * 
 * @author jbuhacoff
 */
public class TlsPolicyTrustKnownCertificate implements TlsPolicy, X509TrustManager {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final InternetAddress server;
    private transient final CertificateRepository repository;
//    private transient X509Certificate trustedCertificate = null;

    public TlsPolicyTrustKnownCertificate(InternetAddress server, CertificateRepository keystore) {
        this.server = server;
        this.repository = keystore;
        
    }
    
    @Override
    public X509TrustManager getTrustManager() { return this; }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        X509Certificate trustedCertificate = repository.getCertificateForAddress(server);  
        if( trustedCertificate == null ) {
            throw new CertificateException("No trusted certificates for server: "+server.toString());
        }
        else {
            boolean isTrusted = false;
            for(X509Certificate cert : xcs) {
                if( Arrays.equals(trustedCertificate.getEncoded(), cert.getEncoded()) ) {
                    isTrusted = true;
                    break;
                }
            }
            if( !isTrusted ) {
                throw new CertificateException("Server certificate is not trusted");
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
    
}
