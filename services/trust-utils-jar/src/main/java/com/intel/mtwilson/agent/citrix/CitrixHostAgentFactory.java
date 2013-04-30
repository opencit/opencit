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
import java.io.IOException;

/**
 *
 * @author stdalex
 */
public class CitrixHostAgentFactory implements VendorHostAgentFactory {

    @Override
    public HostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
          CitrixClient client = new CitrixClient(new TlsConnection(vendorConnectionString, tlsPolicy));
          return new CitrixHostAgent(client);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host: "+hostAddress.toString()+": "+e.toString());
        }
    }
    
}
