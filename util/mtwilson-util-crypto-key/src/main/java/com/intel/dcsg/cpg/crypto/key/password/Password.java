/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

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
     * Creates an empty password
     */
    public Password() {
        this.password = new char[0];
    }
    
    /**
     * 
     * @param password character array to wrap; calling {@code clear()} will clear this array
     */
    public Password(char[] password) {
        if( password == null ) { this.password = new char[0]; }
        else {
        this.password = password;
        }
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
        if( bytes == null ) { this.password = new char[0]; }
        else {
        this.password = charset.decode(ByteBuffer.wrap(bytes)).array();
        }
    }
    
    /**
     * Changes to the returned array will not affect this instance.
     * @return a copy of the character array containing the password 
     */
    public char[] toCharArray() { return Arrays.copyOf(password, password.length); }
    
    /**
     * Changes to the returned array will not affect this instance.
     * @return a byte array representing the password in UTF-8 encoding
     */
    public byte[] toByteArray() { return toByteArray(Charset.forName("UTF-8")); }
    
    /**
     * Changes to the returned array will not affect this instance.
     * @param charset
     * @return a byte array representing the password in the encoding specified by the charset argument
     */
    public byte[] toByteArray(Charset charset) { return charset.encode(CharBuffer.wrap(password)).array(); }
    
    /**
     * Replaces each character in the password with NULL then empties the password.
     * After calling {@code clear()}, the {@code isEmpty()} method will return true.
     */
    public void clear() {
        Arrays.fill(password, '\u0000');
        password = new char[0];
    }
    
    /**
     * 
     * @return true if the password is null or empty
     */
    public boolean isEmpty() {
        return password == null || password.length == 0;
    }
}
