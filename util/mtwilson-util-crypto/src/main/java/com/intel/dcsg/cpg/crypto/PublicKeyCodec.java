/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.mtwilson.codec.ObjectCodec;
import com.intel.dcsg.cpg.x509.X509Util;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class PublicKeyCodec implements ObjectCodec {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyCodec.class);
    
    @Override
    public byte[] encode(Object input) {
        if( input instanceof PublicKey ) {
            PublicKey publicKey = (PublicKey)input;
            return publicKey.getEncoded();
        }
        if( input instanceof X509Certificate ) {
            PublicKey publicKey = ((X509Certificate)input).getPublicKey();
            return publicKey.getEncoded();
        }
        throw new UnsupportedOperationException(String.format("Cannot encode %s", input.getClass().getName()));
    }

    @Override
    public PublicKey decode(byte[] encoded) {
        try {
            return decodePublicKeyOrCertificate(encoded);
        }
        catch(CryptographyException e) {
            throw new IllegalArgumentException("Cannot decode public key", e);
        }
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
    private PublicKey decodePublicKeyOrCertificate(byte[] encoded) throws CryptographyException {
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
    
}
