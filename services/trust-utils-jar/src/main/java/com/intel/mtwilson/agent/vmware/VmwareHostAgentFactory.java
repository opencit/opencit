/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.mtwilson.tls.TlsConnection;
import com.intel.mtwilson.tls.TlsPolicy;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VmwareHostAgentFactory creates instances of VmwareHostAgent. It does
 * not create instances of VmwareClient. It uses 
 * @author jbuhacoff
 */
public class VmwareHostAgentFactory implements VendorHostAgentFactory {
    private Logger log = LoggerFactory.getLogger(getClass());
    protected static VMwareConnectionPool pool = new VMwareConnectionPool(new VmwareClientFactory()); 
    
    @Override
    public VmwareHostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            VMwareClient client = pool.getClientForConnection(new TlsConnection(vendorConnectionString, tlsPolicy)); //pool.getClientForConnection(key(vendorConnectionString, tlsPolicy));
            return new VmwareHostAgent(client, hostAddress.toString());
        }
        catch(Exception e) {
            throw new IOException("Cannot get vmware client for host: "+hostAddress.toString()+": "+e.toString());
        }
    }
}
