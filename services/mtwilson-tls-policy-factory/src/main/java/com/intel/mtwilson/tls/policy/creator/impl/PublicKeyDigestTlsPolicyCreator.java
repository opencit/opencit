/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.digest.DigestUtil;
import com.intel.dcsg.cpg.crypto.digest.UnsupportedAlgorithmException;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyDigestTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableDigestRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getCertificateDigestMetadata;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getCodecByName;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getCodecForData;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getFirst;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.guessAlgorithmForDigest;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyDigestTlsPolicyCreator implements TlsPolicyCreator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyDigestTlsPolicyCreator.class);
    
    
    @Override
    public PublicKeyDigestTlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
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
        if( "public-key-digest".equals(tlsPolicyDescriptor.getPolicyType())) {
            if( tlsPolicyDescriptor.getData() == null || tlsPolicyDescriptor.getData().isEmpty()  ) {
                throw new IllegalArgumentException("TLS policy descriptor does not contain any certificate digests");
            }
            ByteArrayCodec codec;
            PublicKeyDigestMetadata meta = getPublicKeyDigestMetadata(tlsPolicyDescriptor);
            // DEBU GONLY 
            try {
            ObjectMapper mapper = new ObjectMapper();
            log.debug("metadata is: {}", mapper.writeValueAsString(meta));
            } catch(Exception e) { }
            // DEBUG ONLY
            if( meta.digestEncoding == null ) {
                // attempt auto-detection based on first digest
                String sample = getFirst(tlsPolicyDescriptor.getData());
                codec = getCodecForData(sample);
                log.debug("getCodecForData: {}", codec);
            }
            else {
                String encoding = meta.digestEncoding;
                codec = getCodecByName(encoding);
                log.debug("getCodecByName: {}", codec);
            }
            if( codec == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates public key digests but does not declare digest encoding");
            }
            codec = new CertificateDigestTlsPolicyCreator.NormalizingCodec(codec);
            String alg;
            if( meta.digestAlgorithm == null ) {
                // attempt auto-detection based on first digest
                String sample = getFirst(tlsPolicyDescriptor.getData());
                byte[] hash = codec.decode(sample);
                alg = guessAlgorithmForDigest(hash);
            }
            else {
                alg = meta.digestAlgorithm;
            }
            if( alg == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates public key digests but does not declare digest algorithm");
            }
            alg = DigestUtil.getJavaAlgorithmName(alg);
            if( alg == null ) {
                throw new UnsupportedAlgorithmException(alg);
            }
            for(String publicKeyDigest : tlsPolicyDescriptor.getData()) {
                Digest digest = new Digest(alg, codec.decode(publicKeyDigest));
                repository.addDigest(digest);
            }
            return repository;
        }
        return null;
    }
    
    /**
     * 
     * @param tlsPolicyDescriptor
     * @return an instance of CertificateDigestMetadata, but some fields may be null if they were not included in the descriptor's meta section
     */
    public static PublicKeyDigestMetadata getPublicKeyDigestMetadata(TlsPolicyDescriptor tlsPolicyDescriptor) {
        PublicKeyDigestMetadata metadata = new PublicKeyDigestMetadata();
        if( tlsPolicyDescriptor.getMeta() == null ) {
            return metadata;
        }
        if( tlsPolicyDescriptor.getMeta().get("digestEncoding") != null && !tlsPolicyDescriptor.getMeta().get("digestEncoding").isEmpty() ) {
            metadata.digestEncoding = tlsPolicyDescriptor.getMeta().get("digestEncoding");
        }
        if( tlsPolicyDescriptor.getMeta().get("digestAlgorithm") != null && !tlsPolicyDescriptor.getMeta().get("digestAlgorithm").isEmpty() ) {
            metadata.digestAlgorithm = tlsPolicyDescriptor.getMeta().get("digestAlgorithm");
        }
        return metadata;
    }
    
    
    public static class PublicKeyDigestMetadata {
        public String digestAlgorithm;
        public String digestEncoding;
    }
}
