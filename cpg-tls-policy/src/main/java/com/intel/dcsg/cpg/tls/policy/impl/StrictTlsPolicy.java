/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
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
 * The keystore must already have a trusted certificate for this server. If the
 * server certificate (or the last CA in its chain) is not signed by a trusted
 * certificate, and if a delegate is provided, then the delegate is used to 
 * ask whether the server should be trusted. 
 *
 * If the server has a self-signed certificate, it must be in the keystore to be
 * validated. If the server has a ca-signed certificate, the ca must be in the
 * keystore for the server to be validated.
 *
 * It checks the repository for the trusted certificate every time it is used -
 * if you want to cache the repository information (eg. to avoid a database hit
 * on each SSL connection), then you need to provide an in-memory repository
 * that contains just the cached certificates (query the database once and put
 * the results in an ArrayCertificateRepository and provide that to the policy
 * object).
 *
 * See also: java.security.cert.X509CertSelector, java.security.cert.PKIXBuilderParameters,
 * java.security.cert.CertPathParameters, java.security.cert.CertPathBuilder, 
 * and java.security.cert.CertPath
 * 
 * Note:  currently does not use the Java certpath api.  The verification logic needs review,
 * and the certpath api should be considered.
 * 
 * XXX TODO: there is an RFC for hostname verification logic:  http://tools.ietf.org/html/rfc6125
 * should erplace the "strict hostname verifier" with a "rfc6125 hostname verifier" since it's a 
 * little different than what java does.
 * 
 * XXX TODO:  should we support CRL's?  see http://tools.ietf.org/html/rfc5280
 * 
 * See also: 
 * http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html
 * 
 * See also:
 * http://stackoverflow.com/questions/6143646/validate-x509-certificates-using-java-apis
 * http://stackoverflow.com/questions/6629473/validate-x-509-certificate-agains-concrete-ca-java
 * 
 * @author jbuhacoff
 */
public class StrictTlsPolicy implements TlsPolicy, ApacheTlsPolicy, X509TrustManager {

    private Logger log = LoggerFactory.getLogger(getClass());
    private transient final CertificateRepository repository;
    private transient final TrustDelegate delegate;
//    private transient X509Certificate trustedCertificate = null;

    public StrictTlsPolicy(CertificateRepository repository) {
        this.repository = repository;
        this.delegate = null;
    }

    public StrictTlsPolicy(CertificateRepository repository, TrustDelegate delegate) {
        this.repository = repository;
        this.delegate = delegate;
    }
    
    @Override
    public X509TrustManager getTrustManager() {
        return this;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
    }

    @Override
    public X509HostnameVerifier getApacheHostnameVerifier() {
        return SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // XXX TODO use java certpath api 
    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        if (xcs == null || xcs.length == 0) {
            throw new CertificateException("Server did not present any certificates");
        }
        log.debug("Server presented a certificate chain of length {} with authentication type {}", xcs.length, string);
        // whether we got a single certificate or a chain, ensure all certs are within their validity period
        for(int i=0; i<xcs.length; i++) {
            try {
                xcs[i].checkValidity(); // CertificateExpiredException, CertificateNotYetValidException
            }
            catch(Exception e) {
                throw new CertificateException("Server certificate is invalid: "+xcs[i].getSubjectX500Principal().getName()+": "+e.toString());
            }
        }
        // if we got a chain, ensure that each certificate in the chain was signed by the next certificate (except for the last one which doesn't have a next certificate)
        for(int i=0; i<xcs.length-1; i++) {
            try {
                xcs[i].verify(xcs[i+1].getPublicKey()); // throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
            }
            catch(Exception e) {
                throw new CertificateException("Server certificate chain is invalid: cannot verify that "+xcs[i+1].getSubjectX500Principal().getName()+" signed "+xcs[i].getSubjectX500Principal().getName()+": "+e.toString());
            }
        }
        // whether we got one certificate or a chain, check if any are trusted directly
        List<X509Certificate> trustedCertificates = repository.getCertificates(); //repository.getCertificateForSubject(xcs[i].getSubjectX500Principal().getName());  
        for(int i=0; i<xcs.length; i++) {
            try {
                for(X509Certificate trustedCert : trustedCertificates) {
                    if(Arrays.equals(trustedCert.getEncoded(), xcs[i].getEncoded())) {
                        return; // certificate appears in the trusted repository, so we trust it and anything that it signed
                    }
                }
            }
            catch(Exception e) {
                throw new CertificateException("Cannot check if certificate is trusted: "+xcs[i].getSubjectX500Principal().getName()+": "+e.toString());
            }
        }
        // now check if the last certificate in the chain was SIGNED BY any certificate in our trusted repository
        for(X509Certificate trustedCert : trustedCertificates) {
            if( Arrays.equals(trustedCert.getSubjectX500Principal().getEncoded(), xcs[xcs.length-1].getIssuerX500Principal().getEncoded()) ) {
                try {
                    xcs[xcs.length-1].verify(trustedCert.getPublicKey());
                    return; // a trusted certificate has signed the last certificate in the server's chain
                }
                catch(Exception e) {
                    log.info("Trusted certificate {} looks like issuer of {} but could not be verified: {}", new Object[] { trustedCert.getSubjectX500Principal().getName(), xcs[xcs.length-1].getSubjectX500Principal().getName(), e.toString() });                
                }
            }
        }
        // we did not find a trusted certificate... if a delegate is available, we can ask the user
        if( delegate != null ) {
            if( delegate.acceptUnknownCertificate(xcs[xcs.length-1]) ) {
                return; // user accepted the unknown certificate
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
    
    @Override
    public boolean providesConfidentiality() {
        return true;
    }

    @Override
    public boolean providesAuthentication() {
        return true;
    }

    @Override
    public boolean providesIntegrity() {
        return true;
    }
    
}
