/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

/**
 * Interface for encoding Object to byte[] and decoding byte[] back to Object
 * 
 * @author jbuhacoff
 */
public interface ObjectCodec {
    /**
     * 
     * @param input must not be null
     * @return a byte array representation of the input
     * @throws NullPointerException if the input is null
     */
    byte[] encode(Object input);
    
    /**
     * A decoder should return a zero-length array if the input is the
     * empty string.
     * 
     * @param encoded must not be null
     * @return the byte array represented by the input
     * @throws NullPointerException if the input is null
     */
    Object decode(byte[] encoded);
}
