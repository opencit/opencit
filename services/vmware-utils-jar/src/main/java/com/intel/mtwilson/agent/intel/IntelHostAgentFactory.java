/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.intel;

import com.intel.mountwilson.as.helper.TrustAgentSecureClient;
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
    public IntelHostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(vendorConnectionString, tlsPolicy));
            log.debug("Creating IntelHostAgent for host {} with connection string {}", hostAddress, vendorConnectionString);
            return new IntelHostAgent(client, hostAddress);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host: "+hostAddress.toString()+": "+e.toString());
        }
    }
}
