/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyException;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import java.net.Socket;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * Replacement for TrustKnownCertificateTlsPolicy which compares the public keys
 * only and ignores the certificate. Use this policy implementation with
 * per-host trusted public keys. When used with a certificate repository the
 * public keys are extracted from certificates for the comparison; certificate
 * authorities and hostname verification are ignored.
 *
 * Reference:
 * http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509ExtendedTrustManager.html
 * 
 * @author jbuhacoff
 */
public class PublicKeyTlsPolicy extends X509ExtendedServerTrustManager implements TlsPolicy {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyTlsPolicy.class);
    private static final AllowAllHostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
    private transient final PublicKeyRepository repository;
    private transient final TrustDelegate delegate;
    private transient final ProtocolSelector selector;
//    private transient X509Certificate trustedCertificate = null;
    
    public PublicKeyTlsPolicy(PublicKeyRepository repository) {
        this.repository = repository;
        this.delegate = null;
        this.selector = new ConfigurableProtocolSelector("TLS", "TLSv1.1", "TLSv1.2"); // default to any version of TLS
    }

    public PublicKeyTlsPolicy(PublicKeyRepository repository, TrustDelegate delegate) {
        this.repository = repository;
        this.delegate = delegate;
        this.selector = new ConfigurableProtocolSelector("TLS", "TLSv1.1", "TLSv1.2"); // default to any version of TLS
    }

    public PublicKeyTlsPolicy(PublicKeyRepository repository, TrustDelegate delegate, ProtocolSelector selector) {
        this.repository = repository;
        this.delegate = delegate;
        this.selector = selector;
    }

    public PublicKeyTlsPolicy(PublicKeyRepository repository, ProtocolSelector selector) {
        this.repository = repository;
        this.delegate = null;
        this.selector = selector;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        log.debug("getAcceptedIssuers");
        return new X509Certificate[0];
    }

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

    protected PublicKeyRepository getRepository() {
        return repository;
    }

    public boolean isPublicKeyTrusted(final X509Certificate publicKeyCertificate) {
        List<PublicKey> trustedPublicKeys = repository.getPublicKeys();
        log.debug("isPublicKeyTrusted: checking against {} trusted certificates", trustedPublicKeys.size());
        try {
            log.debug("isPublicKeyTrusted: sha1 {}", Sha1Digest.digestOf(publicKeyCertificate.getEncoded()));
            for (PublicKey trustedPublicKey : trustedPublicKeys) {
                log.debug("isPublicKeyTrusted: trusted sha1 {}", Sha1Digest.digestOf(trustedPublicKey.getEncoded()));
                if (Arrays.equals(trustedPublicKey.getEncoded(), publicKeyCertificate.getPublicKey().getEncoded())) {
                    return true; // server public key is in our trusted public keys repository
                }
            }
        } catch (Exception e) {
            log.debug("Cannot check if public key certificate is trusted: {}", publicKeyCertificate.getSubjectX500Principal().getName(), e);
        }
        
        return false;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String authType) throws CertificateException {
        log.debug("checkServerTrusted");
        if (xcs == null || xcs.length == 0) {
            throw new TlsPolicyException("Server did not present any certificates", null, this, xcs);
        }
        // we don't check the validity of the certificate because a public key
        // policy isn't aware of any trusted certificate authorities to be able
        // to trust anything that is in the server certificate anyway - we only
        // check the server public key against a list of trusted public keys.
        // we check only the server public key and ignore its certificate issuer chain.
        if (isPublicKeyTrusted(xcs[0])) {
            return;
        }
        // we did not find a trusted public key... if a delegate is available, we can ask the user
        if (delegate != null) {
            if (delegate.acceptUnknownCertificate(xcs[xcs.length - 1])) {
                return; // user accepted the unknown certificate
            }
        }
        throw new CertificateException("Server certificate is not trusted");
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return this;
    }

    @Override
    public X509HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }
}
