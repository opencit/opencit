/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A thin wrapper for Java's File class
 * @author jbuhacoff
 */
public class ByteArrayResource implements Resource {
    private byte[] array;
    
    public ByteArrayResource() {
        this.array = new byte[] {};
    }
    
    public ByteArrayResource(byte[] array) {
        this.array = array;
    }
    
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(array);
    }

    @Override
    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                array = toByteArray();
            }
        };
    }
    
    public byte[] toByteArray() { return array; }
    
}
