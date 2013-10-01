/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.tls;

import org.junit.Test;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.tls.*;
import com.intel.mtwilson.as.data.*;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author jbuhacoff
 */
public class UpdateTlsKeystoreInDatabaseTest {
    private transient Logger log = LoggerFactory.getLogger(getClass());
        
    @Test
    public void testAddEsxHostAndSaveTlsKeystore() throws IOException {
        TblHosts host = new TblHosts();
        host.setName("10.1.71.176");
        host.setAddOnConnectionInfo("vmware:https://10.1.71.162:443/sdk;administrator;intel123!");
        log.debug("before connecting, tls policy = {} and keystore length = {}", host.getTlsPolicyName(), host.getTlsKeystore() == null ? "null" : host.getTlsKeystore().length);
        HostAgentFactory factory = new HostAgentFactory();
        HostAgent agent = factory.getHostAgent(host);
        String vendorHostReport = agent.getVendorHostReport();
        System.out.println(vendorHostReport);
        log.debug("after connecting, tls policy = {} and keystore length = {}", host.getTlsPolicyName(), host.getTlsKeystore() == null ? "null" : host.getTlsKeystore().length);
    }
}
