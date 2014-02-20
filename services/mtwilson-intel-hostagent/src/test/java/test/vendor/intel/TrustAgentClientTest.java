/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.intel;

import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.agent.intel.TrustAgentSecureClient;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.io.IOException;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TrustAgentClientTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testHostInfoCommand() throws IOException {
        TlsConnection tlsConnection = new TlsConnection(new URL("https://10.1.71.45:9999"), new InsecureTlsPolicy());
        TrustAgentSecureClient client = new TrustAgentSecureClient(tlsConnection);
        HostInfo hostInfo = client.getHostInfo();
        ObjectMapper mapper = new ObjectMapper();
        log.debug(mapper.writeValueAsString(hostInfo));
    }
    
    @Test
    public void testSetAssetTagCommand() throws IOException {
        TlsConnection tlsConnection = new TlsConnection(new URL("https://10.1.71.45:9999" ), new InsecureTlsPolicy());
        TrustAgentSecureClient client = new TrustAgentSecureClient(tlsConnection);
        String hash = "da9b727950aae47fb6fac654519caa4b14bca5d0";
        String uuid = "F4B17194-CAE7-11DF-B40B-001517FA9844";
        boolean response = client.setAssetTag(hash, uuid);
        if(response)
            System.out.println("asset tag set!");
        else
            System.out.println("asset tag  not set!");
    }
    
}
