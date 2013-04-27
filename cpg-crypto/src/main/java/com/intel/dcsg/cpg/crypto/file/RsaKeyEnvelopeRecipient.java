/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Md5Digest;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;

/**
 * The RsaKeyEnvelopeRecipient class can be used by the owner of the RSA private key to unseal an
 * existing key envelope. An instance of RsaKeyEnvelopeRecipient always uses the same private key to
 * unseal envelopes. If you need a one-liner, use the static method unsealEnvelopeWithPrivateKey.
 * 
 * This class uses the algorithm defined by RsaKeyEnvelope. 
 * 
 * 
 * @author jbuhacoff
 */
public class RsaKeyEnvelopeRecipient {
    private RsaCredentialX509 recipient;
    private String recipientFingerprint;
    
    /**
     * 
     * @param recipient private key to be used to open RsaKeyEnvelope objects; corresponds to the public key certificate used to create them using RsaKeyEnvelopeFactory
     */
    public RsaKeyEnvelopeRecipient(RsaCredentialX509 recipient) throws CryptographyException {
        try {
            this.recipient = recipient;
            this.recipientFingerprint = Md5Digest.digestOf(recipient.getCertificate().getEncoded()).toString();
        }
        catch(Exception e) {
            throw new CryptographyException(e);
        }
    }
        

    /**
     * 
     * @param envelope generated using RsaKeyEnvelopeFactory
     * @return
     * @throws CryptographyException with one of the following as the cause: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
     */
    public Key unseal(RsaKeyEnvelope envelope) throws CryptographyException {
        try {
            if( !envelope.getEnvelopeKeyId().equals(recipientFingerprint) ) { throw new IllegalArgumentException("RsaKeyEnvelope created with "+envelope.getEnvelopeKeyId()+" cannot be unsealed using private key corresponding to "+recipientFingerprint); }
            Cipher cipher = Cipher.getInstance(envelope.getEnvelopeAlgorithm()); // NoSuchAlgorithmException, NoSuchPaddingException ; envelopeAlgorithm like "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            cipher.init(Cipher.UNWRAP_MODE,recipient.getPrivateKey()); // InvalidKeyException
            return cipher.unwrap(envelope.getContent(), envelope.getContentAlgorithm(), Cipher.SECRET_KEY); // contentAlgorithm like "AES"
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
    public static Key unsealEnvelopeWithPrivateKey(RsaKeyEnvelope envelope, RsaCredentialX509 recipientCredential) throws CryptographyException {
        RsaKeyEnvelopeRecipient recipient = new RsaKeyEnvelopeRecipient(recipientCredential);
        return recipient.unseal(envelope);
    }
    
}
