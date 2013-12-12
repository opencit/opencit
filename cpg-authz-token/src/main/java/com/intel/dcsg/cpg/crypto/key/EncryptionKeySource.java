/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.io.UUID;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Linked to a MutableSecretKeyRepository, this class provides a valid
 * encryption key for each encryption and automatically rotates keys
 * according to the key management policy.
 * 
 * The MutableSecretKeyRepository is used to archive keys when they expire.
 * 
 * XXX TODO maybe this class is not necessary at all if we move the
 * key generation and rekey logic into the mutable repository... that one 
 * is an interface but we could have a concrete implementation which does
 * it, and that could be abstracted into a key policy interface and implementation....
 * 
 * @author jbuhacoff
 */
public class EncryptionKeySource {
    private transient HashMap<String,EncryptionKey> current = new HashMap<String,EncryptionKey>(); // one current key for each specification
//    private EncryptionKey currentKey;
    private MutableSecretKeyRepository repository;
//    private Protection protection;
    
    public EncryptionKeySource() {
        this.repository = new HashMapMutableSecretKeyRepository(new HashMap<String,EncryptionKey>());
//        this.protection = protection;
    }
    public EncryptionKeySource(MutableSecretKeyRepository repository/*, Protection protection*/) {
        this.repository = repository;
//        this.protection = protection;
    }
    
    public MutableSecretKeyRepository getRepository() {
        return repository;
    }

    protected void setRepository(MutableSecretKeyRepository repository) {
        this.repository = repository;
    }
/*
    public Protection getProtection() {
        return protection;
    }

    protected void setProtection(Protection protection) {
        this.protection = protection;
    }
    */
    
    public EncryptionKey getEncryptionKey(Protection protection) throws CryptographyException {
        EncryptionKey key = current.get(protection.cipher);
        if( key == null ) {
            key = createEncryptionKey(protection);
            current.put(protection.cipher, key);
        }
        return key;
    }
    
    public EncryptionKey createDecryptionKey(Protection protection) throws CryptographyException {
        EncryptionKey key = createEncryptionKey(protection);
        return key;
    }
    

    public EncryptionKey getEncryptionKey(byte[] keyId) {
        EncryptionKey key = repository.find(keyId);
        return key;
    }
    
    
    /**
     * 
     * @param keyId
     * @return the key or null if it was not found
     */
    public EncryptionKey getDecryptionKey(byte[] keyId) {
        EncryptionKey key = repository.find(keyId);
        return key;
    }
    
    public EncryptionKey createEncryptionKey(Protection protection) throws CryptographyException {
        EncryptionKey encryptionKey = new EncryptionKey();
        encryptionKey.secretKey = generateKey(protection); // cryptographyexception represents nosuchalgorithmexception 
        encryptionKey.createdOn = System.currentTimeMillis()/1000L;
        encryptionKey.encryptionCounter = new AtomicLong(0);
        encryptionKey.keyId = new UUID().toByteArray().getBytes();
        encryptionKey.protection = protection;
        // XXX TODO  mark the managed encryption key with "can encrypt" and maybe the # of times that we're allowing it to encrypt (the RSVP) and the expiration time 
        repository.add(encryptionKey);
        return encryptionKey;
    }
    
    private SecretKey generateKey(Protection protection) throws CryptographyException {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(protection.getAlgorithm()); // "AES"
            kgen.init(protection.getKeyLengthBits());
            SecretKey skey = kgen.generateKey();
            return skey;
        }
        catch(NoSuchAlgorithmException e) {
            throw new CryptographyException(e);
        }
    }
    
}
