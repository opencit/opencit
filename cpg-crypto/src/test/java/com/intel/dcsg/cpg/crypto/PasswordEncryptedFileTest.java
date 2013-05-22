/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class PasswordEncryptedFileTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testEncryptText() throws IOException {
        String content = "hello world";
        String password = "password";
        ByteArrayResource resource = new ByteArrayResource();
        PasswordEncryptedFile enc = new PasswordEncryptedFile(resource, password);
        enc.saveString(content);
        
        String encryptedContentEnvelope = new String(resource.toByteArray());
        log.debug("Input content: {}", content);
        log.debug("Encrypted file: {}", encryptedContentEnvelope);
        
        String decryptedContent = enc.loadString();
        log.debug("Decrypted content: {}", decryptedContent);
        assertEquals(content, decryptedContent);
    }
}
