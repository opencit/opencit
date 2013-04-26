/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.TrustKnownCertificateTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.ArrayCertificateRepository;
import com.intel.dcsg.cpg.x509.repository.CertificateRepository;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;

/**
 * Use this factory to build a specific TlsPolicy based on
 * your requirements and available configuration.
 * 
 * If you turn off confidentiality, authentication, and integrity,
 * then you will get the InsecureTlsPolicy and all the connections will be insecure.
 * If you provide any certificate repository or trust delegate they will be ignored.
 * 
 * If you require confidentiality, authentication, and integrity (the default) but do not provide any
 * certificate repositories, it will not be possible to make any connections that satisfy those requirements
 * without known trusted certificates so all the connections will fail.
 * 
 * If you require confidentiality, authentication, and integrity (the default) and you provide
 * certificate repositories, but do not provide a confirmation delegate, then connections will succeed if
 * the server's certificate or chain can be validated using the specified repositories. If the server
 * cannot be verified the connection will fail.
 * 
 * If you require confidentiality, authentication, and integrity (the default) and you provide
 * certificate repositories, and you provide a confirmation delegate, then connections will succeed if
 * the server's certificate or chain can be validated using the specified repositories. If the server
 * cannot be verified the confirmation delegate will be invoked in order to ask if the server certificate
 * or root CA should be accepted. The delegate is then responsible for prompting the user or invoking
 * any other second channel in order to verify certificate fingerprints. If the delegate answers that
 * the certificate should be accepted the new certificate will be written to the provided mutable 
 * certificate repository and the connection will succeed. If the delegate declines, then the connection
 * will fail.
 * 
 * TODO Additional posisbilities that are currently not implemented:
 * Turn off confidentiality but require authentication and integrity - the TlsPolicy will attempt to use
 * https when possible but will also accept non-SSL http because encryption is not required. 
 * The policy will require a certificate repository & client certificate in order to sign all outgoing requests
 * and verify integrity of incoming requests.  It may be possible to specify the algorithm (HMAC-SHA256, RSA-SHA256, etc)
 * that is used to sign the messages - in the case an HMAC is used the repository will need to contain symmetric keys.
 * 
 * Most common usages are expected to be:
 * 
 * TlsPolicyBuilder.factory().insecure() -  developers temporarily turn off security checks because they can't be bothered to setup the
 *               certificate repositories
 * TlsPolicyBuilder.factory().strict(repository) - requires known certificates and fails w/o prompts if the server's certificate is unknown
 * TlsPolicyBuilder.factory().browser(repository, delegate) - typical browser behavior, uses a known certificate repository but
 *               prompts the user to accept new certificates if they are unknown
 * 
 * See also: 
 * http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#TrustManager
 * http://docs.oracle.com/javase/1.5.0/docs/guide/security/certpath/CertPathProgGuide.html
 * http://docs.oracle.com/javase/1.5.0/docs/api/javax/net/ssl/X509TrustManager.html
 * 
 * @author jbuhacoff
 */
public class TlsPolicyBuilder {
    private boolean providesConfidentiality = true;
    private boolean providesAuthentication = true;
    private boolean providesIntegrity = true;
    private CertificateRepository certificateRepository = null;
    private TrustDelegate trustDelegate = null;
    
    public TlsPolicyBuilder() { }
    
    public static TlsPolicyBuilder factory() { return new TlsPolicyBuilder(); }
    
    /**
     * Do not require confidentiality, authentication, or integrity.
     * Such connections are vulnerable to man-in-the-middle attacks.
     * @return 
     */
    public TlsPolicyBuilder insecure() {
        providesConfidentiality = false;
        providesAuthentication = false;
        providesIntegrity = false;  
        certificateRepository = null;
        trustDelegate = null;
        return this;
    }
    
