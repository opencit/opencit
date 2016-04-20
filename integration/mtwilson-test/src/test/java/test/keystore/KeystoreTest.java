/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.keystore;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class KeystoreTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeystoreTest.class);

    @Test
    public void testOpenKeystore() throws Exception  {
        String password = "UQwxoM65xZg_";
        byte[] keystoreBytes = IOUtils.toByteArray(getClass().getResourceAsStream("/keystore/mw_portal_user_keystore1.jks"));
        log.debug("keystore size {}", keystoreBytes.length);
        ByteArrayResource resource = new ByteArrayResource(keystoreBytes);
        SimpleKeystore keystore = new SimpleKeystore(resource, password);
    }
}
