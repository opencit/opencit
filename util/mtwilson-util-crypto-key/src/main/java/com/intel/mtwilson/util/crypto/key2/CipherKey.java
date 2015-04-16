/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.key2;

/**
 *
 * @author jbuhacoff
 */
public class CipherKey extends CipherKeyAttributes {
//    public static enum Attributes { cipherKeyId, encoded; }
    private byte[] encoded;
    

    
    
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
//        this.keyId = source.keyId;
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
