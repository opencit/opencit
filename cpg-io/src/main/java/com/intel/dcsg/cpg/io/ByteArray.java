/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

import java.math.BigInteger;
import java.util.Scanner;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @since 0.1
 * @author jbuhacoff
 */
public class ByteArray {
    public static final ByteArray EMPTY = new ByteArray(); // for convenience
    private byte[] array;
    
    public ByteArray() {
        array = new byte[0];
    }
    public ByteArray(byte[] array) {
        if( array == null ) {
            this.array = new byte[0];
        }
        else {
            this.array = array;
        }
    }
    
    /**
     * If the big integer is positive and 128 bits long, the resulting byte array will be 17 bytes not 16 because it will have
     * a leading zero to indicate the integer is positive. That way when you convert the array back to BigInteger you will
     * get the correct result. If we were to strip off the leading zero then when ou convert back to BigInteger you will get
     * something negative which would not be the same as the original BigInteger. 
     * Therefore callers should be aware that when processing 128-bit big integers (well anytime they are the size of the boundary)
     * the array will have a leading zero for the sign bit, and if callers already know the big integer is positive the 
     * callers can strip off the sign bit.
     * @since 0.1.1
     * @param number 
     */
    public ByteArray(BigInteger number) {
        this(number.toByteArray());
    }
    
    public byte[] getBytes() { return array; }
    
    public int length() { return array.length; }
    
    /**
     * @since 0.1.1
     * @return 
     */
    public BigInteger toBigInteger() {
        return new BigInteger(1, array);
    }
    
    /**
     * @since 0.1.1
     * @return 
     */
    public String toHexString() {
        if( array.length == 0 ) { return ""; } // for ByteArray.EMPTY
        BigInteger value = toBigInteger();
        String hex = String.format("%0"+(array.length*2)+"x", value); // length is in bytes, hex is 2 characters per byte
        if( hex.length() > array.length*2 ) {
            throw new IllegalArgumentException(String.format("Overflow: hex %x does not fit in %d bytes", value, array.length));
        }
        return hex;
    }
    
    public ByteArray append(byte[] more) {
        return new ByteArray(concat(array, more));
    }
    
    public ByteArray subarray(int offset) {
        return new ByteArray(subarray(array, offset));
    }

    public ByteArray subarray(int offset, int length) {
        return new ByteArray(subarray(array, offset, length));
    }
    /*
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    */
    public static byte[] concat(byte[]... arrays) {
        int resultsize = 0;
        for(int i=0; i<arrays.length; i++) { 
            resultsize += arrays[i].length; 
        }
        byte[] result = new byte[resultsize];
        int cursor = 0;
        for(int i=0; i<arrays.length; i++) {
            System.arraycopy(arrays[i], 0, result, cursor, arrays[i].length);
            cursor += arrays[i].length;
        }
        return result;
    }
    
    /**
     * @since 0.1.1
     * @param arrays
     * @return 
     */
    public static byte[] concat(ByteArray... arrays) {
        int resultsize = 0;
        for(int i=0; i<arrays.length; i++) { 
            resultsize += arrays[i].length(); 
        }
        byte[] result = new byte[resultsize];
        int cursor = 0;
        for(int i=0; i<arrays.length; i++) {
            System.arraycopy(arrays[i].getBytes(), 0, result, cursor, arrays[i].length());
            cursor += arrays[i].length();
        }
        return result;        
    }
    
    
    public static byte[] subarray(byte[] a, int offset) {
        byte[] result = new byte[a.length - offset];
        System.arraycopy(a, offset, result, 0, a.length - offset);
        return result;
    }
    
    public static byte[] subarray(byte[] a, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(a, offset, result, 0, length);
        return result;
    }

    /**
     * @since 0.1.1
     * @param text hex string representing the byte array
     * @return 
     */
    public static ByteArray fromHex(String text) {
        Scanner scanner = new Scanner(text);
        ByteArray data = new ByteArray(scanner.nextBigInteger(16));
        scanner.close();
        // preserve leading zeros; BigInteger preserves at most one byte of leading zeros that it uses to indicate the number is positive. we are only using biginteger as a shortcut to parsing so we need to ensure all leading zeros are preserved.
        if( data.length() * 2 < text.length() && text.startsWith("00") ) {
            int size = (text.length() / 2) - data.length();
            byte[] zeros = new byte[size];
            data = new ByteArray(ByteArray.concat(zeros, data.getBytes()));
        }
        // strip off leading zero that biginteger inserts to indicate sign
        else if( data.length() * 2 > text.length() && data.getBytes()[0] == 0 ) {
            data = data.subarray(1);
        }
        return data;
    }
    
    
    /**
     * @since 0.1.1
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        if( other == null ) { return false; }
        if( other == this ) { return true; }
        if( other.getClass() != this.getClass() ) { return false; }
        ByteArray rhs = (ByteArray)other;
        return new EqualsBuilder().append(array, rhs.array).isEquals();
    }
    
    /**
     * 
     * @since 0.1.1
     * @return 
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(13,25).append(array).toHashCode();
    }
    
}
