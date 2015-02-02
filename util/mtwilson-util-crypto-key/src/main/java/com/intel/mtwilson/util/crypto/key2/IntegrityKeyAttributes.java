/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import com.intel.dcsg.cpg.io.Copyable;

/**
 *
 * @author jbuhacoff
 */
public class IntegrityKeyAttributes extends Attributes implements Copyable {
    private String algorithm;
    private Integer keyLength;
    
    /**
     * Refers to the digest algorithm used in HMAC-digestAlgorithm.
     * Examples: MD5, SHA-1, SHA-256
     */
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }


    
    /**
     * In bits, and always refers to the plaintext key without any
     * encoding or encryption. 
     * The key length can be any length up to B, the block length of the
     * corresponding hash function. Key length should be at least L ,
     * the output length of the hash function. 
     * Therefore:   L lte K lte B.
     * 
     * For example:
     * MD5 input block size (B) is 512-bit (64 byte), output size (L) is 128-bit (16 byte)
     * SHA1 input block size (B) is 512-bit (64 byte), output size (L) is 160-bit (20 byte)
     * SHA224 input block size (B) is 512-bit (64 byte), output size (L) is 224-bit (28 byte)
     * SHA256 input block size (B) is 512-bit (64 byte), output size (L) is 256-bit (32 byte)
     * SHA384 input block size (B) is 1024-bit (128 byte), output size (L) is 384-bit (48 byte)
     * SHA512 input block size (B) is 1024-bit (128 byte), output size (L) is 512-bit (64 byte)
     */

    
    public Integer getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }


    
    @Override
    public IntegrityKeyAttributes copy() {
        IntegrityKeyAttributes newInstance = new IntegrityKeyAttributes();
        newInstance.copyFrom(this);
//        copy.digestAlgorithm = this.digestAlgorithm;
//        copy.digestKeyLength = this.digestKeyLength;
        return newInstance;
    }
    
    public void copyFrom(IntegrityKeyAttributes source) {
        super.copyFrom(source);
        this.algorithm = source.algorithm;
        this.keyLength = source.keyLength;
    }

}
