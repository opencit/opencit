/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX TODO maybe create a separate class that is simply a container for (PrivateKey,X509Certificate) for situations
 * in which the pair is needed but the identity is customized (not a SHA-256 hash of the certificate) or situations
 * where the signature is not needed. 
 * @since 0.5.2
 * @author jbuhacoff
 */
public class RsaCredential implements Credential {
    private static Logger log = LoggerFactory.getLogger(RsaCredential.class);
    private final PrivateKey privateKey;
    private PublicKey publicKey;
    private final String digestAlgorithm = "SHA-256";
    private final String signatureAlgorithm = "SHA256withRSA";
    private final byte[] identity;
    
    public RsaCredential(PrivateKey privateKey, byte[] credential) throws CryptographyException {
        if( !"RSA".equals(privateKey.getAlgorithm()) ) {
            throw new IllegalArgumentException("Key must be RSA");
        }
        this.privateKey = privateKey;
        this.identity = createIdentity(credential);
    }
    
    
    /**
     * Initializes the RsaCredential using the private and public keys from the
     * provided key pair. The digest of the public key will be used as the identity.
     * @param keyPair
     * @throws NoSuchAlgorithmException 
     */
    public RsaCredential(KeyPair keyPair) throws CryptographyException {
        this(keyPair.getPrivate(), keyPair.getPublic());
    }

    /**
     * Initializes the RsaCredential using the provided private and public keys.
     * The digest of the public key will be used as the identity.
     * @param privateKey
     * @param publicKey
     * @throws NoSuchAlgorithmException 
     */
    public RsaCredential(PrivateKey privateKey, PublicKey publicKey) throws CryptographyException {
        this(privateKey, publicKey.getEncoded());
        this.publicKey = publicKey;
    }


    private byte[] createIdentity(byte[] publicKeyOrCertificate) throws CryptographyException {
        try {
            MessageDigest hash = MessageDigest.getInstance(digestAlgorithm); // throws NoSuchAlgorithmException
            byte[] digest = hash.digest(publicKeyOrCertificate);
            return digest;
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    /**
     * 
     * @return SHA-256 fingerprint of the Certificate containing the RSA Public Key
     */
    @Override
    public byte[] identity() {
        return identity;
    }
    
    @Override
    public byte[] signature(byte[] document) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance(signatureAlgorithm); 
        rsa.initSign(privateKey);
        rsa.update(document);
        return rsa.sign();
    }
    
    /**
     * 
     * @return the signature algorithm "SHA256withRSA"
     */
    @Override
    public String algorithm() {
        return signatureAlgorithm;
    }
}
