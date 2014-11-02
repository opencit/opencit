/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy.impl;

import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.tls.policy.TrustDelegate;
import com.intel.dcsg.cpg.x509.repository.MutablePublicKeyRepository;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * You may initialize this trust delegate with a mutable public key repository
 * to which new public keys can be added (with user permission).  
 * 
 * This delegate may not be initialized without a repository because that would
 * mean every public key is a "first public key" and therefore would be trusted,
 * which is equivalent to an insecure policy that doesn't require a delegate,
 * but most importantly will not be able to implement the purpose of this delegate
 * which is to only trust a public key if the repository is empty and then add it
 * to the repository.
 * 
 * Difference between this class and FirstCertificateTrustDelegate is that this
 * class does not validate the certificate at all and only stores the public key.
 * 
 * @author jbuhacoff
 */
public class FirstPublicKeyTrustDelegate implements TrustDelegate {
    private Logger log = LoggerFactory.getLogger(getClass());
    private transient final MutablePublicKeyRepository repository;

    public FirstPublicKeyTrustDelegate(MutablePublicKeyRepository repository) {
        if( repository == null ) { throw new NullPointerException("Public key repository must be provided"); }
        this.repository = repository;
    }

    /**
     *
     * @param certificate
     * @return
     */
    @Override
    public boolean acceptUnknownCertificate(X509Certificate certificate) {
        // basic check that the public key certificate format is something we can work with
        String digest = Sha256Digest.digestOf(certificate.getPublicKey().getEncoded()).toHexString();
        log.debug("acceptUnknownCertificate SHA256 Fingerprint: {}", digest);
        
        // trust first certificate means only accept a new certificate automatically if the repository is empty
        List<PublicKey> list = repository.getPublicKeys();
        if( list.isEmpty() ) {
            try {
                repository.addPublicKey(certificate.getPublicKey());
                return true;
            }
            catch(Exception e) {
                // Not being able to save the public key is  fatal error for the FirstPublicKeyTrustDelegate because it means we will trust all public keys equivalent to InsecureTlsPolicy, which is not what the program configured. 
                log.error("Cannot save public key", e);
                return false; 
            }
        }
        log.info("Rejecting untrusted server public key; repository already has {} trusted public keys", list.size());
        return false;
    }

}
