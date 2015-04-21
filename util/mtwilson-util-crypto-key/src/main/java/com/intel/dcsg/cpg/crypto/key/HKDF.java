/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Reference: http://tools.ietf.org/html/rfc5869
 *
 * Tested with HmacSHA1 and HmacSHA256 algorithms.
 * 
 * @author jbuhacoff
 */
public class HKDF {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HKDF.class);
    private String macAlgorithm;
    private int macLength;

    /**
     *
     * @param algorithm such as "HmacSHA1" or "HmacSHA256"
     * @throws NoSuchAlgorithmException
     */
    public HKDF(String algorithm) throws NoSuchAlgorithmException {
        Mac mac = Mac.getInstance(algorithm); // algorithm like HmacSHA256
        macAlgorithm = mac.getAlgorithm();
        macLength = mac.getMacLength();
        log.debug("Mac {} length {} provider {}", mac.getAlgorithm(), mac.getMacLength(), mac.getProvider().getName());
    }

    public String getMacAlgorithm() {
        return macAlgorithm;
    }

    public int getMacLength() {
        return macLength;
    }

    /*
     public static byte[] sha256(byte[] messsage) {
     try {
     MessageDigest hash = MessageDigest.getInstance(SHA256_ALGORITHM);
     hash.update(messsage);
     return hash.digest();
     }
     catch(NoSuchAlgorithmException e) {
     throw new IllegalArgumentException("No such algorithm: "+SHA256_ALGORITHM, e);
     }
     }*/
    public byte[] hmac(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(macAlgorithm); // throws NoSuchAlgorithmException
        SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(key, macAlgorithm);
        mac.init(secret_key); // throws InvalidKeyException
        mac.update(message);
        return mac.doFinal();
    }

    /**
     *
     * @param ikm input keying material
     * @param length of derived key to return
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] ikm, int length) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] salt = new byte[macLength];
        Arrays.fill(salt, (byte) 0x00);
        byte[] info = new byte[0];
        return deriveKey(salt, ikm, length, info);
    }

    /**
     *
     * @param ikm input keying material
     * @param length of derived key to return
     * @param info optional context and application-specific information; must
     * not be null; can be zero-length
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] ikm, int length, byte[] info) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] salt = new byte[macLength];
        Arrays.fill(salt, (byte) 0x00);
        return deriveKey(salt, ikm, length, info);
    }

    /**
     *
     * @param salt a non-secret random value; must not be null
     * @param ikm input keying material
     * @param length of derived key to return
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] salt, byte[] ikm, int length) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] info = new byte[0];
        return deriveKey(salt, ikm, length, info);
    }

    /**
     *
     * @param salt a non-secret random value; must not be null
     * @param ikm input keying material
     * @param length of derived key to return
     * @param info optional context and application-specific information; must
     * not be null; can be zero-length
     * @return key derived using HKDF algorithm with given parameters
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public byte[] deriveKey(byte[] salt, byte[] ikm, int length, byte[] info) throws NoSuchAlgorithmException, InvalidKeyException {
        if (length > 255 * macLength) {
            throw new InvalidParameterException("length");
        }
        if (salt.length == 0) {
            salt = new byte[macLength];
            Arrays.fill(salt, (byte) 0x00);
        }
        // step 1. extract.  HKDF-Extract(salt, IKM) -> PRK
        byte[] prk = hmac(salt, ikm);
        // step 2. expand. HKDF-Expand(PRK, info, L) -> OKM
        Mac mac = Mac.getInstance(macAlgorithm);
        mac.init(new javax.crypto.spec.SecretKeySpec(prk, macAlgorithm));
        int N = (int) Math.ceil((double) length / macLength); // N is the number of blocks we need to produce, T(1) .. T(N)
        int r = length % macLength; // the remainder is the number of bytes we need from the last block T(N), for example if maclength=32 and length=42, then r=10 
        byte[] okm = new byte[length];
        byte[] T = new byte[0];
        for (byte i = 1; i <= N; i++) {
            mac.update(T);
            mac.update(info);
            mac.update(i); // if i were an int, we'd have to convert with (byte) (i & 0xff) to achieve 0x01, 0x02, etc.
            T = mac.doFinal();
            System.arraycopy(T, 0, okm, (i - 1) * macLength, (i < N ? macLength : r));
        }
        return okm;
    }
}
