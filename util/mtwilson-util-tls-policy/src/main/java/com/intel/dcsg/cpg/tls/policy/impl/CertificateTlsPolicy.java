/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyException;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
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
 * Reference:
 * http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509ExtendedTrustManager.html
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
public class CertificateTlsPolicy extends X509ExtendedServerTrustManager implements TlsPolicy {

    private Logger log = LoggerFactory.getLogger(getClass());
    private transient final CertificateRepository repository;
    private transient final TrustDelegate delegate;
    private transient final ProtocolSelector selector;

    public CertificateTlsPolicy(CertificateRepository repository) {
        this.repository = repository;
        this.delegate = null;
        this.selector = new ConfigurableProtocolSelector("TLS", "TLSv1.1", "TLSv1.2"); // default to any version of TLS
    }

    public CertificateTlsPolicy(CertificateRepository repository, TrustDelegate delegate) {
        this.repository = repository;
        this.delegate = delegate;
        this.selector = new ConfigurableProtocolSelector("TLS", "TLSv1.1", "TLSv1.2"); // default to any version of TLS
    }

    public CertificateTlsPolicy(CertificateRepository repository, TrustDelegate delegate, ProtocolSelector selector) {
        this.repository = repository;
        this.delegate = delegate;
        this.selector = selector;
    }

    public CertificateTlsPolicy(CertificateRepository repository, ProtocolSelector selector) {
        this.repository = repository;
        this.delegate = null;
        this.selector = selector;
    }

    @Override
    public X509ExtendedServerTrustManager getTrustManager() {
        return this;
    }

    @Override
    public X509HostnameVerifier getHostnameVerifier() {
        return new StrictHostnameVerifier();
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    /*
     @Override
     public CertificateRepository getCertificateRepository() {
     return repository;
     }
     */
    @Override
    public ProtocolSelector getProtocolSelector() {
        return selector;
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

    protected CertificateRepository getRepository() {
        return repository;
    }
    
    public boolean isCertificateTrusted(X509Certificate certificate) {
            List<X509Certificate> trustedCertificates = repository.getCertificates(); //repository.getCertificateForSubject(xcs[i].getSubjectX500Principal().getName());  
            log.debug("isCertificateTrusted: checking against {} trusted certificates", trustedCertificates.size());
            try {
                log.debug("isCertificateTrusted: sha1 {}", Sha1Digest.digestOf(certificate.getEncoded()));
                for (X509Certificate trustedCert : trustedCertificates) {
                    log.debug("isCertificateTrusted: trusted sha1 {}", Sha1Digest.digestOf(trustedCert.getEncoded()));
                    if (Arrays.equals(trustedCert.getEncoded(), certificate.getEncoded())) {
                        return true; // certificate appears in the trusted repository, so we trust it and anything that it signed
                    }
                }
            } catch (Exception e) {
                log.debug("Cannot check if certificate is trusted: {}", certificate.getSubjectX500Principal().getName(), e);
            }
            return false;
    }
    
    public boolean isCertificateIssuerTrusted(X509Certificate certificate) {
            List<X509Certificate> trustedCertificates = repository.getCertificates(); //repository.getCertificateForSubject(xcs[i].getSubjectX500Principal().getName());  
            log.debug("isCertificateIssuerTrusted: checking against {} trusted certificates", trustedCertificates.size());
        for (X509Certificate trustedCert : trustedCertificates) {
                log.debug("isCertificateIssuerTrusted: issuer {}", certificate.getIssuerX500Principal());
            if (Arrays.equals(trustedCert.getSubjectX500Principal().getEncoded(), certificate.getIssuerX500Principal().getEncoded())) {
                log.debug("isCertificateIssuerTrusted: trusted issuer {}", trustedCert.getIssuerX500Principal());
                try {
                    certificate.verify(trustedCert.getPublicKey());
                    return true; // a trusted certificate has signed the last certificate in the server's chain
                } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                    log.debug("Trusted certificate {} looks like issuer of {} but could not be verified: {}", new Object[]{trustedCert.getSubjectX500Principal().getName(), certificate.getSubjectX500Principal().getName(), e.toString()});
                }
            }
        }
        return false;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        if (xcs == null || xcs.length == 0) {
            throw new TlsPolicyException("Server did not present any certificates", null, this, xcs);
        }
        log.debug("Server presented a certificate chain of length {} with authentication type {}", xcs.length, string);
        // whether we got a single certificate or a chain, ensure all certs are within their validity period
        for (int i = 0; i < xcs.length; i++) {
            try {
                xcs[i].checkValidity(); // CertificateExpiredException, CertificateNotYetValidException
            } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                throw new CertificateException("Server certificate is invalid: " + xcs[i].getSubjectX500Principal().getName() + ": " + e.toString());
            }
        }
        // if we got a chain, ensure that each certificate in the chain was signed by the next certificate (except for the last one which doesn't have a next certificate)
        for (int i = 0; i < xcs.length - 1; i++) {
            try {
                xcs[i].verify(xcs[i + 1].getPublicKey()); // throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                throw new CertificateException("Server certificate chain is invalid: cannot verify that " + xcs[i + 1].getSubjectX500Principal().getName() + " signed " + xcs[i].getSubjectX500Principal().getName() + ": " + e.toString());
            }
        }
        
        // whether we got one certificate or a chain, check if any are trusted directly
        for (int i = 0; i < xcs.length; i++) {
            if( isCertificateTrusted(xcs[i])) {
                return;
            }
        }
        // now check if the last certificate in the chain was SIGNED BY any certificate in our trusted repository
        if( isCertificateIssuerTrusted(xcs[xcs.length-1])) {
            return;
        }
        // we did not find a trusted certificate... if a delegate is available, we can ask the user
        if (delegate != null) {
            if (delegate.acceptUnknownCertificate(xcs[xcs.length - 1])) {
                return; // user accepted the unknown certificate
            }
        }
        throw new CertificateException("Server certificate is not trusted");
        
    }

}
