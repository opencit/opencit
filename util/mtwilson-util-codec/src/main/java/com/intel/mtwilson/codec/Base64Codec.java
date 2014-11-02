/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

import org.apache.commons.codec.binary.Base64;

/**
 * Usage example:
 * 
 * <pre>
        Base64Codec base64 = new Base64Codec();
        String encoded = base64.encode(bytes);
        log.debug("encoded: {}", encoded);
        byte[] decodedBytes = base64.decode(encoded);
 * </pre>
 * 
 * The chunk and newline settings should really be in a separate
 * String transformation
 * class. 
 * 
 * @author jbuhacoff
 */
public class Base64Codec implements ByteArrayCodec {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Base64Codec.class);
    private String newline = "\r\n";
    private int chunkSize = 76;
    private boolean normalizeInput = false;
    private boolean chunkOutput = false;

    /**
     * Default value is false. When enabled, the input is pre-processed
     * to remove any non-base64 characters before decoding.
     * @param normalizeInput 
     */
    public void setNormalizeInput(boolean normalizeInput) {
        this.normalizeInput = normalizeInput;
    }

    /**
     * Default newline is CRLF, use this method
     * to change it. The newline setting only
     * affects the output when chunking is enabled.
     * @param newline 
     */
    public void setNewline(String newline) {
        this.newline = newline;
    }

    /**
     * Default setting is false which results in 
     * one line of encoded output. Enable this
     * setting to get "paragraphs" of output. 
     * @param chunkOutput 
     */
    public void setChunkOutput(boolean chunkOutput) {
        this.chunkOutput = chunkOutput;
    }

    /**
     * Default value is 76. Only effective when chunkOutput is enabled.
     * 
     * @param chunkSize 
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    
    
    
    @Override
    public String encode(byte[] input) {
        log.debug("encode byte[] to string");
        String base64 = Base64.encodeBase64String(input);
        return chunkOutput ? chunk(base64) : base64;
    }

    @Override
    public byte[] decode(String encoded) {
        log.debug("decode string to byte[]");
        String input = normalizeInput ? Base64Util.normalize(encoded) : encoded;
        return Base64.decodeBase64(input);
    }

    /**
     * This method is used to chunk the single-line output of Base64.encodeBase64String
     * 
     * XXX TODO sumbmit a feature request to Apache Commons to add 
     * Base64.encodeBase64StringChunked defined as chunk(Base64.encodeBase64String)
     * because currently the only method for chunking is Base64.encodeBase64Chunked
     * which returns a byte array
     * 
     * @param input any non-null string
     * @return the same string with carriage return + newlines inserted every 76 characters
     */
    private String chunk(String input) {
        int cursor = 0;
        int max = input.length();
        StringBuilder output = new StringBuilder();
        do {
            output.append(input.substring(cursor, Math.min(cursor+chunkSize, max)));
            output.append(newline);
            cursor += chunkSize;
        } while(cursor < max);
        return output.toString().trim(); // remove trailing newlines
    }
    
}
