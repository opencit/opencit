/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

/**
 * Interface for encoding byte[] to String and decoding String back to byte[]
 *
 * @author jbuhacoff
 */
public interface ByteArrayCodec {
    /**
     * 
     * @param input must not be null
     * @return a string representation of the input
     * @throws NullPointerException if the input is null
     */
    String encode(byte[] input);
    
    /**
     * A decoder should return a zero-length array if the input is the
     * empty string.
     * 
     * @param encoded must not be null
     * @return the byte array represented by the input
     * @throws NullPointerException if the input is null
     */
    byte[] decode(String encoded);
}
