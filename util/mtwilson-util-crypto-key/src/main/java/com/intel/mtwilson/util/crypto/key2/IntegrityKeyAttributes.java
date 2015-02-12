/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import com.intel.dcsg.cpg.io.Attributes;
import com.intel.dcsg.cpg.io.Copyable;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class IntegrityKeyAttributes extends Attributes implements Copyable {
    private String keyId;
    private String algorithm;
    private Integer keyLength;
    private List<String> manifest;
    
    /**
     * The key id can be used to look up the key in a database or 
     * key server when the encoded key is not present
     */
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
    
    /**
     * Refers to the signature algorithm, for example RSA-SHA256 
     * or HMAC-SHA256.  This includes choice of RSA or HMAC as well as
     * the digest algorithm used with it. 
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
     * For HMAC, the key length can be any length up to B, the block length of the
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
     * 
     * For RSA, the key length can be any valid RSA key length such as 1024
     * or 2048.
     */
    public Integer getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }

    /**
     * The manifest indicates what is being covered by the 
     * integrity signature. It could be a document, or a
     * document and its metadata, or some other set of related
     * items. The manifest is an ordered list - the items
     * are concatenated in the order specified in the manifest
     * before applying the digest.
     * 
     * @return 
     */
    public List<String> getManifest() {
        return manifest;
    }

    public void setManifest(List<String> manifest) {
        this.manifest = manifest;
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
        this.keyId = source.keyId;
        this.algorithm = source.algorithm;
        this.keyLength = source.keyLength;
    }

}
