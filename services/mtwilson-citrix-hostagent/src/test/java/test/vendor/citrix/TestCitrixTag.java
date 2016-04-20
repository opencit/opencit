/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.citrix;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.agent.citrix.CitrixClient;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class TestCitrixTag {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestCitrixTag.class);

    @Test
    public void testCitrixTag() {
        // given this tag:
        Sha1Digest tag = Sha1Digest.valueOf("50108aa703f8f2bea8a705a4e958ccc80aea2fea");
        log.debug("Asset tag: {}", tag.toHexString());
        // what we send to citrix is base64(hex-tag):
        String citrix = Base64.encodeBase64String(tag.toHexString().getBytes());
        log.debug("Send to citrix base64 of hex tag: {}", citrix);
        // they will base64-decode it to get back the hex-tag, and they write the hex-tag to the nvram
        Sha1Digest pcr22 = Sha1Digest.ZERO.extend( Sha1Digest.digestOf(tag.toHexString().getBytes()) );
        log.debug("PCR: {}", pcr22.toHexString()); // 0c9d49067ed4d013ee715a5dcf7be8e3ae4fd646
        // server actual pcr 22 value: 0c9d49067ed4d013ee715a5dcf7be8e3ae4fd646  
    }

    
    @Test
    public void testPushTag() throws Exception {
        CitrixClient client = new CitrixClient(new TlsConnection(new URL("https://10.1.71.91:443/;root;P@ssw0rd"), new InsecureTlsPolicy()));
        client.init();
        client.setAssetTag(Sha1Digest.ZERO);
    }
}
