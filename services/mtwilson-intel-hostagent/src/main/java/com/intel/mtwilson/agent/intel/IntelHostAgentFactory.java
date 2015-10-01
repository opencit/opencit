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
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.trustagent.client.jaxrs.TrustAgentClient;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.core.UriBuilder;
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
    public HostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            log.debug("IntelHostAgentFactory getHostAgent connection string: {}", vendorConnectionString);
            ConnectionString.IntelConnectionString intelConnectionString = ConnectionString.IntelConnectionString.forURL(vendorConnectionString);
            log.debug("IntelHostAgentFactory: Complete connection string is {}", intelConnectionString.toString());
            URL url = new URL(vendorConnectionString);
            if( url.getPort() == 1443 || url.getPath().contains("/v2") ) {
                // assume trust agent v2
                log.debug("Creating IntelHostAgent v2 for host {}", hostAddress);
                Properties properties = new Properties();
                // mtwilson version 2.0 beta has authentication support on the trust agent but not yet in the mtwilson portal
                // so we use this default username and empty password until the mtwilson portal is updated to ask for trust agent
                // login credentials
                properties.setProperty("mtwilson.api.username", "mtwilson");
                properties.setProperty("mtwilson.api.password", "");
//                properties.setProperty("mtwilson.api.ssl.policy", "INSECURE");
                
                // now add the /v2 path if it's not already there,  to maintain compatibility with the existing UI that only prompts for
                // the hostname and port and doesn't give the user the ability to specify the complete connection url
                if( url.getPath().isEmpty() || url.getPath().equals("/") ) {
                    url = UriBuilder.fromUri(url.toURI()).replacePath("/v2").build().toURL();
                    log.debug("Rewritten intel host url: {}", url.toExternalForm());
                }
                
                TrustAgentClient client = new TrustAgentClient(properties, new TlsConnection(url, tlsPolicy));
                return new IntelHostAgent2(client, hostAddress);
            }
            else /*if( url.getPort() == 9999 )*/ {
                // assume trust agent v1
                TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(url, tlsPolicy));
                log.debug("Creating IntelHostAgent v1 for host {}", hostAddress); // removed  vendorConnectionString to prevent leaking secrets  with connection string {}
                return new IntelHostAgent(client, hostAddress);
            }
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host: "+hostAddress.toString()+": "+e.toString(), e);
        }
    }

    @Override
    public HostAgent getHostAgent(String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            URL url = new URL(vendorConnectionString);
            InternetAddress hostAddress = new InternetAddress(url.getHost());
            return getHostAgent(hostAddress, vendorConnectionString, tlsPolicy);
        }
        catch(Exception e) {
            throw new IOException("Cannot get trust agent client for host connection: "+vendorConnectionString+": "+e.toString(), e);
        }
    }
}
