/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.security.Key;
import java.security.PrivateKey;
import javax.crypto.Cipher;

/**
 * The RsaKeyEnvelopeRecipient class can be used by the owner of the RSA private key to unseal an
 * existing key envelope. An instance of RsaKeyEnvelopeRecipient always uses the same private key to
 * unseal envelopes. If you need a one-liner, use the static method unsealEnvelopeWithPrivateKey.
 * 
 * This class uses the algorithm defined by RsaKeyEnvelope. 
 * 
 * Compatibility note: earlier version of this class in 0.2 was called 
 * RsaKeyEnvelopeRecipient
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class RsaPublicKeyProtectedPemKeyEnvelopeOpener {
    private PrivateKey privateKey;
    private String privateKeyId;
    
    /**
     * 
     * @param recipient private key to be used to open RsaKeyEnvelope objects; corresponds to the public key certificate used to create them using RsaKeyEnvelopeFactory
     */
    public RsaPublicKeyProtectedPemKeyEnvelopeOpener(RsaCredentialX509 recipient) throws CryptographyException {
        try {
            this.privateKey = recipient.getPrivateKey();
            this.privateKeyId = Md5Digest.digestOf(recipient.getCertificate().getEncoded()).toString();
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    public RsaPublicKeyProtectedPemKeyEnvelopeOpener(PrivateKey recipient, String encryptionKeyId) throws CryptographyException {
        try {
            this.privateKey = recipient;
            this.privateKeyId = encryptionKeyId;
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
        
    public Key unseal(Pem pem) throws CryptographyException {
        PemKeyEncryption envelope = PemKeyEncryptionUtil.getEnvelope(pem);
        if( envelope == null ) {
            throw new IllegalArgumentException("Unsupported format");
        }
        return unseal(envelope);
    }

    /**
     * 
     * @param envelope generated using RsaKeyEnvelopeFactory
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public Key unseal(PemKeyEncryption envelope) throws CryptographyException {
        try {
            if( !envelope.getEncryptionKeyId().equals(privateKeyId) ) { throw new IllegalArgumentException("RsaKeyEnvelope created with "+envelope.getEncryptionKeyId()+" cannot be unsealed using private key corresponding to "+privateKeyId); }
            
            // the envelope may have Encryption-Algorithm: RSA/ECB/OAEPWithSHA-256AndMGF1Padding  without Encryption-Mode or Encryption-Padding-Mode, or it may have Encryption-Algorithm: RSA, Encryption-Mode: ECB, Encryption-Padding-Mode: RSA/ECB/OAEPWithSHA-256AndMGF1Padding
            // so we need to make sure we have the full string like "RSA/ECB/OAEPWithSHA-256AndMGF1Padding" to pass to Cipher.getInstance
            String algorithm = envelope.getEncryptionAlgorithm();
            if( envelope.getEncryptionMode() != null && envelope.getEncryptionPaddingMode() != null ) {
                algorithm = String.format("%s/%s/%s", envelope.getEncryptionAlgorithm(),envelope.getEncryptionMode(),envelope.getEncryptionPaddingMode());
            }
            
            Cipher cipher = Cipher.getInstance(algorithm); // NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            cipher.init(Cipher.UNWRAP_MODE,privateKey); // InvalidKeyException
            return cipher.unwrap(envelope.getDocument().getContent(), envelope.getContentAlgorithm(), Cipher.SECRET_KEY); // contentAlgorithm like "AES"
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
    
    /**
     * 
     * @param envelope generated using RsaKeyEnvelopeFactory
     * @param recipientCredential private key to be used to open RsaKeyEnvelope objects; corresponds to the public key certificate used to create them using RsaKeyEnvelopeFactory
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public static Key unsealEnvelopeWithPrivateKey(PemKeyEncryption envelope, RsaCredentialX509 recipientCredential) throws CryptographyException {
        RsaPublicKeyProtectedPemKeyEnvelopeOpener recipient = new RsaPublicKeyProtectedPemKeyEnvelopeOpener(recipientCredential);
        return recipient.unseal(envelope);
    }
    
    public static boolean isCompatible(Pem pem) {
        if( pem.getBanner().equals("SECRET KEY") || pem.getBanner().equals("ENCRYPTED SECRET KEY") || pem.getBanner().equals("ENCRYPTED KEY") ) {
            PemKeyEncryption envelope = PemKeyEncryptionUtil.getEnvelope(pem);
            if( envelope.getEncryptionAlgorithm().startsWith("RSA") ) {
                return true;
            }
        }
        return false;
    }
        
    
}
