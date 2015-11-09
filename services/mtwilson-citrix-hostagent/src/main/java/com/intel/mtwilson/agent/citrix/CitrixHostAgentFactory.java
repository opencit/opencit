/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.citrix;

import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.model.InternetAddress;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.Vendor;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author stdalex
 */
public class CitrixHostAgentFactory implements VendorHostAgentFactory {
    private String citrixVendorConnectionString = "";
    
    @Override
    public String getVendorProtocol() { return "citrix"; }
    

    @Override
    public HostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
          citrixVendorConnectionString = new ConnectionString(Vendor.CITRIX, vendorConnectionString).getConnectionStringWithPrefix();
          URL url = new URL(vendorConnectionString);
          
          CitrixClient client = new CitrixClient(new TlsConnection(url, tlsPolicy));
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
          citrixVendorConnectionString = new ConnectionString(Vendor.CITRIX, vendorConnectionString).getConnectionStringWithPrefix();
          URL url = new URL(vendorConnectionString);
          
          CitrixClient client = new CitrixClient(new TlsConnection(url, tlsPolicy));
          client.init();
          return new CitrixHostAgent(client);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host connection: "+vendorConnectionString+": "+e.toString(), e);
        }
    }

    @Override
    public String getVendorConnectionString() {
        return citrixVendorConnectionString;
    }
    
}
