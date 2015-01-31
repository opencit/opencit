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
    private String cipherAlgorithm;
    private Integer cipherKeyLength;
    private String cipherMode;
    private String cipherPaddingMode;
    
    /**
     * For a certificate, refers to the algorithm of the enclosed public key.
     * Examples: AES, RSA
     */
    
    
    public String getCipherAlgorithm() {
        return cipherAlgorithm;
//        return (String)attributes.get(Attributes.cipherAlgorithm.name());
    }

    public void setCipherAlgorithm(String cipherAlgorithm) {
        this.cipherAlgorithm = cipherAlgorithm;
//        attributes.put(Attributes.cipherAlgorithm.name(), cipherAlgorithm);
    }

    
    /**
     * In bits, and always refers to the plaintext key without any
     * encoding or encryption. For certificates, this refers to the
     * length of the enclosed public key. 
     * Examples of key length for AES: 128, 256
     * Examples of key length for RSA: 1024, 2048
     */
    
    public Integer getCipherKeyLength() {
        return cipherKeyLength;
//        return (Integer)attributes.get(Attributes.cipherKeyLength.toString());
    }

    public void setCipherKeyLength(Integer cipherKeyLength) {
        this.cipherKeyLength = cipherKeyLength;
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
    
    public String getCipherMode() {
        return cipherMode;
//        return (String)attributes.get(Attributes.cipherMode.toString());
    }

    public void setCipherMode(String cipherMode) {
        this.cipherMode = cipherMode;
//        attributes.put(Attributes.cipherMode.name(), cipherMode);
    }

    
    /**
     * Padding mode used with this key. 
     * Examples: 
     */

    public String getCipherPaddingMode() {
        return cipherPaddingMode;
//        return (String)attributes.get(Attributes.cipherPaddingMode.toString());
    }

    public void setCipherPaddingMode(String cipherPaddingMode) {
        this.cipherPaddingMode = cipherPaddingMode;
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
        this.cipherAlgorithm = source.cipherAlgorithm;
        this.cipherKeyLength = source.cipherKeyLength;
        this.cipherMode = source.cipherMode;
        this.cipherPaddingMode = source.cipherPaddingMode;
    }

}
