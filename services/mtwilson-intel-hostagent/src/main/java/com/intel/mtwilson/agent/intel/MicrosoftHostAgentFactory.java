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
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.MwHostPreRegistrationDetails;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.datatypes.Vendor;
import com.intel.mtwilson.trustagent.client.jaxrs.TrustAgentClient;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MicrosoftHostAgentFactory creates instances of IntelHostAgent. It does
 * not create instances of IntelClient. It uses the IntelClientFactory to do that.
 * @author hxia5
 */
public class MicrosoftHostAgentFactory implements VendorHostAgentFactory {
    private Logger log = LoggerFactory.getLogger(getClass());
    private String microsoftVendorConnectionString = "";
    
    @Override
    public String getVendorProtocol() { return "microsoft"; }
    
    @Override
    public HostAgent getHostAgent(InternetAddress hostAddress, String vendorConnectionString, TlsPolicy tlsPolicy) throws IOException {
        try {
            microsoftVendorConnectionString = vendorConnectionString;  //the vendorConnectionString parameter only contains the URL portion
            String tempMicrosoftVendorConnectionString = new ConnectionString(Vendor.MICROSOFT, vendorConnectionString).getConnectionStringWithPrefix();
            log.debug("MicrosoftHostAgentFactory: vendorConnectionString is {}", microsoftVendorConnectionString);
            ConnectionString.MicrosoftConnectionString microsoftConnectionString = ConnectionString.MicrosoftConnectionString.forURL(tempMicrosoftVendorConnectionString);
            log.debug("MicrosoftHostAgentFactory: Connection string URL is {}", microsoftConnectionString.toURL());
            // We need to verify if the user has specified the login id and password for the host. If not, we will check in the pre-register host table.
            // If it is not even present in that table, we will throw an error.
            if (microsoftConnectionString.getUsername() == null || microsoftConnectionString.getUsername().isEmpty() ||
                    microsoftConnectionString.getPassword() == null || microsoftConnectionString.getPassword().isEmpty()) {
                log.debug("MicrosoftHostAgentFactory - User name or password not specified. Retrieving from table");
                MwHostPreRegistrationDetails hostLoginDetails = My.jpa().mwHostPreRegistrationDetails().findByName(microsoftConnectionString.getHost().toString());
                if (hostLoginDetails != null) {
                    ConnectionString tempConnectionString = ConnectionString.forMicrosoft(microsoftConnectionString.getHost().toString(), microsoftConnectionString.getPort(), 
                            hostLoginDetails.getLogin(), hostLoginDetails.getPassword());
                    // Would be used to return back the modified connection string.
                    microsoftVendorConnectionString = tempConnectionString.getConnectionString();
                    log.debug("MicrosoftHostAgentFactory - URL of new connection string is {}", tempConnectionString.getURL());
                    microsoftConnectionString = ConnectionString.MicrosoftConnectionString.forURL(tempConnectionString.getConnectionStringWithPrefix());
                }
            }
            microsoftVendorConnectionString = new ConnectionString(Vendor.MICROSOFT, microsoftVendorConnectionString).getConnectionStringWithPrefix();
            URL url = microsoftConnectionString.toURL();
            if( url.getPort() == 1443 || url.getPath().contains("/v2") ) {
                // assume trust agent v2
                log.debug("Creating IntelHostAgent v2 for host {} with URL {}", hostAddress, url);
                Properties properties = new Properties();
                // mtwilson version 2.0 beta has authentication support on the trust agent but not yet in the mtwilson portal
                // so we use this default username and empty password until the mtwilson portal is updated to ask for trust agent
                // login credentials
                if( microsoftConnectionString.getUsername() != null ) {
                properties.setProperty("mtwilson.api.username", microsoftConnectionString.getUsername());
                }
                if( microsoftConnectionString.getPassword() != null ) {
                properties.setProperty("mtwilson.api.password", microsoftConnectionString.getPassword());
                }
//                properties.setProperty("mtwilson.api.username", "mtwilson");
//                properties.setProperty("mtwilson.api.password", "");
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

    @Override
    public String getVendorConnectionString() {
        return microsoftVendorConnectionString;
    }
}
