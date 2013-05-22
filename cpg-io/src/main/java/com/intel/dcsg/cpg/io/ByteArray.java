/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

/**
 * @since 0.1
 * @author jbuhacoff
 */
public class ByteArray {
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
    
    public byte[] getBytes() { return array; }
    
    public int length() { return array.length; }
    
    public ByteArray append(byte[] more) {
        return new ByteArray(concat(array, more));
    }
    
    public ByteArray subarray(int offset) {
        return new ByteArray(subarray(array, offset));
    }

    public ByteArray subarray(int offset, int length) {
        return new ByteArray(subarray(array, offset, length));
    }
    
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
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

    
}
