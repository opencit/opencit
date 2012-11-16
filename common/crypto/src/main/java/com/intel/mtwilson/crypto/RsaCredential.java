/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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
    
    protected RsaCredential(PrivateKey privateKey, byte[] credential) throws NoSuchAlgorithmException {
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
    public RsaCredential(KeyPair keyPair) throws NoSuchAlgorithmException {
        this(keyPair.getPrivate(), keyPair.getPublic());
    }

    /**
     * Initializes the RsaCredential using the provided private and public keys.
     * The digest of the public key will be used as the identity.
     * @param privateKey
     * @param publicKey
     * @throws NoSuchAlgorithmException 
     */
    public RsaCredential(PrivateKey privateKey, PublicKey publicKey) throws NoSuchAlgorithmException {
        this(privateKey, publicKey.getEncoded());
        this.publicKey = publicKey;
    }


    private byte[] createIdentity(byte[] publicKeyOrCertificate) throws NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance(digestAlgorithm);
        byte[] digest = hash.digest(publicKeyOrCertificate);
        return digest;
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
