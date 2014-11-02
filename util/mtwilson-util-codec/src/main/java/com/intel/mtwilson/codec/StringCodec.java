/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import java.nio.charset.Charset;

/**
 * Example of an ObjectCodec which handles only Strings and can be 
 * initialized with a specific character set. It relies on the String
 * class for encoding and decoding.
 * 
 * Note that this class does not encode arbitrary objects as strings - 
 * it only encodes strings into byte arrays and decodes byte arrays to
 * strings. So this class is NOT suitable for "chaining" encoders where 
 * one encoder converts any Object to a byte array and a second encoder
 * converts the byte array to hex or base64. 
 * 
 * @author jbuhacoff
 */
public class StringCodec implements ObjectCodec {
    private final Charset charset;
    
    public StringCodec() {
        this.charset = Charset.forName("UTF-8");
    }
    
    public StringCodec(Charset charset) {
        this.charset = charset;
    }
    
    @Override
    public byte[] encode(Object input) {
        if( input instanceof String ) {
            String str = (String)input;
            return str.getBytes(charset);
        }
        throw new UnsupportedOperationException(String.format("Cannot encode %s",input.getClass().getName()));
    }

    @Override
    public String decode(byte[] encoded) {
        return new String(encoded, charset);
    }
    
}
