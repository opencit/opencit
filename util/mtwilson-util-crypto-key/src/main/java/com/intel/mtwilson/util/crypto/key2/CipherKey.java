/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Map;

/**
 *
 * @author jbuhacoff
 */
public class CipherKey extends CipherKeyAttributes {
//    public static enum Attributes { cipherKeyId, encoded; }
    private String cipherKeyId;
    private byte[] encoded;
    
    /**
     * The key id can be used to look up the key in a database or 
     * key server when the encoded key is not present
     */
    public String getCipherKeyId() {
        return cipherKeyId;
//        return (String)attributes.get(Attributes.cipherKeyId.name());
    }

    public void setCipherKeyId(String cipherKeyId) {
        this.cipherKeyId = cipherKeyId;
//        attributes.put(Attributes.cipherKeyId.name(), cipherKeyId);
    }

    
    
    /**
     * The encoded key, in the format specified by its attributes
     */
    

    public byte[] getEncoded() {
        return encoded;
//        return (byte[])attributes.get(Attributes.encoded.name());
    }

    public void setEncoded(byte[] encoded) {
        this.encoded = encoded;
//        attributes.put(Attributes.encoded.name(), encoded);
    }

    
    

    @Override
    public CipherKey copy() {
        CipherKey newInstance = new CipherKey();
        newInstance.copyFrom(this);
//        copy.attributes = super.copy().attributes;
//        copy.cipherKeyId = this.cipherKeyId;
//        copy.encoded = this.encoded;
        return newInstance;
    }
    
    public void copyFrom(CipherKey source) {
        super.copyFrom(source);
        this.cipherKeyId = source.cipherKeyId;
        this.encoded = source.encoded;
    }

    /*
    @JsonAnyGetter
    @Override
    public Map<String, Object> getAttributeMap() {
        return super.getAttributeMap();
    }

    @JsonAnySetter
    @Override
    public void setAttributeMap(Map<String, Object> map) {
        super.setAttributeMap(map);
    }
    */
}
