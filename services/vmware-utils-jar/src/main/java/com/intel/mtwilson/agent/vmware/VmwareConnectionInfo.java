/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.tls.TlsPolicy;

/**
 *
 * @author jbuhacoff
 */
public class VmwareConnectionInfo {
    public InternetAddress hostname; // the esxi hostname
    public String connectionString; // URL to vcenter
    public TlsPolicy tlsPolicy; // security policy for the connection
}
