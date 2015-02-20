/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto.password;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 *
 * @author jbuhacoff
 */
public class Password {
    private char[] password;
    
    /**
     * 
     * @param password character array to wrap; calling {@code clear()} will clear this array
     */
    public Password(char[] password) {
        this.password = password;
    }
    
    /**
     * The byte array should be cleared if not needed after creating the Password object
     * @param bytes representing the password in UTF-8 encoding
     */
    public Password(byte[] bytes) {
        this(bytes, Charset.forName("UTF-8"));
    }
    
    /**
     * The byte array should be cleared if not needed after creating the Password object
     * @param bytes representing the password in the encoding specified by charset
     * @param charset for example UTF-8
     */
    public Password(byte[] bytes, Charset charset) {
        this.password = charset.decode(ByteBuffer.wrap(bytes)).array();
    }
    
    public char[] toCharArray() { return password; }
    
    /**
     * 
     * @return a byte array representing the password in UTF-8 encoding
     */
    public byte[] toByteArray() { return toByteArray(Charset.forName("UTF-8")); }
    
    /**
     * 
     * @param charset
     * @return a byte array representing the password in the encoding specified by the charset argument
     */
    public byte[] toByteArray(Charset charset) { return charset.encode(CharBuffer.wrap(password)).array(); }
    
    public void clear() {
        Arrays.fill(password, '\u0000');
        password = new char[0];
    }
}
