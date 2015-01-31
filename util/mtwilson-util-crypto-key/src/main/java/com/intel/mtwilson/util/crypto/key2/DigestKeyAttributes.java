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
public class DigestKeyAttributes extends Attributes implements Copyable {
    private String digestAlgorithm;
    private Integer digestKeyLength;
    
    /**
     * Refers to the digest algorithm used in HMAC-digestAlgorithm.
     * Examples: MD5, SHA-1, SHA-256
     */
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
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

    
    public Integer getDigestKeyLength() {
        return digestKeyLength;
    }

    public void setDigestKeyLength(Integer digestKeyLength) {
        this.digestKeyLength = digestKeyLength;
    }


    
    @Override
    public DigestKeyAttributes copy() {
        DigestKeyAttributes newInstance = new DigestKeyAttributes();
        newInstance.copyFrom(this);
//        copy.digestAlgorithm = this.digestAlgorithm;
//        copy.digestKeyLength = this.digestKeyLength;
        return newInstance;
    }
    
    public void copyFrom(DigestKeyAttributes source) {
        super.copyFrom(source);
        this.digestAlgorithm = source.digestAlgorithm;
        this.digestKeyLength = source.digestKeyLength;
    }

}
