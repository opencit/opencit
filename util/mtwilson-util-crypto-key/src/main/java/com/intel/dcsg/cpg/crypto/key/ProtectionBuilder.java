/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory;
import com.intel.dcsg.cpg.crypto.key.password.PasswordCryptoCodecFactory.EncryptionAlgorithmInfo;

/**
 *
 * @author jbuhacoff
 */
public class ProtectionBuilder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProtectionBuilder.class);

    protected Protection protection = new Protection();
    
    public static ProtectionBuilder factory() { return new ProtectionBuilder(); }
    
    /**
     * Sets the algorithm name to AES, block size to 16 bytes, and key length to 128 bits.
     * You still need to set mode and padding after calling this.
     * @return 
     */
    public ProtectionBuilder aes(int keyLengthBits) {
        if( keyLengthBits == 128 || keyLengthBits == 192 || keyLengthBits == 256 ) {
            protection.algorithm = "AES";
            protection.blockSizeBytes = 16; // AES standard uses only 16-byte block sizes even for larger key sizes;  the original Rijndael algorithm allowed larger block sizes so for Rijndael it would be keyLengthBits/8 
            protection.keyLengthBits = keyLengthBits;
        }
        else {
            throw new IllegalArgumentException("AES supports only 128, 192, or 256 bit key lengths");
        }
        return this;
    }
    
    // XXX TODO 3DES and others similar to aes(int) method.
    
    public ProtectionBuilder sha1() {
        protection.digestAlgorithm = "SHA-1";
        protection.digestSizeBytes = 20;
        return this;
    }

    public ProtectionBuilder sha256() {
        protection.digestAlgorithm = "SHA-256";
        protection.digestSizeBytes = 32;
        return this;
    }

    public ProtectionBuilder sha384() {
        protection.digestAlgorithm = "SHA-384";
        protection.digestSizeBytes = 48;
        return this;
    }

    public ProtectionBuilder sha512() {
        protection.digestAlgorithm = "SHA-512";
        protection.digestSizeBytes = 64;
        return this;
    }
    
    public ProtectionBuilder block() {
        protection.mode = "CBC";
        protection.padding = "PKCS5Padding";
        return this;
    }
    public ProtectionBuilder stream() {
        protection.mode = "OFB8";
        protection.padding = "NoPadding";
        return this;
    }
    
    
    /**
     * 
     * @param algorithm for example "AES" or "AES/OFB8/NoPadding" (in the second case you don't need to call mode or padding separately)
     * @return 
     */
    public ProtectionBuilder algorithm(String algorithm) {
        log.debug("protectionbuilder algorithm {}", algorithm);
        if( algorithm.contains("/") ) {
            String parts[] = algorithm.split("/");
            if( parts.length != 3 ) { throw new IllegalArgumentException("Invalid algorithm name: "+algorithm); }  // XX TODO maybe use a Model/Fault system and defer faults until the end ?
            protection.algorithm = parts[0];
            protection.mode = parts[1];
            protection.padding = parts[2];
        }
        else if( algorithm.equals("AES128") ) {
            aes(128);
        }
        else if( algorithm.equals("AES192")) {
            aes(192);
        }
        else if( algorithm.equals("AES256")) {
            aes(256);
        }
        else {
            protection.algorithm = algorithm; // could be AES
        }
        log.debug("algorithm set to {}", protection.algorithm);
        EncryptionAlgorithmInfo info = PasswordCryptoCodecFactory.getEncryptionAlgorithmInfo(protection.algorithm);
        if( info != null ) {
            log.debug("found encryption alg info for {}", algorithm);
            if( protection.blockSizeBytes == 0 ) {
                protection.blockSizeBytes = info.getBlockSize();
                log.debug("set block size bytes {}", protection.blockSizeBytes);
            }
            if( protection.keyLengthBits == 0 ) {
                protection.keyLengthBits = info.getKeyBits();
                log.debug("set key length bits {}", protection.keyLengthBits);
            }
        }
        return this;
    }

    /**
     * XXX TODO  need constants for known modes (in addition to this custom name)  or maybe helper methods like ofb8()  cbc()  etc.
     * @param mode for example "OFB8"
     * @return 
     */
    public ProtectionBuilder mode(String mode) {
        protection.mode = mode;
        return this;
    }
    
    /**
     * 
     * @param padding for example "NoPadding"
     * @return 
     */
    public ProtectionBuilder padding(String padding) {
        protection.padding = padding;
        return this;
    }
    
    /**
     * 
     * @param digestAlgorithm for example "SHA-256"
     * @return 
     */
    public ProtectionBuilder digestAlgorithm(String digestAlgorithm) {
        protection.digestAlgorithm = digestAlgorithm;
        return this;
    }

    /**
     * 
     * @param keyLengthBits for example 128 for AES-128
     * @return 
     */
    public ProtectionBuilder keyLengthBits(int keyLengthBits) {
        protection.keyLengthBits = keyLengthBits;
        return this;
    }

        /**
     * 
     * @param blockSizeBytes for example 16 for AES-128
     * @return 
     */
    public ProtectionBuilder blockSizeBytes(int blockSizeBytes) {
        protection.blockSizeBytes = blockSizeBytes;
        return this;
    }

        /**
     * 
     * @param digestSizeBytes for example 20 for SHA-1, or 32 for SHA-256
     * @return 
     */
    public ProtectionBuilder digestSizeBytes(int digestSizeBytes) {
        protection.digestSizeBytes = digestSizeBytes;
        return this;
    }

    public Protection build() /*throws NoSuchAlgorithmException, NoSuchPaddingException*/ {
//        protection.cipher = Cipher.getInstance(String.format("%s/%s/%s", protection.algorithm, protection.mode, protection.padding));
        if( protection.mode == null || protection.padding == null ) {
            throw new IllegalArgumentException("Missing cipher mode and padding scheme");
        }
        protection.cipher = String.format("%s/%s/%s", protection.algorithm, protection.mode, protection.padding);
        return protection;
    }
}
