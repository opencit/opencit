/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class CertificateDigestTlsPolicy extends CertificateTlsPolicy {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateDigestTlsPolicy.class);

    private DigestRepository repository;

    public CertificateDigestTlsPolicy(DigestRepository repository) {
        super(null, null, new ConfigurableProtocolSelector("TLS", "TLSv1.1", "TLSv1.2"));
        if( repository == null ) {
            throw new IllegalArgumentException("Repository is required"); // programmer error, throw an exception now so it's clear what happened instead of letting a NullPointerException be thrown during the SSL handshake when isCertificateTrusted is called
        }
        this.repository = repository;
    }

    @Override
    public boolean isCertificateTrusted(X509Certificate certificate) {
        log.debug("CertificateDigestTlsPolicy isCertificateTrusted: {}", certificate.getSubjectX500Principal().getName());
        List<Digest> trustedCertificateDigests = repository.getDigests();
        log.debug("CertificateDigestTlsPolicy isCertificateTrusted: {} trusted digests", trustedCertificateDigests.size());
        for (Digest trustedCertificateDigest : trustedCertificateDigests) {
            try {
                byte[] peerCertificateDigest = MessageDigest.getInstance(trustedCertificateDigest.getAlgorithm()).digest(certificate.getEncoded());
                log.debug("Peer certificate digest:  {}", peerCertificateDigest);
                log.debug("Trust certificate digest: {}", trustedCertificateDigest.getBytes());
                if (Arrays.equals(trustedCertificateDigest.getBytes(), peerCertificateDigest)) {
                    log.debug("looks the same, returning true");
                    return true; // server public key is in our trusted public keys repository
                }
            } catch (NoSuchAlgorithmException  e) {
                log.debug("Cannot compare digest {} because algorithm {} is not available: {}", trustedCertificateDigest.toHex(), trustedCertificateDigest.getAlgorithm(), e.getMessage());
                continue; // if the algorithm we need to compare to that trusted digest is not available, then we have to say the input doesn't match
            }
            catch(CertificateEncodingException e) {
                log.debug("Cannot compare digest {} because peer certificate cannot be encoded: {}", trustedCertificateDigest.toHex(), e.getMessage());
                continue; // if the algorithm we need to compare to that trusted digest is not available, then we have to say the input doesn't match
            }
        }
        log.debug("no match, returning false");
        return false;
    }
    
    /**
     * A public key certificate alone does not provide enough information
     * to determine if we trust its issuer; the CertificateTlsPolicy 
     * @param certificate
     * @return 
     */
    @Override
    public boolean isCertificateIssuerTrusted(X509Certificate certificate) {
        return false;
    }
}
