/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.configuration;

import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import org.junit.Test;
import java.util.Properties;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author jbuhacoff
 */
public class EncryptedConfigurationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncryptedConfigurationTest.class);

    @Test
    public void testStoreEncryptedConfiguration() throws Exception {
        // create a configuration to encrypt
        Properties properties = new Properties();
        properties.setProperty("foo", "bar");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        properties.store(out, null);
        String content = out.toString();
        log.debug("content {}", content);
        // prepare the file encryption
        String password = RandomUtil.randomBase64String(8);
        log.debug("password {}", password);
        ByteArrayResource resource = new ByteArrayResource();
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password);
        encryptedFile.saveString(content);
        // show the encrypted content
        String encryptedContent = new String(resource.toByteArray());
        log.debug("encrypted content {}", encryptedContent);
    }
    
    /**
     * Example output:
#Mon Mar 03 16:54:35 PST 2014
foo=bar
     * 
     */
    @Test
    public void testLoadEncryptedConfiguration3DES() throws Exception {
        String encryptedContent = "-----BEGIN ENCRYPTED DATA-----\n" +
"Content-Encoding: base64\n" +
"Encryption-Algorithm: PBEWithSHA1AndDESede/CBC/PKCS5Padding\n" +
"Encryption-Key-Id: pqjdCucjUPI=:XLuvEmCFhax5Sbc+K5CzVu9j8kuMoWjdpfxlHgo3Sn4=\n" +
"Integrity-Algorithm: SHA256\n" +
"\n" +
"0Q0bnmbGvwDoaX4CCXts6z28aHuDnL2WdjHaEXY8jPPRFwSxCbsbtrV2PFJOmMbERl1qzW+0GEDX\n" +
"ATGdnkDKYJ/RNQMvxcFTQgakoBCzKpD6zLu9Ji3fYA==\n" +
"-----END ENCRYPTED DATA-----\n" +
"";
        String password = "wDHjjAqr6F4=";
        // decrypt
        ByteArrayResource resource = new ByteArrayResource(encryptedContent.getBytes());
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(resource, password);
        String content = encryptedFile.loadString();
        log.debug("decrypted {}", content);
    }
}
