/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.digest;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.net.IPv4Address;
import com.intel.dcsg.cpg.io.ByteArray;
import java.nio.charset.Charset;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class HashExtensionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HashExtensionTest.class);

    @Test
    public void testExtendSha1() {
        Sha1Digest nonce = Sha1Digest.valueOf(RandomUtil.randomByteArray(20));
        log.debug("nonce = {}", nonce.toHexString());
        String ip = "127.0.0.1";
        String uuid = new UUID().toString();
        byte[] ipBytes = new IPv4Address(ip).toByteArray(); // 4 bytes
        byte[] uuidBytes = UUID.valueOf(uuid).toByteArray().getBytes(); // 16 bytes
        Sha1Digest nonce_ip_uuid = nonce.extend(ByteArray.concat(ipBytes,uuidBytes));
        log.debug("nonce+ip+uuid 1 = {}", nonce_ip_uuid.toHexString());
        Sha1Digest nonce_ip_uuid2 = nonce.extend(ipBytes).extend(uuidBytes);
        log.debug("nonce+ip+uuid 2 = {}", nonce_ip_uuid.toHexString());        
    }
    
    @Test
    public void testExtendSha256() {
        Sha256Digest nonce = Sha256Digest.valueOf(RandomUtil.randomByteArray(32));
        log.debug("nonce = {}", nonce.toHexString());
        String ip = "127.0.0.1";
        String uuid = new UUID().toString();
        byte[] ipBytes = new IPv4Address(ip).toByteArray(); // 4 bytes
        byte[] uuidBytes = UUID.valueOf(uuid).toByteArray().getBytes(); // 16 bytes
        Sha256Digest nonce_ip_uuid = nonce.extend(ByteArray.concat(ipBytes,uuidBytes));
        log.debug("nonce+ip+uuid 1 = {}", nonce_ip_uuid.toHexString());
        Sha256Digest nonce_ip_uuid2 = nonce.extend(ipBytes).extend(uuidBytes);
        log.debug("nonce+ip+uuid 2 = {}", nonce_ip_uuid.toHexString());        
    }
    
}
