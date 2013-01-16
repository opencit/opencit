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
 * A simple implementation of Resource using a byte[] as the storage.
 * 
 * You can extend this class to implement custom actions when the
 * resource is updated by overriding the onClose() method. The default
 * implementation of onClose() is a no-op. 
 * 
 * @author jbuhacoff
 */
public class ByteArrayResource implements Resource {
    protected byte[] array;
    
    public ByteArrayResource() {
        this.array = null;
    }
    
    public ByteArrayResource(byte[] array) {
        this.array = array;
    }
    
    @Override
    public InputStream getInputStream() {
        if( array == null || array.length == 0 ) { return null; }
        return new ByteArrayInputStream(array);
    }

    @Override
    public OutputStream getOutputStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                array = toByteArray(); // calls toByteArray() in ByteArrayOutputStream
                onClose();
            }
        };
    }
    
    public byte[] toByteArray() { return array; }
    
    protected void onClose() { }
    
}
