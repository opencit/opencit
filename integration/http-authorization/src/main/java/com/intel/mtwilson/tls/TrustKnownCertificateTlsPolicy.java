/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
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
 * If the server has a self-signed certificate, it must be in the keystore to
 * be validated. If the server has a ca-signed certificate, the ca must be
 * in the keystore for the server to be validated.
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
public class TrustKnownCertificateTlsPolicy implements TlsPolicy, ApacheTlsPolicy, X509TrustManager {
    private Logger log = LoggerFactory.getLogger(getClass());
    private transient final CertificateRepository repository;
//    private transient X509Certificate trustedCertificate = null;

    public TrustKnownCertificateTlsPolicy(CertificateRepository keystore) {
        this.repository = keystore;
        
    }
    
    @Override
    public X509TrustManager getTrustManager() { return this; }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
    }

    @Override
    public X509HostnameVerifier getApacheHostnameVerifier() {
        return SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
    }
    
    
    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        if( xcs == null || xcs.length == 0 ) { throw new IllegalArgumentException("Server did not present any certificates"); }
        System.out.println("TlsPolicyTrustKnownCertificate(#"+xcs.length+","+string+")");
        List<X509Certificate> trustedCertificates = repository.getCertificates(); //repository.getCertificateForSubject(xcs[i].getSubjectX500Principal().getName());  
        for(int i=0; i<xcs.length; i++) {
            for(X509Certificate trustedCert : trustedCertificates) {
                if( Arrays.equals(trustedCert.getEncoded(), xcs[i].getEncoded())) {
                    try {
                        xcs[i].checkValidity(); // CertificateExpiredException, CertificateNotYetValidException
                        return; // XXX TODO   we need to check the entire chain... we can't just accept any ca  , we need t omake sure tehre is a PATH from the server cert to the trusted cert.
                    }
                    catch(Exception e) {
                        log.trace("TrustKnownCertificateTlsPolicy checkServerTrusted cert was not valid. checking next cert");
                        // don't rethrow because we need to check the rest of the certs
                    }
                }
            }
        }
        throw new CertificateException("Server certificate is not trusted");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public CertificateRepository getCertificateRepository() {
        return repository;
    }
    

}
