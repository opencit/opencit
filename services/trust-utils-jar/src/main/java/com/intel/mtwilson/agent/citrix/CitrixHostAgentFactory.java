/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.citrix;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.mtwilson.tls.TlsConnection;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TlsPolicyManager;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author stdalex
 */
public class CitrixHostAgentFactory implements VendorHostAgentFactory {

    @Override
    public HostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
          URL url = new URL(vendorConnectionString);
          TlsPolicyManager.getInstance().setTlsPolicy(url.getHost(), tlsPolicy);
          CitrixClient client = new CitrixClient(new TlsConnection(url, TlsPolicyManager.getInstance()));
          client.init();
          return new CitrixHostAgent(client);
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
          CitrixClient client = new CitrixClient(new TlsConnection(url, TlsPolicyManager.getInstance()));
          client.init();
          return new CitrixHostAgent(client);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host connection: "+vendorConnectionString+": "+e.toString(), e);
        }
    }
    
}
