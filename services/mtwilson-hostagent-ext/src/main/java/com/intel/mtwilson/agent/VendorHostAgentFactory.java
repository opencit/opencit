/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mtwilson.model.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public interface VendorHostAgentFactory {
    String getVendorProtocol(); // returns the protocol for connection string like "intel", "citrix", "vmware"  , for example  "intel:https://192.168.1.100:9999"
    
    // returns the connection string if modified. In case of IntelConnection strings, instead of specifying in the connection string, users might pre-register the host
    // Need to return back this modified connection string.
    String getVendorConnectionString(); 
    
    /**
     * On success, a HostAgent object should be returned for the specified hostAddress
     * which is able to obtain information about the host.
     * 
     * If the vendor-specific factory uses a connection pool to maintain connections
     * to its servers, the connection pool should be keyed by the server AND by the
     * TLS Policy provided by the caller. A connection should only be reused if one
     * exists for that server with the specified TLS Policy.  If Creates or reuses a vmware client connection to the vcenter from the
     * connection pool. In order to reuse a connection both the connection string
     * and the tlspolicy must be the same. If the connection string matches a
     * connection in the pool but the tlspolicy is different, a new connection
     * will be created with the given tlspolicy.
     * 
     * @param hostAddress the IP Address or Hostname of the Intel TXT-enabled host for which the caller wants a HostAgent instance
     * @param vendorConnectionString a vendor-specific URL or other string that specifies how to connect to the host
     * @param tlsPolicy the TLS Policy for the connection, specifying what are trusted certificates and whether or which self-signed certificates are accepted, etc.
     * @return 
     */
    HostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException;

    /**
     * Revised interface where the host address is embedded in the vendor connection string and it's up 
     * to the vendor factory class to extract it. The vendorConnectionString is everything after the 
     * vendor prefix.  
     * 
     * For example,  for the connection string vmware:https://vcenter:443/sdk;administrator;password;hostname the 
     * vendorConnectionString is https://vcenter:443/sdk;administrator;password;hostname 
     * 
     * @param vendorConnectionString
     * @param tlsPolicy
     * @return
     * @throws IOException 
     */
    HostAgent getHostAgent(String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException;

}
