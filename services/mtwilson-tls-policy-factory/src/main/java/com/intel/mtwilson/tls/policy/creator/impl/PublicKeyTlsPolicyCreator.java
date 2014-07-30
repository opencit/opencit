/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.dcsg.cpg.crypto.PublicKeyCodec;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.security.PublicKey;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactoryUtil;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyTlsPolicyCreator implements TlsPolicyCreator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyTlsPolicyCreator.class);
    private PublicKeyCodec publicKeyCodec = new PublicKeyCodec();
    
    @Override
    public PublicKeyTlsPolicy createTlsPolicy(TlsPolicyDescriptor tlsPolicyDescriptor) {
        if( "public-key".equalsIgnoreCase(tlsPolicyDescriptor.getPolicyType()) ) {
            try {
                PublicKeyRepository repository = getPublicKeyRepository(tlsPolicyDescriptor);
                return new PublicKeyTlsPolicy(repository); //return TlsPolicyBuilder.factory().strict(repository).skipHostnameVerification().build();
            }
            catch(CryptographyException e) {
                throw new IllegalArgumentException("Cannot create public key policy from given repository", e);
            }
        }
        return null;
    }
    
    protected PublicKeyRepository getPublicKeyRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CryptographyException {
        if( "public-key".equals(tlsPolicyDescriptor.getPolicyType()) && tlsPolicyDescriptor.getData() != null  ) {
            if( tlsPolicyDescriptor.getData() == null || tlsPolicyDescriptor.getData().isEmpty()  ) {
                throw new IllegalArgumentException("TLS policy descriptor does not contain any public keys");
            }
            ByteArrayCodec codec = getCodecForTlsPolicyDescriptor(tlsPolicyDescriptor);
            if( codec == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates public keys but does not declare public key encoding");
            }
            HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
            for(String publicKeyEncoded : tlsPolicyDescriptor.getData()) {
                PublicKey publicKey = publicKeyCodec.decode(codec.decode(publicKeyEncoded));
                repository.addPublicKey(publicKey);
            }
            return repository;
        }
        return null;
    }
        
    protected ByteArrayCodec getCodecForTlsPolicyDescriptor(TlsPolicyDescriptor tlsPolicyDescriptor) {
        ByteArrayCodec codec;
        PublicKeyMetadata meta = getPublicKeyMetadata(tlsPolicyDescriptor);
        if( meta.encoding == null ) {
            // attempt auto-detection based on first certificate
            String sample = TlsPolicyFactoryUtil.getFirst(tlsPolicyDescriptor.getData());
            meta.encoding = TlsPolicyFactoryUtil.guessEncodingForData(sample); // safe because if input is null return value will be null
            log.debug("Guessing codec {} for sample data {}", meta.encoding, sample);
        }
        codec = TlsPolicyFactoryUtil.getCodecByName(meta.encoding); // safe because if input is null return value will be null
        log.debug("Codec {} for cerrtificate encoding {}", (codec==null?"null":codec.getClass().getName()), meta.encoding);
        return codec;
    }
    
    
    public static class PublicKeyMetadata {
        public String encoding; // base64
    }

    /**
     * 
     * @param tlsPolicyDescriptor
     * @return an instance of CertificateDigestMetadata, but some fields may be null if they were not included in the descriptor's meta section
     */
    public static PublicKeyMetadata getPublicKeyMetadata(TlsPolicyDescriptor tlsPolicyDescriptor) {
        PublicKeyMetadata metadata = new PublicKeyMetadata();
        if( tlsPolicyDescriptor.getMeta() == null ) {
            return metadata;
        }
        if(tlsPolicyDescriptor.getMeta() == null) {
            throw new IllegalArgumentException("TLS policy descriptor metadata cannot be null.");
        }
        if( tlsPolicyDescriptor.getMeta().get("encoding") != null && !tlsPolicyDescriptor.getMeta().get("encoding").isEmpty() ) {
            metadata.encoding = tlsPolicyDescriptor.getMeta().get("encoding");
        }
        return metadata;
    }

}
