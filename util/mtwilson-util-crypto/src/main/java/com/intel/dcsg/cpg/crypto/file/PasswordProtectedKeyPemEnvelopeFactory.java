/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.PasswordHash;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * The PasswordKeyEnvelopeFactory creates PasswordKeyEnvelope objects for various AES secret keys by encrypting those keys 
 * with a given password-based encryption key.  An instance of PasswordKeyEnvelopeFactory always uses
 * the same password to create envelopes. If you need a "one-liner" use the static method sealKeyWithCertificate.
 * 
 * To open the envelopes, use the PasswordKeyEnvelopeRecipient class with the same password used to create the envelope.
 * 
 * This class uses the algorithm PBEWithMD5AndDES which is expected to be present
 * on every Java platform, even though its weak.
 * 
 * Stronger algorithms like  PBEWithHmacSHA1AndDESede  (found in pkcs5 1.0) may not be available on all platforms.
 * 
 * NoSuchAlgorithmException: PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding SecretKeyFactory not available
 * NoSuchAlgorithmException: PBEWithHmacSHA1AndDESede SecretKeyFactory not available  ;  this is defined in PKCS#5 v2.0
 * NoSuchAlgorithmException: Cannot find any provider supporting PBKDF2WithHmacSHA1 ;  ;  this is defined in PKCS#5 v2.0
 * InvalidKeyException: The wrapped key is not padded correctly   ; if you use PBEWithMD5AndDES  w/o a mode and padding
 * NoSuchAlgorithmException: PBEWithMD5AndDES/CBC/PKCS5Padding SecretKeyFactory not available  ;  this is defined in PKCS#5 v2.0
 * 
 * See also:
 * http://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html
 * http://en.wikipedia.org/wiki/Triple_DES
 * http://docs.oracle.com/javase/7/docs/api/javax/crypto/spec/PBEKeySpec.html
 * 
 * Developer see also:
 * PBEParameterSpec
 * 
 * 
 * Example code:
 * 
 * PasswordKeyEnvelopeFactory factory = new PasswordKeyEnvelopeFactory(password);
 * SecretKey key = Aes128.generateKey();
 * PasswordKeyEnvelope keyEnvelope = factory.seal(key);
 * 
 * Compatibility note: earlier version of this class in 0.2 was called 
 * PasswordKeyEnvelopeFactory
 * 
 * See also:  
 * http://www.openssl.org/docs/apps/cms.html
 * http://etutorials.org/Programming/secure+programming/Chapter+7.+Public+Key+Cryptography/7.17+Representing+Keys+and+Certificates+in+Plaintext+PEM+Encoding/
 * http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html 
 * 
 * XXX TODO look into support for PBKDF2WithHmacSHA1 ... according to Java docs (see Standard Names ref above) it's only available as a parameter to SecretKeyFactory 
 * 
 * @since 0.3
 * @author jbuhacoff
 */
public class PasswordProtectedKeyPemEnvelopeFactory {
//    public static final String KEYGEN_ALGORITHM = "PBEWithMD5AndDES"; // "PBEWithHmacSHA1AndDESede"; // password-based encryption
    public static final String DEFAULT_ALGORITHM = "PBEWithMD5AndDES/CBC/PKCS5Padding"; // "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding"; // password-based encryption
    public static final int SALT_LENGTH_BYTES = 8; // bytes
    public static final int PBE_KEY_SIZE = 168; // bits
    public static final int PBE_ITERATIONS = 512; // affects the strength of the password-derived encryption key;  more iterations is stronger
    private SecretKeyFactory secretKeyFactory = null;
//    private String keygenAlgorithm;
//    private String cipherAlgorithm;
    private String algorithm; // like PBEWithMD5AndDES/CBC/PKCS5Padding
    private String password;
    
    /**
     * 
     * @param certificate public key certificate of the recipient to which envelopes will be addressed
     * @throws CryptographyException with NoSuchAlgorithmException as the root cause
     */
    public PasswordProtectedKeyPemEnvelopeFactory(String password) {
        this.password = password;
    }
    
    /**
     * Set algorithm, cipher mode, and padding.
     * If you don't call this method, the default algorithm will be used.
     * @param algorithm to use for deriving a new password-based key and for using it to encrypt the secret keys for example PBEWithMD5AndDES/CBC/PKCS5Padding
     */
    public void setAlgorithm(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        
        String algorithmParts[] = algorithm.split("/"); // split "PBEWithMD5AndDES/CBC/PKCS5Padding" into PBEWithMD5AndDES, CBC, and PKCS5Padding
        secretKeyFactory = SecretKeyFactory.getInstance(algorithmParts[0]); // NoSuchAlgorithmException        
    }
    
    public String getAlgorithm() { return algorithm; }
    
    
    /**
     * Creates a PasswordKeyEnvelope for the given secret key, using the password provided to the constructor
     * and a random salt.
     * 
     * @param secretKey for example new SecretKeySpec(aesKeyWith128bits, "AES")
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public PemKeyEncryption seal(Key secretKey) throws CryptographyException {
        try {
            if( secretKeyFactory == null ) {
                setAlgorithm(DEFAULT_ALGORITHM);
            }
            // create a new random salt for the password
            byte[] salt = new byte[SALT_LENGTH_BYTES];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(salt);
            // create a password-based key-encryption key (kek) and encrypt the secret key
            SecretKey kek = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), salt, PBE_ITERATIONS, PBE_KEY_SIZE));
            Cipher cipher = Cipher.getInstance(algorithm); // NoSuchAlgorithmException, NoSuchPaddingException
            AlgorithmParameterSpec kekParams = new PBEParameterSpec(salt, PBE_ITERATIONS); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
            cipher.init(Cipher.WRAP_MODE, kek, kekParams); // InvalidKeyException
            byte[] encryptedKey = cipher.wrap(secretKey); // IllegalBlockSizeException, BadPaddingException
            // create a digest of the password with the salt so the password can be confirmed when loading the file later
            PasswordHash passwordHash = new PasswordHash(password, salt);
            Pem pem = new Pem(KeyEnvelope.PEM_BANNER, encryptedKey);
            KeyEnvelope keyEnvelope = new KeyEnvelope(pem);
            keyEnvelope.setContentAlgorithm(secretKey.getAlgorithm()); // expected to be "AES" (for secret key) or "RSA" (for private key)
            keyEnvelope.setEncryptionKeyId(passwordHash.toString()); // a string like  salt-base64 ":" password-hash-base64
            keyEnvelope.setEncryptionAlgorithm(algorithm);
            return keyEnvelope;
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    /**
     * A convenience method for "one-liners" where you need to encrypt a given secret key that you need to send
     * protected by the password provided (the owner or recipient must know the password to decrypt)
     * @param secretKey for example Aes128.generateKey() for a new key  or new SecretKeySpec(existingAesKeyWith128bits, "AES") for an existing key
     * @param password
     * @return
     * @throws CryptographyException with one of the following as the cause: CertificateEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public static PemKeyEncryption sealKeyWithPassword(Key secretKey, String password) throws CryptographyException {
        PasswordProtectedKeyPemEnvelopeFactory factory = new PasswordProtectedKeyPemEnvelopeFactory(password);
        return factory.seal(secretKey);
    }
}