    /**
     * Require confidentiality, authentication, and integrity. Provide
     * a repository of trusted certificates and do not allow any additions
     * to it - if a server certificate is not trusted it is rejected
     * immediately.
     * @param repository
     * @return 
     */
    public TlsPolicyBuilder strict(CertificateRepository repository) {
        providesConfidentiality = true;
        providesAuthentication = true;
        providesIntegrity = true;  
        certificateRepository = repository;
        trustDelegate = null;
        return this;
    }
    
    /**
     * Require confidentiality, authentication, and integrity. Provide
     * a repository of trusted certificates and a delegate to confirm
     * fingerprints of unknown certificates (and optionally add them to
     * the trusted certificates repository -- that's up to the delegate).
     * @param repository
     * @param delegate
     * @return 
     */
    public TlsPolicyBuilder browser(CertificateRepository repository, TrustDelegate delegate) {
        providesConfidentiality = true;
        providesAuthentication = true;
        providesIntegrity = true;  
        certificateRepository = repository;
        trustDelegate = delegate;
        return this;
    }
    
    /**
     * Require a policy that provides confidentiality. This is the default.
     * @return 
     */
    public TlsPolicyBuilder providesConfidentiality() {
        providesConfidentiality = true;
        return this;
    }

    /**
     * Do not require a policy that provides confidentiality.
     * @return 
     */
    public TlsPolicyBuilder noConfidentiality() {
        providesConfidentiality = false;
        return this;
    }

    /**
     * Require a policy that provides authentication. This is the default.
     * 
     * Specify a read-only repository of certificates. If your trusted certificates are in
     * multiple locations (for example a global trusted root CA list and a per-host
     * known certificate list) you can provide an aggregate repository instance that combines them.
     * repository.
     * 
     * If you want to allow the user to accept untrusted certificates "just this once" or "always", then
     * you need to also set a trust delegate to handle it.  See the trustDelegate method.
     * 
     * @param repository must not be null; you cannot authenticate a server without a list of trusted identities or CA's
     * @return 
     */
    public TlsPolicyBuilder providesAuthentication(CertificateRepository repository) {
        providesAuthentication = true;
        certificateRepository = repository;
        return this;
    }

    /**
     * Do not require a policy that provides authentication.
     * 
     * XXX TODO this may be confusing... are we enforcing SERVER authentication or CLIENT authentication to the server?
     * Naturally it seems we are enforcing that the SERVER authenticate to the CLIENT,  because the user of this class
     * is the client so it doesn't make sense for the client to enforce that it authenticates itself to the server... 
     * the server will demand it if it's required. So to support client authentication you only have to call the method
     * that allows you to provide a client keystore with an RSA key or HMAC , whatever the server needs. 
     * @return 
     */
    public TlsPolicyBuilder noAuthentication() {
        providesAuthentication = false;
        return this;
    }

    /**
     * Require a policy that provides message integrity. This is the default.
     * @return 
     */
    public TlsPolicyBuilder providesIntegrity() {
        providesIntegrity = true;
        return this;
    }

    /**
     * Do not require a policy that provides message integrity.
     * @return 
     */
    public TlsPolicyBuilder noIntegrity() {
        providesIntegrity = false;
        return this;
    }
    
    public TlsPolicyBuilder javaTrustStore() {
        try {
            certificateRepository = new KeystoreCertificateRepository(System.getProperty("javax.net.ssl.trustStore"), System.getProperty("javax.net.ssl.trustStorePassword"));
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Cannot load javax.net.ssl.trustStore from "+System.getProperty("javax.net.ssl.trustStore")+": "+e.toString());
        }
        return this;
    }
        
