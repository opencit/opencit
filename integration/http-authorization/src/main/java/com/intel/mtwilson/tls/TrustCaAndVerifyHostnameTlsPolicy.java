/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls;

import com.intel.mtwilson.crypto.X509Util;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TrustCaAndVerifyHostnameTlsPolicy implements TlsPolicy, ApacheTlsPolicy, X509TrustManager {
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateRepository repository;

    public TrustCaAndVerifyHostnameTlsPolicy() { }
    public TrustCaAndVerifyHostnameTlsPolicy(CertificateRepository repository) {
        setKeystore(repository);
    }
    
    public final void setKeystore(CertificateRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public X509TrustManager getTrustManager() { return this;
    /*
        try {
            return SslUtil.createX509TrustManagerWithCertificates(repository.getCertificateAuthorities().toArray(new X509Certificate[0]));  // we either need to make a getCertificateAuthorities():X509Certificate[] function in the CertificateRepository interface, or write our own path-builder using the existing function getCertificateForSubjetByIssuer
        }
        catch(Exception e) {
            System.err.println("Cannot create X509 Trust Manager with Keystore: "+e.toString());
            return new DenyAllTrustManager();
        }*/
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

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        if( xcs == null || xcs.length == 0 ) { throw new IllegalArgumentException("Server did not present any certificates"); }
        System.out.println("TlsPolicyTrustCaAndVerifyHostname(#"+xcs.length+","+string+")");
//        List<X509Certificate> trustedAuthorities = repository.getCertificateAuthorities();
        List<X509Certificate> trustedCertificates =  repository.getCertificates();
        List<X509Certificate> trustedSubjects = getSubjects(trustedCertificates); //repository.getCertificateForSubject(xcs[i].getSubjectX500Principal().getName());  
        List<X509Certificate> trustedIssuers = getIssuers(trustedCertificates);
        for(int i=0; i<xcs.length; i++) {
//            System.out.println(String.format("xcs[%d] = %s", i, xcs[i].getSubjectX500Principal().getName()));
            // for each certificate in the chain, check if we know it as a trusted cert or if it is signed by one of our trusted certs
            for(X509Certificate trustedCert : trustedSubjects) {
                if( Arrays.equals(trustedCert.getEncoded(), xcs[i].getEncoded())) {
                    try {
                        xcs[i].checkValidity(); // CertificateExpiredException, CertificateNotYetValidEception
                        return;
                    }
                    catch(Exception e) {
                        // this certificate is not a copy of xcs[i], but we continue to check other certificates
                    }
                }
            }
            //List<X509Certificate> trustedIssuers = repository.getCertificateForSubject(xcs[i].getIssuerX500Principal().getName());
            for(X509Certificate trustedIssuer : trustedIssuers) {
                System.out.println("- checking against trusted issuer: "+trustedIssuer.getSubjectX500Principal().getName());
                // check if the trusted issuer signed xcs[i]
                try {
                    xcs[i].verify(trustedIssuer.getPublicKey()); // NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException, CertificateException
                    xcs[i].checkValidity(); // CertificateExpiredException, CertificateNotYetValidEception
                    return;// XXX TODO   this works because if any of our trusted certs signed it, we're ok -  but we need to make sure there is a valid certificate PATH from xcs[0] to xcs[i] 
                }
                catch(Exception e) {
                    // this issuer did not sign xcs[i], but we continue checking other issuers
                }
            }
        }
        throw new CertificateException("Server certificate is not trusted");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return getIssuers(repository.getCertificates()).toArray(new X509Certificate[0]); //repository.getCertificateAuthorities().toArray(new X509Certificate[0]);
    }
    
    private List<X509Certificate> getSubjects(List<X509Certificate> certificates) {
        ArrayList<X509Certificate> subjectCerts = new ArrayList<X509Certificate>(certificates.size());
        for(X509Certificate cert : certificates) {
            if( !X509Util.isCA(cert) ) {
                subjectCerts.add(cert);
            }
        }
        return subjectCerts;
    }

    private List<X509Certificate> getIssuers(List<X509Certificate> certificates) {
        ArrayList<X509Certificate> caCerts = new ArrayList<X509Certificate>(certificates.size());
        for(X509Certificate cert : certificates) {
            if( X509Util.isCA(cert) ) {
                caCerts.add(cert);
            }
        }
        return caCerts;        
    }

    @Override
    public CertificateRepository getCertificateRepository() {
        return repository;
    }
    
    
}
