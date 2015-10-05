/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyDigestTlsPolicy extends PublicKeyTlsPolicy {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyDigestTlsPolicy.class);
    private DigestRepository repository;

    public PublicKeyDigestTlsPolicy(DigestRepository repository) {
        super(null, null, new ConfigurableProtocolSelector("TLS", "TLSv1.1", "TLSv1.2"));
        if( repository == null ) {
            throw new IllegalArgumentException("Repository is required"); // programmer error, throw an exception now so it's clear what happened instead of letting a NullPointerException be thrown during the SSL handshake when isCertificateTrusted is called
        }
        this.repository = repository;
    }

    @Override
    public boolean isPublicKeyTrusted(final X509Certificate publicKeyCertificate) {
//        Cache<String, byte[]> publicKeyDigests = new Cache<>();
        List<Digest> trustedPublicKeyDigests = repository.getDigests();
        for (Digest trustedPublicKeyDigest : trustedPublicKeyDigests) {
            try {
                final String algorithm = trustedPublicKeyDigest.getAlgorithm();
                // first check if we have a direct match on the public key
                byte[] publicKeyDigest = MessageDigest.getInstance(algorithm).digest(publicKeyCertificate.getPublicKey().getEncoded());
                if (Arrays.equals(trustedPublicKeyDigest.getBytes(), publicKeyDigest)) {
                    return true; // server public key is in our trusted public keys repository
                }
                // second check if we have a match on the public key certificate, so we can trust the public key is the same, but ignore the certificate details
                byte[] publicKeyCertificateDigest = MessageDigest.getInstance(algorithm).digest(publicKeyCertificate.getEncoded());
                if (Arrays.equals(trustedPublicKeyDigest.getBytes(), publicKeyCertificateDigest)) {
                    return true; // server public key is in our trusted public keys repository
                }
            } catch (Exception e) {
                log.debug("Cannot compare digest {} because algorithm {} is not available: {}", trustedPublicKeyDigest.toHex(), trustedPublicKeyDigest.getAlgorithm(), e.getMessage());
                continue; // if the algorithm we need to compare to that trusted digest is not available, then we have to say the input doesn't match
            }
        }
        return false;
    }
}