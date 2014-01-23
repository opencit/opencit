/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.intel;

import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.agent.intel.TrustAgentSecureClient;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.TlsConnection;
import java.io.IOException;
import java.net.URL;
import org.codehaus.jackson.map.ObjectMapper;
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
        String hash = "2e2b83a5af1ce1e11a751fcca409b483c2b93e28";
        String uuid = "F4B17194-CAE7-11DF-B40B-001517FA9844".toLowerCase();
        boolean response = client.setAssetTag(hash, uuid);
        if(response)
            System.out.println("asset tag set!");
        else
            System.out.println("asset tag  not set!");
    }
    
}
