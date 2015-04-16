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
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PasswordKeyEnvelopeRecipient class can be used by the owner of the password to unseal an
 * existing key envelope. An instance of PasswordKeyEnvelopeRecipient always uses the same password to
 * unseal envelopes. If you need a one-liner, use the static method unsealEnvelopeWithPassword.
 * 
 * This class uses the algorithm defined by PasswordKeyEnvelope. 
 * 
 * Compatibility note: earlier version of this class in 0.2 was called 
 * PasswordKeyEnvelopeRecipient
 * 
 * @since 0.3
 * @author jbuhacoff
 */
public class PasswordProtectedKeyPemEnvelopeOpener {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String password;    
    private SecretKeyFactory secretKeyFactory;
    private String algorithm;
    
    /**
     * 
     * @param password password to be used to open PasswordKeyEnvelope objects; must be same as the password used to create them using PasswordKeyEnvelopeFactory
     * @throws CryptographyException with NoSuchAlgorithmException as the root cause
     */
    public PasswordProtectedKeyPemEnvelopeOpener(String password) {
        this.password = password;
    }
    
    /**
     * Create a SecretKeyFactory for the given algorithm, extracting the algorithm from the algorithm, cipher mode, and padding.
     * @param algorithm to use for decrypting an existing password-based key, for example PBEWithMD5AndDES/CBC/PKCS5Padding
     */
    private void initSecretKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        String algorithmParts[] = algorithm.split("/"); // split "PBEWithMD5AndDES/CBC/PKCS5Padding" into PBEWithMD5AndDES, CBC, and PKCS5Padding
        secretKeyFactory = SecretKeyFactory.getInstance(algorithmParts[0]); // NoSuchAlgorithmException        
    }
        
    

    /**
     * 
     * @param envelope generated using PasswordKeyEnvelopeFactory
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public Key unseal(PemKeyEncryption envelope) throws CryptographyException {
        try {
            initSecretKeyFactory(envelope.getEncryptionAlgorithm());
            PasswordHash envelopePasswordHash = PasswordHash.valueOf(envelope.getEncryptionKeyId());
            PasswordHash recipientPasswordHash = new PasswordHash(password, envelopePasswordHash.getSalt());
            log.debug("envelope password hash: {}", envelopePasswordHash.toString());
            log.debug("recipient password hash: {}", recipientPasswordHash.toString());
            log.debug("envelope salt length: {}", envelopePasswordHash.getSalt().length);
            if( !Arrays.equals(envelopePasswordHash.getHash(), recipientPasswordHash.getHash()) ) { throw new IllegalArgumentException("PasswordKeyEnvelope created with "+envelopePasswordHash+" cannot be unsealed using password corresponding to "+recipientPasswordHash); }
            // derive the password-based key-encryption key 
            SecretKey kek = secretKeyFactory.generateSecret(new PBEKeySpec(password.toCharArray(), envelopePasswordHash.getSalt(), PasswordProtectedKeyPemEnvelopeFactory.PBE_ITERATIONS, PasswordProtectedKeyPemEnvelopeFactory.PBE_KEY_SIZE)); // XXX TODO need to move these parameters from constants to parameters in the file... additional attribtues in the header maybe like envelopeAlgorithm maybe?  OR need to find out what are the default values, and if they are acceptable just use that and do not specify it
            AlgorithmParameterSpec kekParams = new PBEParameterSpec(envelopePasswordHash.getSalt(), PasswordProtectedKeyPemEnvelopeFactory.PBE_ITERATIONS); // need to define the algorithm parameter specs because the cipher receives the Key interface which is generic... so it doesn't know about the parameters that are embedded in it
            Cipher cipher = Cipher.getInstance(envelope.getEncryptionAlgorithm()); // NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "PBEWithHmacSHA1AndDESede/CBC/PKCS5Padding" 
            cipher.init(Cipher.UNWRAP_MODE, kek, kekParams); // InvalidKeyException
            return cipher.unwrap(envelope.getDocument().getContent(), envelope.getContentAlgorithm(), Cipher.SECRET_KEY); // contentAlgorithm like "AES" or "RSA"
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    /**
     * 
     * @param envelope generated using PasswordKeyEnvelopeFactory
     * @param password password to be used to open PasswordKeyEnvelope objects; must be same as the password used to create them using PasswordKeyEnvelopeFactory
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public static Key unsealEnvelopeWithPassword(PemKeyEncryption envelope, String password) throws CryptographyException {
        PasswordProtectedKeyPemEnvelopeOpener recipient = new PasswordProtectedKeyPemEnvelopeOpener(password);
        return recipient.unseal(envelope);
    }
    
    
    public static boolean isCompatible(Pem pem) {
        if( pem.getBanner().equals("SECRET KEY") || pem.getBanner().equals("ENCRYPTED SECRET KEY") || pem.getBanner().equals("ENCRYPTED KEY") ) {
            PemKeyEncryption envelope = PemKeyEncryptionUtil.getEnvelope(pem);
            if( envelope.getEncryptionAlgorithm().startsWith("PBEWith") || envelope.getEncryptionAlgorithm().startsWith("PBKDF2With") ) {
                return true;
            }
        }
        return false;
    }
    
}
