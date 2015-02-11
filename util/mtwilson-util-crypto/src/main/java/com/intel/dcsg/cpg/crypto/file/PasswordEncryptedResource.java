/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.file;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.io.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Because the PasswordEncryptedFile class doesn't support streams, the workaround
 * is to buffer the entire input or output and then pass it to the PasswordEncryptedFile.
 * This means we don't know if there was an error until the caller calls
 * close() on the OutputStream. 
 * 
 * @author jbuhacoff
 */
public class PasswordEncryptedResource implements Resource {
    private PasswordEncryptedFile encfile = null;
    
    public PasswordEncryptedResource(Resource resource, String password, PasswordProtection protection) {
        this.encfile = new PasswordEncryptedFile(resource, password, protection);
    }
    
    /**
     * Caller is responsible for closing the stream
     * @return
     * @throws IOException 
     */
    @Override
    public InputStream getInputStream() throws IOException {
        byte[] buffer = encfile.decrypt();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        return in;
    }

    /**
     * Caller is responsible for closing the stream. Current implementation
     * only writes out the encrypted file when caller calls close().
     * @return
     * @throws IOException 
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        // subclass OutputStream instead of ByteArrayOutputStream because
        // OutputStream's close() throws IOException whereas 
        // ByteArrayOutputStream's close() does not throw exceptions
        return new OutputStream() {
            private ByteArrayOutputStream out = new ByteArrayOutputStream();
            @Override
            public void close() throws IOException {
                encfile.encrypt(out.toByteArray());
            }

            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };
    }
    
}
