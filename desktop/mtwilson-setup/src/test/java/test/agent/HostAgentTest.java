/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.agent;

import org.junit.Test;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.as.data.*;
import java.io.IOException;
/**
 *
 * @author jbuhacoff
 */
public class HostAgentTest {
    @Test
    public void testEsxWithoutTpm() throws IOException {
        HostAgentFactory factory = new HostAgentFactory();
        TblHosts host = new TblHosts();
        host.setName("10.1.71.176");
        host.setAddOnConnectionInfo("vmware:https://10.1.71.162:443/sdk;administrator;intel123!");
        HostAgent agent = factory.getHostAgent(host);
        String hostReport = agent.getVendorHostReport();
        System.out.println(hostReport);
    }
}
