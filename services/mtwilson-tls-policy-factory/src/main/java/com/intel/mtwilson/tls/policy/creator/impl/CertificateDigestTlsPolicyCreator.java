/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.mtwilson.codec.Base64Codec;
import com.intel.mtwilson.codec.Base64Util;
import com.intel.mtwilson.codec.ByteArrayCodec;
import com.intel.mtwilson.codec.HexCodec;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.digest.DigestUtil;
import com.intel.dcsg.cpg.crypto.digest.UnsupportedAlgorithmException;
import com.intel.mtwilson.codec.HexUtil;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableDigestRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class CertificateDigestTlsPolicyCreator implements TlsPolicyCreator{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateDigestTlsPolicyCreator.class);
    
    @Override
    public CertificateDigestTlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "certificate-digest".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            try {
                DigestRepository repository = getCertificateDigestRepository(tlsPolicyDescriptor);
                return new CertificateDigestTlsPolicy(repository);
            }
            catch(CryptographyException e) {
                throw new IllegalArgumentException("Cannot create certificate digest policy from given repository", e);
            }
        }
        return null; // IAW TlsPolicyCreator interface, returning null means the given policy type is not supported so caller can try a different creator
    }
    

    private DigestRepository getCertificateDigestRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CryptographyException {
        HashSetMutableDigestRepository repository = new HashSetMutableDigestRepository();
        if( "certificate-digest".equals(tlsPolicyDescriptor.getPolicyType()) ) {
            if( tlsPolicyDescriptor.getData() == null || tlsPolicyDescriptor.getData().isEmpty()  ) {
                throw new IllegalArgumentException("TLS policy descriptor does not contain any certificate digests");
            }
            log.debug("getCertificateDigestRepository policyType: {}", tlsPolicyDescriptor.getPolicyType());
            log.debug("getCertificateDigestRepository policyType: {}", tlsPolicyDescriptor.getMeta().get("digestEncoding"));
            log.debug("getCertificateDigestRepository policyType: {}", tlsPolicyDescriptor.getMeta().get("digestAlgorithm"));
            ByteArrayCodec codec;
            CertificateDigestMetadata meta = getCertificateDigestMetadata(tlsPolicyDescriptor);
            if( meta.digestEncoding == null ) {
                // attempt auto-detection based on first digest
                String sample = TlsPolicyFactoryUtil.getFirst(tlsPolicyDescriptor.getData());
                meta.digestEncoding = TlsPolicyFactoryUtil.guessEncodingForData(sample);
                log.debug("Guessing codec {} for sample data {}", meta.digestEncoding, sample);
            }
            codec = TlsPolicyFactoryUtil.getCodecByName(meta.digestEncoding); // safe because if input is null return value will be null
            if( codec == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates certificate digests but does not declare digest encoding");
            }
            log.debug("Codec {} for digest encoding {}", codec.getClass().getName(), meta.digestEncoding);
            String alg;
            if( meta.digestAlgorithm == null || meta.digestAlgorithm.isEmpty() ) {
                log.debug("Guessing algorithm for sample data");
                // attempt auto-detection based on first digest
                String sample = TlsPolicyFactoryUtil.getFirst(tlsPolicyDescriptor.getData());
                byte[] hash = codec.decode(sample);
                alg = TlsPolicyFactoryUtil.guessAlgorithmForDigest(hash);
                log.debug("Algorithm {} for sample data {} decoded length {}", alg, sample, hash.length);
            }
            else {
                log.debug("Using specified algorithm for sample data {}", meta.digestAlgorithm);
                alg = meta.digestAlgorithm;
            }
            if( alg == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates certificate digests but does not declare digest algorithm");
            }
            alg = DigestUtil.getJavaAlgorithmName(alg);
            if( alg == null ) {
                throw new UnsupportedAlgorithmException(alg);
            }
            for(String certificateDigest : tlsPolicyDescriptor.getData()) {
                Digest digest = new Digest(alg, codec.decode(certificateDigest));
                repository.addDigest(digest);
            }
            return repository;
        }
        throw new UnsupportedOperationException("TLS policy type must be 'certificate-digest'");
    }    
    
    public static class CertificateDigestMetadata {
        public String digestAlgorithm;
        public String digestEncoding;
    }
    
    /**
     * 
     * @param tlsPolicyDescriptor
     * @return an instance of CertificateDigestMetadata, but some fields may be null if they were not included in the descriptor's meta section
     */
     public static CertificateDigestMetadata getCertificateDigestMetadata(TlsPolicyDescriptor tlsPolicyDescriptor) {
        CertificateDigestMetadata metadata = new CertificateDigestMetadata();
        if( tlsPolicyDescriptor.getMeta() == null ) {
            return metadata;
        }
        if(tlsPolicyDescriptor.getMeta() == null) {
            throw new IllegalArgumentException("TLS policy descriptor metadata cannot be null.");
        }
        if( tlsPolicyDescriptor.getMeta().get("digestEncoding") != null && !tlsPolicyDescriptor.getMeta().get("digestEncoding").isEmpty() ) {
            metadata.digestEncoding = tlsPolicyDescriptor.getMeta().get("digestEncoding");
        }
        if( tlsPolicyDescriptor.getMeta().get("digestAlgorithm") != null && !tlsPolicyDescriptor.getMeta().get("digestAlgorithm").isEmpty() ) {
            metadata.digestAlgorithm = tlsPolicyDescriptor.getMeta().get("digestAlgorithm");
        }
        return metadata;
    }
}
