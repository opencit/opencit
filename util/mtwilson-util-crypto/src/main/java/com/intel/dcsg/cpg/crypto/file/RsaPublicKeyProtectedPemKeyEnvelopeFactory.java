/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * The RsaKeyEnvelopeFactory creates RsaKeyEnvelope objects for various AES secret keys by encrypting those keys 
 * with a given public key (presented as an X509Certificate).  An instance of RsaKeyEnvelopeFactory always uses
 * the same public key to create envelopes. If you need a "one-liner" use the static method sealKeyWithCertificate.
 * 
 * To open the envelopes, use the RsaKeyEnvelopeRecipient class with the corresponding RSA private key.
 * 
 * This class uses the algorithm RSA/ECB/OAEPWithSHA-256AndMGF1Padding which is expected to be present
 * on every Java platform.  http://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html
 * 
 * Developer see also:
 * RSAKeyGenParameterSpec, MGF1ParameterSpec, OAEPParameterSpec
 * 
 * Example code:
 * 
 * RsaKeyEnvelopeFactory factory = new RsaKeyEnvelopeFactory(certificate);
 * SecretKey key = Aes128.generateKey();
 * RsaKeyEnvelope keyEnvelope = factory.seal(key);
 * 
 * Compatibility note: earlier version of this class in 0.2 was called 
 * RsaKeyEnvelopeFactory
 * 
 * @since 0.3
 * @author jbuhacoff
 */
public class RsaPublicKeyProtectedPemKeyEnvelopeFactory {
    public static final String DEFAULT_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"; // RSA encryption in ECB mode (since we expect to only have one block anyway) with OAEP padding that uses SHA-256 and MGF1
    private PublicKey publicKey;
    private String publicKeyId;
//    private X509Certificate certificate;
//    private String certificateFingerprint; // MD5 of the certificate
    private String algorithm = null;
    
    /**
     * 
     * @param certificate public key certificate of the recipient to which envelopes will be addressed
     * @throws CryptographyException with CertificateEncodingException as the root cause
     */
    public RsaPublicKeyProtectedPemKeyEnvelopeFactory(X509Certificate certificate) throws CryptographyException {
        try {
            this.algorithm = DEFAULT_ALGORITHM;
//            this.certificate = certificate;
//            this.certificateFingerprint = Md5Digest.digestOf(certificate.getEncoded()).toString();
            this.publicKey = certificate.getPublicKey();
            this.publicKeyId = Md5Digest.digestOf(certificate.getEncoded()).toString(); // maybe should be sha1 or sha256
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }

    public RsaPublicKeyProtectedPemKeyEnvelopeFactory(PublicKey publicKey, String publicKeyId) {
        this.publicKey = publicKey;
        this.publicKeyId = publicKeyId;
        this.algorithm = DEFAULT_ALGORITHM; // note that publicKey.getAlgorithm() is "RSA" but the algorithm string we need to provide the cipher is cipher/mode/padding
    }
    
    
    /**
     * Set algorithm, cipher mode, and padding.
     * If you don't call this method, the default algorithm will be used.
     * @param algorithm to use for encrypting a  key, for example RSA/ECB/OAEPWithSHA-256AndMGF1Padding
     */
    public void setAlgorithm(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
    }
    
    public String getAlgorithm() { return algorithm; }
    
    
    
    /**
     * Creates an RsaKeyEnvelope for the given secret key
     * 
     * @param secretKey for example new SecretKeySpec(aesKeyWith128bits, "AES")
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public PemKeyEncryption seal(Key secretKey) throws CryptographyException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm); // NoSuchAlgorithmException, NoSuchPaddingException
            cipher.init(Cipher.WRAP_MODE,publicKey); // InvalidKeyException
            byte[] encryptedKey = cipher.wrap(secretKey); // IllegalBlockSizeException, BadPaddingException
            Pem pem = new Pem(KeyEnvelope.PEM_BANNER, encryptedKey);
            KeyEnvelope keyEnvelope = new KeyEnvelope(pem);
            keyEnvelope.setContentAlgorithm(secretKey.getAlgorithm()); // expected to be "AES"
            keyEnvelope.setEncryptionKeyId(publicKeyId);
            keyEnvelope.setEncryptionAlgorithm(algorithm);
            return keyEnvelope;
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    /**
     * A convenience method for "one-liners" where you need to encrypt a given secret key that you need to send
     * to the owner of the certificate provided (the owner has the corresponding private key)
     * @param secretKey for example Aes128.generateKey() for a new key  or new SecretKeySpec(existingAesKeyWith128bits, "AES") for an existing key
     * @param certificate
     * @return
     * @throws CryptographyException with one of the following as the cause: CertificateEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public static PemKeyEncryption sealKeyWithCertificate(Key secretKey, X509Certificate certificate) throws CryptographyException {
        RsaPublicKeyProtectedPemKeyEnvelopeFactory factory = new RsaPublicKeyProtectedPemKeyEnvelopeFactory(certificate);
        return factory.seal(secretKey);
    }
}
