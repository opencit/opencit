/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent;

import com.intel.mountwilson.manifest.strategy.TrustAgentStrategy;
import com.intel.mountwilson.manifest.strategy.VMWareManifestStrategy;
import com.intel.mtwilson.datatypes.TxtHost;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Use this class to instantiate the appropriate agent or client for a given
 * host. It looks primarily at the "AddOn_Connection_String".
 * @throws UnuspportedOperationException if the appropriate agent type cannot be determined from the given host
 * @author jbuhacoff
 */
public class HostAgentFactory {
    
    /**
     * TODO: should return "Host" interface (from this package) so attestation
     * service does not need to know the implementation of each one
     * @param txtHost
     * @return 
     */
    public Object createHostAgent(TxtHost txtHost) {
        String connectionString = txtHost.getAddOn_Connection_String();
        Object agent = createHostAgent(connectionString);
        // TODO: set the ssl policy and trusted ssl certificate...
        return agent;
    }
    
    /**
     * TODO: should return "Host" interface (from this package) so attestation
     * service does not need to know the implementation of each one
     * @param connectionString what is also known as the "AddOn_Connection_String"
     * @return 
     */
    private Object createHostAgent(String connectionString) {
        if( connectionString == null ) {
            return new TrustAgentStrategy(null); // TODO: currently requires an entity manager factory...   also not sure if this is the right object to implement Host. will need to look into it later.
        }
        if( connectionString.startsWith("intel:") ) {
            String urlpart = connectionString.substring("intel:".length());
            return null; // TODO: create the trust agent client; rename it to "linux agent";  new LinuxAgent(urlpart);
        }
        if( connectionString.startsWith("citrix:") ) {
            String urlpart = connectionString.substring("citrix:".length());
            return null; // TODO: citrix client not complete yet;  new CitrixAgent(urlpart);
        }
        if( connectionString.startsWith("vmware:") ) {
            String urlpart = connectionString.substring("vmware:".length());
            return null; // TODO: instantiate vcenter client not complete yet;  new VCenterAgent(urlpart);
        }
        if( connectionString.startsWith("http") && connectionString.contains("/sdk;") ) {
            return new VMWareManifestStrategy(null);  // TODO: currently requires an entity manager factory...   also not sure if this is the right object to implement Host. will need to look into it later.;  new VCenterAgent(urlpart)
        }
        throw new UnsupportedOperationException("No agent registered for this host");
    }
}
