/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import com.intel.dcsg.cpg.io.Copyable;

/**
 * Extensible with new attributes via the attributes map and Jackson's
 * annotations JsonAnyGetter and JsonAnySetter.  
 * Note: if attributes was another object instead of a map we could have
 * used the JsonUnwrapped annotation instead to flatten its attributes
 * into this parent class.
 * 
 * @author jbuhacoff
 */
public class CipherKeyAttributes extends Attributes implements Copyable {
//    public static enum Attributes { cipherAlgorithm, cipherKeyLength, cipherMode, cipherPaddingMode; }
    private String algorithm;
    private Integer keyLength;
    private String mode;
    private String paddingMode;
    
    /**
     * For a certificate, refers to the algorithm of the enclosed public key.
     * Examples: AES, RSA
     */
    
    
    public String getAlgorithm() {
        return algorithm;
//        return (String)attributes.get(Attributes.cipherAlgorithm.name());
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
//        attributes.put(Attributes.cipherAlgorithm.name(), cipherAlgorithm);
    }

    
    /**
     * In bits, and always refers to the plaintext key without any
     * encoding or encryption. For certificates, this refers to the
     * length of the enclosed public key. 
     * Examples of key length for AES: 128, 256
     * Examples of key length for RSA: 1024, 2048
     */
    
    public Integer getKeyLength() {
        return keyLength;
//        return (Integer)attributes.get(Attributes.cipherKeyLength.toString());
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
//        attributes.put(Attributes.cipherKeyLength.name(), cipherKeyLength);
    }
    
    
    
    /**
     * Cipher mode of operation for this key. 
     * Using the same key for multiple modes is strongly not recommended
     * because it could aid cryptanalysis. Therefore the cipher mode is
     * required so that all users of the key know which cipher mode should
     * be used with it.
     * Examples: CBC, OFB
     */
    
    public String getMode() {
        return mode;
//        return (String)attributes.get(Attributes.cipherMode.toString());
    }

    public void setMode(String mode) {
        this.mode = mode;
//        attributes.put(Attributes.cipherMode.name(), cipherMode);
    }

    
    /**
     * Padding mode used with this key. For example: ANSIX923, ISO10126,
     * None, PKCS7, Zeros, PKCS15, OAEP.
     * Examples: 
     */

    public String getPaddingMode() {
        return paddingMode;
//        return (String)attributes.get(Attributes.cipherPaddingMode.toString());
    }

    public void setPaddingMode(String paddingMode) {
        this.paddingMode = paddingMode;
//        attributes.put(Attributes.cipherPaddingMode.name(), cipherPaddingMode);
    }

    

    @Override
    public CipherKeyAttributes copy() {
        CipherKeyAttributes newInstance = new CipherKeyAttributes();
        newInstance.copyFrom(this);
//        copy.attributes = super.copy().attributes;
//        copy.cipherAlgorithm = this.cipherAlgorithm;
//        copy.cipherKeyLength = this.cipherKeyLength;
//        copy.cipherMode = this.cipherMode;
//        copy.cipherPaddingMode = this.cipherPaddingMode;
        return newInstance;
    }
    
    public void copyFrom(CipherKeyAttributes source) {
        super.copyFrom(source);
        this.algorithm = source.algorithm;
        this.keyLength = source.keyLength;
        this.mode = source.mode;
        this.paddingMode = source.paddingMode;
    }

}
