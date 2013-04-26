/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Md5Digest;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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
 * @author jbuhacoff
 */
public class RsaKeyEnvelopeFactory {
    public static final String DEFAULT_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"; // RSA encryption in ECB mode (since we expect to only have one block anyway) with OAEP padding that uses SHA-256 and MGF1
    private X509Certificate certificate;
    private String certificateFingerprint; // MD5 of the certificate
    private String algorithm = null;
    
    /**
     * 
     * @param certificate public key certificate of the recipient to which envelopes will be addressed
     * @throws CryptographyException with CertificateEncodingException as the root cause
     */
    public RsaKeyEnvelopeFactory(X509Certificate certificate) throws CryptographyException {
        try {
            this.algorithm = DEFAULT_ALGORITHM;
            this.certificate = certificate;
            this.certificateFingerprint = Md5Digest.valueOf(certificate.getEncoded()).toString();
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
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
    public RsaKeyEnvelope seal(Key secretKey) throws CryptographyException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm); // NoSuchAlgorithmException, NoSuchPaddingException
            cipher.init(Cipher.WRAP_MODE,certificate); // InvalidKeyException
            byte[] encryptedKey = cipher.wrap(secretKey); // IllegalBlockSizeException, BadPaddingException
            RsaKeyEnvelope keyEnvelope = new RsaKeyEnvelope();
            keyEnvelope.setContent(encryptedKey);
            keyEnvelope.setContentAlgorithm(secretKey.getAlgorithm()); // expected to be "AES"
            keyEnvelope.setEnvelopeKeyId(certificateFingerprint);
            keyEnvelope.setEnvelopeAlgorithm(algorithm);
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
    public static RsaKeyEnvelope sealKeyWithCertificate(Key secretKey, X509Certificate certificate) throws CryptographyException {
        RsaKeyEnvelopeFactory factory = new RsaKeyEnvelopeFactory(certificate);
        return factory.seal(secretKey);
    }
}
