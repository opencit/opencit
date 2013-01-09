/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.intel;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.tls.TlsConnection;
import com.intel.mtwilson.tls.TlsPolicy;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IntelHostAgentFactory creates instances of IntelHostAgent. It does
 * not create instances of IntelClient. It uses the IntelClientFactory to do that.
 * @author jbuhacoff
 */
public class IntelHostAgentFactory implements VendorHostAgentFactory {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public HostAgent getHostAgent(String vendorConnectionString, TlsPolicy tlsPolicy, InternetAddress hostAddress) throws IOException {
        try {
//            VMwareClient client = pool.getClientForConnection(new TlsConnection(vendorConnectionString, tlsPolicy)); //pool.getClientForConnection(key(vendorConnectionString, tlsPolicy));
//            return new IntelHostAgent(client, hostAddress.toString());
            return null;
        }
        catch(Exception e) {
            throw new IOException("Cannot get vmware client for host: "+hostAddress.toString()+": "+e.toString());
        }
    }
}
