/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.key.Protection;
import com.intel.dcsg.cpg.crypto.key.ProtectionBuilder;
import com.intel.dcsg.cpg.crypto.key.ProtectionPolicy;
import com.intel.dcsg.cpg.crypto.key.ProtectionPolicyBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class ProtectionPolicyTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProtectionPolicyTest.class);
    
    @Test
    public void testAes128Stream() {
        ProtectionPolicy protectionPolicy = ProtectionPolicyBuilder.factory().stream().aes128().sha256().build();
        assertTrue(protectionPolicy.accept(ProtectionBuilder.factory().aes(128).mode("OFB8").padding("NoPadding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().aes(128).mode("CBC").padding("NoPadding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().aes(128).mode("CBC").padding("PKCS5Padding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().aes(128).mode("OFB8").padding("PKCS5Padding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().algorithm("3DES").mode("CBC").padding("PKCS5Padding").sha256().build()));
    }

    @Test
    public void testAes256Stream() {
        ProtectionPolicy protectionPolicy = ProtectionPolicyBuilder.factory().stream().aes256().sha256().build();
        assertTrue(protectionPolicy.accept(ProtectionBuilder.factory().aes(256).mode("OFB8").padding("NoPadding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().aes(192).mode("OFB8").padding("NoPadding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().aes(128).mode("OFB8").padding("NoPadding").sha256().build()));
        assertFalse(protectionPolicy.accept(ProtectionBuilder.factory().aes(256).mode("OFB8").padding("NoPadding").sha1().build()));
    }

}
