/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.datatypes.Vendor;
import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VmwareHostAgentFactory creates instances of VmwareHostAgent. It does
 * not create instances of VmwareClient. It uses 
 * @author jbuhacoff
 */
public class VmwareHostAgentFactory implements VendorHostAgentFactory {
    private Logger log = LoggerFactory.getLogger(getClass());
    private String vmwareVendorConnectionString = "";
    protected static VMwareConnectionPool pool = new VMwareConnectionPool(new VmwareClientFactory()); 
    
    @Override
    public String getVendorProtocol() { return "vmware"; }
    
    
    @Override
    public VmwareHostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            vmwareVendorConnectionString = new ConnectionString(Vendor.VMWARE, vendorConnectionString).getConnectionStringWithPrefix();
            // If the connection string does not include the host address, add it here so that if there is an exception in the client layer the hostname will appear when printing the connection string
            ConnectionString.VmwareConnectionString connStr = ConnectionString.VmwareConnectionString.forURL(vendorConnectionString);
            if( connStr.getHost() == null ) {
                connStr.setHost(hostAddress);
                vendorConnectionString = connStr.toString();
            }
            // Original call 
          URL url = new URL(vendorConnectionString);
          
            VMwareClient client = pool.getClientForConnection(new TlsConnection(url, tlsPolicy));
//            VMwareClient client = pool.createClientForConnection(new TlsConnection(vendorConnectionString, tlsPolicy));
            return new VmwareHostAgent(client, hostAddress.toString());
        }
        catch(Exception e) {
            throw new IOException("Cannot get vmware client for host: "+hostAddress.toString()+": "+e.toString(), e);
        }
    }

    @Override
    public VmwareHostAgent getHostAgent(String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        ConnectionString.VmwareConnectionString vmware = ConnectionString.VmwareConnectionString.forURL(vendorConnectionString);
        try {
            vmwareVendorConnectionString = vendorConnectionString;
          URL url = new URL(vendorConnectionString);
//            log.debug("getHostAgent {}", vendorConnectionString);
            VMwareClient client = pool.getClientForConnection(new TlsConnection(url, tlsPolicy));
//            VMwareClient client = pool.createClientForConnection(new TlsConnection(vendorConnectionString, tlsPolicy));
            log.debug("vmware host = {}", vmware.getHost().toString());
            log.debug("vmware port = {}", vmware.getPort());
//            log.debug("vmware username = {}", vmware.getUsername());
//            log.debug("vmware password = {}", vmware.getPassword());
            log.debug("vmware vcenter = {}", vmware.getVCenter().toString());
//            log.debug("vmware toURL = {}", vmware.toURL());
            return new VmwareHostAgent(client, vmware.getHost().toString());
        }
        catch(Exception e) {
            throw new IOException("Cannot get vmware client for host: "+vmware.getHost().toString()+" at vcenter: "+vmware.getVCenter().toString()+" with username: "+vmware.getUsername()+": "+e.toString(), e);
        }
    }

    @Override
    public String getVendorConnectionString() {
        return vmwareVendorConnectionString;
    }
}