    /**
     * Specify a delegate (callback) implementation to ask if a new unknown certificate should
     * be accepted. THe delegate should prompt the user or use some other second channel to 
     * confirm the certificate fingerprint before accepting it.
     * The delegate may have access to a writable repository of certificates. This means if a server certificate or CA
     * is not present in the repository, and if the trust delegate has determined it should accept the certificate
     * (for example by asking the user) then it can choose to save it into any available mutable certificate repository,
     * or even the one that was passed into the trustedCertificates method.
     * @param repository may be null to clear a previously set value
     * @return 
     */
    public TlsPolicyBuilder trustDelegate(TrustDelegate delegate) {
        trustDelegate = delegate;
        return this;
    }
    
// TODO need to combine "known cert" with "trust root and verify hostname" somehow... problem is the hostname verifir and cert checker are used independently !!!    
    public TlsPolicy build() {
        if( !providesConfidentiality && !providesAuthentication && !providesIntegrity ) {
            return new InsecureTlsPolicy();
        }
        if( providesAuthentication && certificateRepository == null ) {
            certificateRepository = ArrayCertificateRepository.EMPTY;
        }
        if( providesConfidentiality && providesAuthentication && providesIntegrity ) {
            if( trustDelegate == null ) {
                return new TrustKnownCertificateTlsPolicy(certificateRepository); // strict mode - trust only certificates in repository and reject all others; do not prompt to accept new certificates.... if certificate repository was set to null, then all connections will be rejected!
            }
            if( trustDelegate != null ) {
                return new TrustKnownCertificateTlsPolicy(certificateRepository, trustDelegate); // browser mode with initial set of trusted certificates                
            }
        }
        if( !providesConfidentiality && providesAuthentication && providesIntegrity ) {
            throw new UnsupportedOperationException("No support for authentication and integrity without encryption"); // TODO see below on implementing signing of messages, possibly with an Authentication (in contrast to Authorziation) header but using http instead of https,   either with RSA or HMAC.  
        }
        if( providesConfidentiality && !providesAuthentication && providesIntegrity ) {
            throw new UnsupportedOperationException("No support for encryption and integrity without authentication"); // TODO is there a way to provide an encrypted channel without server authentication? it would be vulnerable to MITM attacks unless the encryption keys are shared in advance. so probably that has to be a requirement - cannot use SSL protocl or DH handshake, must use pre-shared encryption keys.
        }
        if( providesConfidentiality && providesAuthentication && !providesIntegrity ) {
            throw new UnsupportedOperationException("No support for encryption and authentication without integrity"); // TODO if the channel is encrypted and authenticated, shouldn't it also have integrity?? is there any example of a channel that is encrypted & authenticated but does not do any message integrity checks?  
        }
        if( !providesConfidentiality && !providesAuthentication && providesIntegrity ) {
            throw new UnsupportedOperationException("No support for integrity without authentication or encryption"); // only because we don't have a standard protocol right now to handle it... it's basically a checksum using HMAC (so that an attacker can't change the message AND the checksum to match) ... but without authentication it means the server may share an hmac key with multiple clients, so the server doesn't necessarily know WHO is sending it.  
        }
        if( !providesConfidentiality && providesAuthentication && !providesIntegrity ) {
            throw new UnsupportedOperationException("No support for authentication without integrity or encryption"); // only because we don't have a standard protocol right now to handle it
        }
        if( providesConfidentiality && !providesAuthentication && !providesIntegrity ) {
            throw new UnsupportedOperationException("No support for confidentiality without authentication or integrity"); // only because we don't have a standard protocol right now to handle it
        }
        /*
        * TODO Additional posisbilities that are currently not implemented:
        * Turn off confidentiality but require authentication and integrity - the TlsPolicy will attempt to use
        * https when possible but will also accept non-SSL http because encryption is not required. 
        * The policy will require a certificate repository & client certificate in order to sign all outgoing requests
        * and verify integrity of incoming requests.  It may be possible to specify the algorithm (HMAC-SHA256, RSA-SHA256, etc)
        * that is used to sign the messages - in the case an HMAC is used the repository will need to contain symmetric keys.
         */
        // if we haven't identified a policy by now, the caller did something wrong
        throw new UnsupportedOperationException("The specified requirements are not supported");
    }
}
