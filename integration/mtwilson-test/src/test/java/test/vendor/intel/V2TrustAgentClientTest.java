/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.intel;

import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.io.IOException;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import com.intel.mtwilson.trustagent.client.jaxrs.TrustAgentClient;
import com.intel.mtwilson.trustagent.model.HostInfo;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;

/**
 *
 * @author jbuhacoff
 */
public class V2TrustAgentClientTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static TrustAgentClient client;
    
    @BeforeClass
    public static void createClient() throws Exception {
        TlsConnection tlsConnection = new TlsConnection(new URL("https://10.1.71.96:1443/v2"), new InsecureTlsPolicy());
        Properties properties = new Properties();
        properties.setProperty("mtwilson.api.username", "mtwilson");
        properties.setProperty("mtwilson.api.password", "");
        client = new TrustAgentClient(properties, tlsConnection);
    }
    
    @Test
    public void testHostInfoCommand() throws Exception {
        HostInfo hostInfo = client.getHostInfo();
        ObjectMapper mapper = new ObjectMapper();
        log.debug(mapper.writeValueAsString(hostInfo));
    }
    
//    @Test
    public void testSetAssetTagCommand() throws IOException, DecoderException {
        String hash = "8f110749fd76cc35526c2ed30c95ed113fd0220a";
        String uuid = "f4b17194-cae7-11df-b40b-001517fa9844";
        client.writeTag(Hex.decodeHex(hash.toCharArray()), UUID.valueOf(uuid));
    }
    
}
