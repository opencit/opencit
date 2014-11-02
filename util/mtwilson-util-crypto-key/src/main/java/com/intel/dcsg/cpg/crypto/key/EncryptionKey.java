/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.SecretKey;

/**
 * Facilitates key life-cycle management for applications.
 *
 * A ManagedSecretKey tracks how many times the key was used for encryption and
 * automatically expires the key after the maximum number of encryptions has
 * occurred. It also tracks when the key was created and automatically expires
 * the key if its too old.
 *
 * The ManagedSecretKey restricts the use of each key to a single cipher mode
 * and padding scheme in order to avoid any potential cryptanalysis resulting
 * from different uses of the same key.
 *
 * The Java algorithm name "AES/OFB8/NoPadding" is read cipher/mode/padding.
 * 
 * This class in conjunction with the key manager can be used to replace the Aes class in cpg-crypto-1.5.
 *
 * See also
 * http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html
 * for information on available ciphers and restrictions on key sizes, for
 * example AES key size can only be 128, 192, or 256.
 *
 * See also http://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html
 * for list of standard ciphers supported on all Java platforms.
 *
 * @author jbuhacoff
 */
public class EncryptionKey {
    
    // key info 
    protected transient SecretKey secretKey;
//    protected transient byte[] keyBytes; // should only serialize the key in encrypted/wrapped form
    protected byte[] keyId;
    protected long createdOn; // seconds since unix epoch   Jan 1, 1970    XXX TODO  use Date class instead?
    protected transient AtomicLong encryptionCounter; // must serialize its value
    
    protected Protection protection; // algorithm, mode, padding, key length, block size, digest algorithm
    
    //keypolicy
    private long keyCounterMax = Integer.MAX_VALUE;
    private long expiresOn; // seconds since unix epoch ;   may need to use Long if it 
    
    //bogus key / timing side-channel defense
    protected boolean bogus = false; // set to true by decryption key source only when the named key cannot be found.  valid; //XXX TODO  maybe rename to "bogus" ?? and flip the logic from valid true if this is the key that was requested;  false if this is a bogus key that was created to defend against timing side-channel attacks

    public EncryptionKey() {
        encryptionCounter = new AtomicLong(0);
    }

    /**
     *
     * @param key
     */
    protected void setSecretKey(SecretKey key) {
        this.secretKey = key;
    }

    /**
     *
     * @param keyId
     */
    protected void setKeyId(byte[] keyId) {
        this.keyId = keyId;
    }

    protected void setEncryptionCounter(AtomicLong encryptionCounter) {
        this.encryptionCounter = encryptionCounter;
    }


    protected void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public SecretKey getSecretKey() { return secretKey; }
    
    public byte[] getKeyId() {
        return keyId;
    }
    
    public long getEncryptionCounter() {
        return encryptionCounter.get();
    }

    public long getCreatedOn() {
        return createdOn;
    }

    protected void setProtection(Protection protection) {
        this.protection = protection;
    }

    public Protection getProtection() {
        return protection;
    }
    
    

    public String getAlgorithm() {
        return protection.algorithm;
    }

    public String getMode() {
        return protection.mode;
    }

    public String getPadding() {
        return protection.padding;
    }

    public int getKeyLength() {
        return protection.keyLengthBits;
    }

    public int getBlockSize() {
        return protection.blockSizeBytes;
    }

    public boolean isBogus() {
        return bogus;
    }

    
    
}
