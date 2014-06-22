/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.digest.DigestUtil;
import com.intel.dcsg.cpg.crypto.digest.UnsupportedAlgorithmException;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyDigestTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableDigestRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyDigestTlsPolicyCreator implements TlsPolicyCreator {

    
    @Override
    public TlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "public-key-digest".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            try {
                DigestRepository repository = getPublicKeyDigestRepository(tlsPolicyDescriptor);
                return new PublicKeyDigestTlsPolicy(repository);
            }
            catch(CryptographyException e) {
                throw new IllegalArgumentException("Cannot create public key digest policy from given repository", e);
            }
        }
        return null;
    }
    
    private DigestRepository getPublicKeyDigestRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CryptographyException {
        HashSetMutableDigestRepository repository = new HashSetMutableDigestRepository();
        if( "public-key-digest".equals(tlsPolicyDescriptor.getPolicyType()) && tlsPolicyDescriptor.getData() != null && tlsPolicyDescriptor.getMeta() != null ) {
            if( tlsPolicyDescriptor.getMeta().get("digestAlgorithm") == null || tlsPolicyDescriptor.getMeta().get("digestAlgorithm").isEmpty() ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates public key digests but does not declare digest algorithm");
            }
            String alg = DigestUtil.getJavaAlgorithmName(tlsPolicyDescriptor.getMeta().get("digestAlgorithm"));
            if( alg == null ) {
                throw new UnsupportedAlgorithmException(alg);
            }
            for(String publicKeyDigestBase64 : tlsPolicyDescriptor.getData()) {
                Digest digest = new Digest(alg, Base64.decodeBase64(publicKeyDigestBase64));
                repository.addDigest(digest);
            }
            return repository;
        }
        return null;
    }
    
    public static class PublicKeyDigestMetadata {
        public String digestAlgorithm;
        public String digestEncoding;
    }
}
