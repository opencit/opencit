/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Usage example:
        HexCodec hex = new HexCodec();
        String encoded = hex.encode(bytes);
        log.debug("encoded: {}", encoded);
        byte[] decodedBytes = hex.decode(encoded);
 * 
 * @author jbuhacoff
 */
public class HexCodec implements ByteArrayCodec {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HexCodec.class);
    
    private boolean normalizeInput;

    /**
     * Default value is false. When enabled, the input is pre-processed
     * to remove any non-base64 characters before decoding.
     * @param normalizeInput 
     */
    public void setNormalizeInput(boolean normalizeInput) {
        this.normalizeInput = normalizeInput;
    }
    
    
    @Override
    public String encode(byte[] input) {
        log.debug("encode byte[] to string");
        return Hex.encodeHexString(input);
    }

    @Override
    public byte[] decode(String encoded) {
        try {
            String input = normalizeInput ? HexUtil.normalize(encoded) : encoded;
            log.debug("hex decode normalized? {} input: '{}' length: {}", normalizeInput, input, input.length());
            return Hex.decodeHex(input.toCharArray());
        }
        catch(DecoderException e) {
            throw new HexFormatException(e);
        }
    }
    
}
