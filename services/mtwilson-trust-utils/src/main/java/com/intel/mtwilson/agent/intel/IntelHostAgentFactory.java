/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.intel;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyManager;
import java.io.IOException;
import java.net.URL;
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
    public String getVendorProtocol() { return "intel"; }
    
    @Override
    public IntelHostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            URL url = new URL(vendorConnectionString);
            TlsPolicyManager.getInstance().setTlsPolicy(url.getHost(), tlsPolicy);
            TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(url, TlsPolicyManager.getInstance()));
            log.debug("Creating IntelHostAgent for host {}", hostAddress); // removed  vendorConnectionString to prevent leaking secrets  with connection string {}
            return new IntelHostAgent(client, hostAddress);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host: "+hostAddress.toString()+": "+e.toString(), e);
        }
    }

    @Override
    public HostAgent getHostAgent(String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            URL url = new URL(vendorConnectionString);
            TlsPolicyManager.getInstance().setTlsPolicy(url.getHost(), tlsPolicy);
            TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(url, TlsPolicyManager.getInstance()));
//            log.debug("Creating IntelHostAgent for connection string {}", vendorConnectionString); // removed  vendorConnectionString to prevent leaking secrets  with connection string {}
            InternetAddress hostAddress = new InternetAddress(url.getHost());
            return new IntelHostAgent(client, hostAddress);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host connection: "+vendorConnectionString+": "+e.toString(), e);
        }
    }
}
