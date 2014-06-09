/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.attestation;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.util.ByteArray;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.IPv4Address;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class NonceExtensionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NonceExtensionTest.class);

    @Test
    public void testIdentityAwareChallenger() {
        byte[] nonce = RandomUtil.randomByteArray(16);
        assertEquals(16, nonce.length);
        log.debug("nonce: {}", Hex.encodeHexString(nonce));
        byte[] ipv4 = new IPv4Address("192.168.1.100").toByteArray();
        assertEquals(4, ipv4.length);
        log.debug("ipv4: {}", Hex.encodeHexString(ipv4));
        byte[] extended = Sha1Digest.digestOf(nonce).extend(ipv4).toByteArray();
        assertEquals(20, extended.length);
        log.debug("extended nonce: {}", Hex.encodeHexString(extended));
    }
    
    @Test
    public void testPrivacyGuardedChallenger() {
        byte[] challengerNonce = RandomUtil.randomByteArray(16);
        assertEquals(16, challengerNonce.length);
        log.debug("challenger nonce: {}", Hex.encodeHexString(challengerNonce));
        
        // now challenger submits the nonce to mtwilson and mtwilson will calculate the extended nonce and provide it to the challenger
        byte[] nonce = RandomUtil.randomByteArray(16);
        assertEquals(16, nonce.length);
        log.debug("nonce: {}", Hex.encodeHexString(nonce));
        byte[] ipv4 = new IPv4Address("192.168.1.100").toByteArray();
        assertEquals(4, ipv4.length);
        log.debug("ipv4: {}", Hex.encodeHexString(ipv4));
        byte[] extended = Sha1Digest.digestOf(nonce).extend(ipv4).extend(challengerNonce).toByteArray();
        assertEquals(20, extended.length);
        log.debug("extended nonce: {}", Hex.encodeHexString(extended));
        
        // mtwilson reports to the challenger only the last intermediate nonce:
        byte[] attestationServiceNonce = Sha1Digest.digestOf(nonce).extend(ipv4).toByteArray();
        log.debug("attestation service nonce: {}", Hex.encodeHexString(attestationServiceNonce));
        // challenger can verify the attestation service nonce combined with challenger's own nonce is what the host used when signing:
        byte[] verifyExtended = Sha1Digest.valueOf(attestationServiceNonce).extend(challengerNonce).toByteArray();
        assertArrayEquals(extended, verifyExtended);
        log.debug("verify extended nonce: {}", Hex.encodeHexString(verifyExtended));
        byte[] verifyExtended2 = Sha1Digest.digestOf(ByteArray.concat(attestationServiceNonce,challengerNonce)).toByteArray();
        log.debug("verify extended nonce 2 : {}", Hex.encodeHexString(verifyExtended2));
    }
    
    @Test
    public void testGetPrivacyGuardedHostId() {
        UUID hostId = new UUID();
        log.debug("host id (not hardware uuid or ip address): {}", hostId.toString());
    }
    
}
