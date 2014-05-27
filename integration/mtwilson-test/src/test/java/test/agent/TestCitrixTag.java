/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.agent;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author jbuhacoff
 */
public class TestCitrixTag {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestCitrixTag.class);

    @Test
    public void testCitrixTag() {
        // given this tag:
        //.valueOf
        Sha1Digest tag = Sha1Digest.valueOf("940a3e5c1610b686ee21bca1a648869c253bf626");
        log.debug("Asset tag: {}", tag.toHexString());
        // what we send to citrix is base64(hex-tag):
        String citrix = Base64.encodeBase64String(tag.toHexString().getBytes());
        log.debug("Send to citrix base64 of hex tag: {}", citrix);
        // they will base64-decode it to get back the hex-tag, and they write the hex-tag to the nvram
        Sha1Digest pcr22 = Sha1Digest.ZERO.extend(tag);// Sha1Digest.ZERO.extend( Sha1Digest.digestOf(tag.toHexString().getBytes()) );
        log.debug("PCR: {}", pcr22.toHexString()); // 0c9d49067ed4d013ee715a5dcf7be8e3ae4fd646
        // server actual pcr 22 value: 0c9d49067ed4d013ee715a5dcf7be8e3ae4fd646  
    }

}
