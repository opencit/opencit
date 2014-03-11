/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.crypto;

import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import java.io.File;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class EncryptFileTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncryptFileTest.class);
    
    @Test
    public void testEncryptFile() throws Exception {
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        ByteArrayResource resource = new ByteArrayResource();
        String password = "password";
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password, protection);
        encryptedFile.saveString("hello world");
        log.debug("encrypted: {}", new String(resource.toByteArray()));
    }
}
