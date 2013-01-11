/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.tls.TlsPolicy;
import java.io.IOException;

/**
 *
 * @author jbuhacoff
 */
public interface VendorHostAgentFactory {
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
}
