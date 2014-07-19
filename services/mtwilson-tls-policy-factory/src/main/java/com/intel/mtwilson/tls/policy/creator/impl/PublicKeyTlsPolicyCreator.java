/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.creator.impl;

import com.intel.dcsg.cpg.codec.ByteArrayCodec;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.dcsg.cpg.x509.repository.HashSetMutablePublicKeyRepository;
import com.intel.dcsg.cpg.x509.repository.PublicKeyRepository;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getCertificateDigestMetadata;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getCodecByName;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getCodecForData;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.getFirst;
import static com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.guessAlgorithmForDigest;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyTlsPolicyCreator implements TlsPolicyCreator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyTlsPolicyCreator.class);
    
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
    
    private PublicKeyRepository getPublicKeyRepository(TlsPolicyDescriptor tlsPolicyDescriptor) throws CryptographyException {
        HashSetMutablePublicKeyRepository repository = new HashSetMutablePublicKeyRepository();
        if( "public-key".equals(tlsPolicyDescriptor.getPolicyType()) && tlsPolicyDescriptor.getData() != null  ) {
            if( tlsPolicyDescriptor.getData() == null || tlsPolicyDescriptor.getData().isEmpty()  ) {
                throw new IllegalArgumentException("TLS policy descriptor does not contain any public keys");
            }
            ByteArrayCodec codec;
            PublicKeyMetadata meta = getPublicKeyMetadata(tlsPolicyDescriptor);
            if( meta.encoding == null ) {
                // attempt auto-detection based on first digest
                String sample = getFirst(tlsPolicyDescriptor.getData());
                codec = getCodecForData(sample);
                log.debug("Codec {} for sample data {}", (codec==null?"null":codec.getClass().getName()), sample);
            }
            else {
                String encoding = meta.encoding;
                codec = getCodecByName(encoding);
                log.debug("Codec {} for public key encoding {}", (codec==null?"null":codec.getClass().getName()), encoding);
            }
            if( codec == null ) {
                throw new IllegalArgumentException("TlsPolicyDescriptor indicates public keys but does not declare public key encoding");
            }
            for(String publicKeyEncoded : tlsPolicyDescriptor.getData()) {
                PublicKey publicKey = decodePublicKey(codec.decode(publicKeyEncoded));
                repository.addPublicKey(publicKey);
            }
            return repository;
        }
        return null;
    }
    
    /**
     * Public Key X509 structure looks like this:
     * <pre>
     * PublicKeyInfo ::= SEQUENCE {
     *     algorithm     AlgorithmIdentifier,
     *     PublicKey     BIT STRING (see RSAPublicKey below)
     * }
     * 
     * AlgorithmIdentifier ::= SEQUENCE {
     *     algorithm     OBJECT IDENTIFIER,  (1.2.840.113549.1.1.1 for RSA)
     *     parameters    as defined by algorithm (optional)
     * }
     * </pre>
     * 
     * RSA Public Key X509 structure looks like:
     * <pre>
     * RSAPublicKey ::= SEQUENCE {
     *     modulus         INTEGER, (n)
     *     publicExponent  INTEGER  (e)
     * }
     * </pre>
     * 
     * So a 1024-bit RSA public key in hex looks like this:
     * 30 81 9F 30 0D 06 09 2A  86 48 86 F7 0D 01 01 01
     * 
     * 30 81 9F 30 0D (SEQUENCE len1 SEQUENCE len2)
     * 06 09 (OID)
     * 2A  86 48 86 F7 0D 01 01 01 (1.2.840.113549.1.1.1)
     * 
     * A 2048-bit RSA public key in hex looks like this:
     * 30 82 01 22 30 0D 06 09  2A 86 48 86 F7 0D 01 01 01
     * 
     * A 4096-bit RSA public key in hex looks like this:
     * 30 82 02 22 30 0D 06 09  2A 86 48 86 F7 0D 01 01 01
     * 
     * An X509 certificate using sha256WithRsaEncryption would have an OID 1.2.840.113549.1.1.11 somewhere near the top but that's not the only possible OID
     * 
     * @param publicKeyBase64
     * @return
     * @throws CryptographyException 
     */
    private PublicKey decodePublicKey(byte[] encoded) throws CryptographyException {
        // the X.509 DER encodings for PublicKey have these headers:
        String rsa1024 = "30819F300D06092A864886F70D010101";
        String rsa2048 = "30820122300D06092A864886F70D010101";
        String rsa4096 = "30820222300D06092A864886F70D010101";
        String hex = Hex.encodeHexString(encoded).toUpperCase();
        // first try to recognize an RSAPublicKey structure
        if( hex.startsWith(rsa1024) || hex.startsWith(rsa2048) || hex.startsWith(rsa4096) ) { 
            PublicKey publicKey = RsaUtil.decodeDerPublicKey(encoded);
            return publicKey;
        }
        // second try decoding as an X509Certificate from which we should extract the public key
        try {
            X509Certificate certificate = X509Util.decodeDerCertificate(encoded);
            return certificate.getPublicKey();
        }
        catch(Exception e) {
            log.debug("Failed attempt to decode public key certificate: {}", e.getMessage());
        }
        // finally just try decoding it as a public key because it might be something  other than RSA so the OID would be different
        PublicKey publicKey = RsaUtil.decodeDerPublicKey(encoded);
        return publicKey;
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
        if( tlsPolicyDescriptor.getMeta().get("encoding") != null && !tlsPolicyDescriptor.getMeta().get("encoding").isEmpty() ) {
            metadata.encoding = tlsPolicyDescriptor.getMeta().get("encoding");
        }
        return metadata;
    }

}
