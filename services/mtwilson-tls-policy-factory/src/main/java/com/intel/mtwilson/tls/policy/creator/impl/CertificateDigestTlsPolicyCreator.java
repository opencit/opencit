/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.codec.Base64Codec;
import com.intel.dcsg.cpg.codec.Base64Util;
import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.codec.HexCodec;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.crypto.digest.DigestUtil;
import com.intel.dcsg.cpg.crypto.digest.UnsupportedAlgorithmException;
import com.intel.dcsg.cpg.codec.HexUtil;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateDigestTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.DigestRepository;
import com.intel.dcsg.cpg.x509.repository.HashSetMutableDigestRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
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
            ByteArrayCodec codec;
            CertificateDigestMetadata meta = getCertificateDigestMetadata(tlsPolicyDescriptor);
            if( meta.digestEncoding == null ) {
                // attempt auto-detection based on first digest
                String sample = getFirst(tlsPolicyDescriptor.getData());
                codec = getCodecForData(sample);
                log.debug("Codec {} for sample data {}", (codec==null?"null":codec.getClass().getName()), sample);
            }
            else {
                String encoding = meta.digestEncoding;
                codec = getCodecByName(encoding);
                log.debug("Codec {} for digest encoding {}", (codec==null?"null":codec.getClass().getName()), encoding);
            }
            if( codec == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates certificate digests but does not declare digest encoding");
            }
            String alg;
            if( meta.digestAlgorithm == null ) {
                // attempt auto-detection based on first digest
                String sample = getFirst(tlsPolicyDescriptor.getData());
                byte[] hash = codec.decode(sample);
                alg = guessAlgorithmForDigest(hash);
                log.debug("Algorithm {} for sample data {} decoded length {}", alg, sample, hash.length);
            }
            else {
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
    
    public static String guessAlgorithmForDigest(byte[] hash) {
        if( hash.length == 16 ) { return "MD5"; }
        if( hash.length == 20 ) { return "SHA-1"; }
        if( hash.length == 32 ) { return "SHA-256"; }
        if( hash.length == 48 ) { return "SHA-384"; }
        if( hash.length == 64 ) { return "SHA-512"; }
        return null;
    }
    
    /**
     * Utility function to get a sample item from a collection
     * @param collection
     * @return the first item from the collection, or null if the collection is empty
     */
    public static String getFirst(Collection<String> collection) {
        Iterator<String> it = collection.iterator();
        if( it.hasNext() ) {
            return it.next();
        }
        return null;
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
        if( tlsPolicyDescriptor.getMeta().get("digestEncoding") != null && !tlsPolicyDescriptor.getMeta().get("digestEncoding").isEmpty() ) {
            metadata.digestEncoding = tlsPolicyDescriptor.getMeta().get("digestEncoding");
        }
        if( tlsPolicyDescriptor.getMeta().get("digestAlgorithm") != null && !tlsPolicyDescriptor.getMeta().get("digestAlgorithm").isEmpty() ) {
            metadata.digestAlgorithm = tlsPolicyDescriptor.getMeta().get("digestAlgorithm");
        }
        return metadata;
    }
    
    /**
     * Utility function to instantiate a codec by name
     * @param encoding "base64" or "hex"
     * @return new codec instance or null if the encoding name is not recognized
     */
    public static ByteArrayCodec getCodecByName(String encoding) {
        if( encoding.equalsIgnoreCase("base64")) {
            return new Base64Codec();
        }
        else if( encoding.equalsIgnoreCase("hex")) {
            return new HexCodec();
        }
        else {
            return null;
        }
    }
    
    /**
     * Utility function to detect if the sample is base64-encoded or
     * hex-encoded and return a new instance of the appropriate codec. 
     * If the sample
     * encoding cannot be detected, this method will return null.
     * @param sample of data either base64-encoded or hex-encoded
     * @return a new codec instance or null if the encoding is not recognized
     */
    public static ByteArrayCodec getCodecForData(String sample) {
        log.debug("getCodecForData: {}", sample);
        String printable = sample.replaceAll("[^\\p{Print}]", ""); // remove all non-printable characters; for example if someone copies a hex-encoded certificate "thumbprint" from a certificate information box it might be preceded by a non-printable character
        String hex = HexUtil.trim(printable); // important to remove only whitespace here and not ALL non-hex characters (because then isHex will almost always return true)
        if( HexUtil.isHex(hex)) {
            log.debug("getCodecForData hex: {}", hex);
            HexCodec codec = new HexCodec(); // encoding = "hex";
            codec.setNormalizeInput(true); // automatically remove non-hex characters before decoding
            return codec;
        }
        String base64 = Base64Util.trim(printable); // important to remove only whitespace here and not ALL non-hex characters (because then isBase64 will almost always return true)
        if( Base64Util.isBase64(base64) ) {
            log.debug("getCodecForData base64: {}", base64);
            Base64Codec codec = new Base64Codec(); // encoding = "base64";
            codec.setNormalizeInput(true); // automatically remove non-base64 characters before decoding
            return codec;
        }
        return null;
    }
}
