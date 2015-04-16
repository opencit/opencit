/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * XXX TODO may need to split this up into the 3 categories: 1) confidentiality
 * (symmetric key info) 2) integrity (digest info) 3) authentication (asymmetric
 * key inof - currently not here) and make each one an interface: public
 * interface Integrity { String getDigestAlgorithm(); int getDigestSizeBytes();
 * } public interface Confidentiality { String getAlgorithm(); // AES String
 * getMode(); String getPadding(); String getCipher(); // format is
 * algorithm/mode/padding int getKeyLengthBits(); int blockSizeBytes(); } public
 * interface Authentication { String getAlgorithm(); // RSA int
 * getKeyLengthBits(); } then public class Protection implements
 * Confidentiality,Integrity,Authentication { ... } that way individual methods
 * that require a protection object as a parameter can use one of the specific
 * interfaces if they're only looking for digest info, or cipher info. and also
 * then other objects could be passed to them too, as well as evaluate other
 * objects against the protection policy .
 *
 * @author jbuhacoff
 */
public class Protection /*implements Copyable*/ {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Protection.class);

    protected String algorithm; // algorithm like "AES"
    protected String mode; // mode like "OFB8"
    protected String padding; // padding like "NoPadding"
    protected int keyLengthBits;
    protected int blockSizeBytes;
    protected String digestAlgorithm; // like "SHA-256" used for integrity protection; may be null if the content is not protected
    protected int digestSizeBytes; // 20 for SHA-1, 32 for SHA-256, ...
    protected transient String cipher;    //  in the format  algorithm/mode/padding   or just algorithm  if mode and padding are not specified (dangerous - because the crypto provider will use its default so the platforma nd provider become a part of the message specification)

    public String getAlgorithm() {
        return algorithm;
    }

    public int getBlockSizeBytes() {
        return blockSizeBytes;
    }

    public int getKeyLengthBits() {
        return keyLengthBits;
    }

    public String getMode() {
        return mode;
    }

    public String getPadding() {
        return padding;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public int getDigestSizeBytes() {
        return digestSizeBytes;
    }

    public String getCipher() {
        return cipher;
    }

    protected void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    protected void setKeyLengthBits(int keyLengthBits) {
        this.keyLengthBits = keyLengthBits;
    }

    protected void setBlockSizeBytes(int blockSizeBytes) {
        this.blockSizeBytes = blockSizeBytes;
    }
    
    protected void setCipher(String cipher) {
        this.cipher = cipher;
    }

    protected void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    protected void setDigestSizeBytes(int digestSizeBytes) {
        this.digestSizeBytes = digestSizeBytes;
    }

    protected void setMode(String mode) {
        this.mode = mode;
    }

    protected void setPadding(String padding) {
        this.padding = padding;
    }
    
    public boolean isAvailable() {
        try {
            // generate a random key
            KeyGenerator kgen = KeyGenerator.getInstance(getAlgorithm()); // "AES"  // throws NoSuchAlgorithmException
            kgen.init(getKeyLengthBits());
            SecretKey skey = kgen.generateKey();
            // generate a random iv
            RandomSource random = new RandomSource();            
            byte[] iv = random.nextBytes(getBlockSizeBytes());
            Cipher testCipher = Cipher.getInstance(getCipher()); // like AES/OFB8/NoPadding
            testCipher.init(Cipher.ENCRYPT_MODE, skey, new IvParameterSpec(iv));
            // generate one block of random plaintext to encrypt
            byte[] plaintext = random.nextBytes(getBlockSizeBytes());
            byte[] ciphertext = testCipher.doFinal(plaintext);
            assert ciphertext != null; // any error would have already been thrown by the cipher so if we get here the doFinal call must have succeeded and ciphertext wouldn't be null
            return true;
        }
        catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            log.debug("Protection algorithm {} key length {} mode {} padding {} not available: {}", getAlgorithm(), getKeyLengthBits(), getMode(), getPadding(), e.getMessage());
            return false;
        }
    }
    
}
    /*
    @Override
    public void copy(Object other) {
        if (other instanceof Protection) {
            Protection protection = (Protection) other;
            this.algorithm = protection.algorithm;
            this.mode = protection.mode;
            this.padding = protection.padding;
            this.keyLengthBits = protection.keyLengthBits;
            this.blockSizeBytes = protection.blockSizeBytes;
            this.digestAlgorithm = protection.digestAlgorithm;
            this.digestSizeBytes = protection.digestSizeBytes;
            this.cipher = protection.cipher;
        }
    }
    */

