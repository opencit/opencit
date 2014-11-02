/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import java.security.KeyPair;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class RsaSignatureMessageTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RsaSignatureMessageTest.class);
    private static KeyPair keypair;
    private static RsaSignatureMessageWriter writer;

    @BeforeClass
    public static void init() throws Exception {
        keypair = RsaUtil.generateRsaKeyPair(1024);
        writer = new RsaSignatureMessageWriter();
        writer.setPrivateKey(keypair.getPrivate());
    }
    
    /**
     * Example message output:
     * 
Content-Length: 128
Content-Transfer-Encoding: base64
Content-Type: text/plain; charset="UTF-8"

Ne1pGsN0aAiwZyCWrT95e86sJMo2l00aV8lw+Y4hmj8mKDziYmT5A5G8n9hw+KDKN343mWE8RFQ8
9q72DP29ziNdK9F3A4rv3IrAuLLx4sDusZS4MAzz1vWvF5HiJY4+xxp3WTPVKoHXcVUC+HLhxOBz
8N8mVLqLOO0VZEYZdEM=
     * 
     * 
     * Example base64 encoded message output (an entire message as shown above):
     * 
Q29udGVudC1MZW5ndGg6IDEyOA0KQ29udGVudC1UcmFuc2Zlci1FbmNvZGluZzogYmFzZTY0DQpDb250ZW50LVR5cGU6IHRleHQvcGxhaW47IGNoYXJzZXQ9IlVURi04Ig0KDQpKa00wMU1OSEFKUUtZUG4rWTA1SUl3MUdKSmlNVElQVkdid3U3TFd1bjdQNDFaUzdraytVVWRMalVBOEtqa3QvVERxaHQvVENVMFJvDQpDOWNKSEpraGVRZ2YxZlhURFMwS2VqZGdlTU5SdnRFQUhEc2t3bGtwbTVOZld3eDdlbERmT2ZPUFpaZWF1TkFzdkV4WktlTENrYzdlDQo5Qm1JcTFOWnJsTkhXaUVvY1JZPQ0K
     * 
     */
    @Test
    public void testSignString() {
        byte[] message = writer.writeString("hello world");
        log.debug("Message: {}", new String(message));
        log.debug("Message base64: {}", Base64.encodeBase64String(message));
    }
    
}
