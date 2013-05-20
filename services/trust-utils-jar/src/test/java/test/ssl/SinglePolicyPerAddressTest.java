/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.ssl;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import com.intel.mtwilson.tls.*;

/**
 * This class tests a model where each destination address can have just
 * one TLS Policy associated with it.  Selecting this model means we would
 * have to edit the database schema to add a new mw_tls_policy table with
 * fields like (ID, InternetAddress, TlsPolicyName, TlsKeystore). 
 * So for example, there would only be one policy associated with a given
 * vcenter and it can be managed independently of any related host records.
 * 
 * The challenge is still that the clients just accept a URL and they open
 * their own SSL connection - so we can't even provide the right policy when
 * it's one policy per address... there is still a race condition for 
 * concurrent connections.
 *
 * @author jbuhacoff
 */
public class SinglePolicyPerAddressTest {
    
    private ArrayList<String> getTargets() {
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        ArrayList<String> targets = new ArrayList<String>();
        targets.add("https://10.1.71.162/sdk;Administrator;intel123!");
        targets.add("https://10.1.71.163/sdk;Administrator;intel123!");
        targets.add("https://10.1.71.201/;Administrator;intel123!");
        targets.add("https://10.1.71.126/;Administrator;intel123!");
        targets.add("https://10.1.71.167:9999");
        targets.add("https://10.1.71.170:9999");
        return targets;
    }
    
    /**
     * This one should work because we can create a different instance for each connection;
     * However it's not an option for production since we don't control the client code...
     */
    @Test
    public void testApacheHttpConnectionsWithTrustFirstPolicy() {
        // set up the workload: 
        ArrayList<String> targets = getTargets();
        HashMap<String,TrustFirstCertificateTlsPolicy> map = new HashMap<String,TrustFirstCertificateTlsPolicy>();
        for(String url : targets) {/*
            ByteArrayResource resource = new ByteArrayResource();
            
            TrustFirstCertificateTlsPolicy tlsPolicy = new TrustFirstCertificateTlsPolicy();
            map.put(tlsPolicy);*/
        }
        // connect one at a time with a trust first policy to collect the current certificates
        // now we have a "trust first certicate" policy and keystore for each one.
        // spawn a thread for each connection and connect again, look for errors; expect some random errors
    }

    /**
     * This one should result in errors because the ssl context is shared
     */
    @Test
    public void testJavaSslContextConnectionsWithTrustFirstPolicy() {
        // set up the workload: 
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        ArrayList<String> targets = new ArrayList<String>();
        targets.add("https://10.1.71.162/sdk;");
        // connect one at a time with a trust first policy to collect the current certificates
        // now we have a "trust first certicate" policy and keystore for each one.
        // spawn a thread for each connection and connect again, look for errors; expect some random errors
    }
    
    @Test
    public void testConnectionsWithInsecurePolicy() {
        // set up the workload: 
        // vmware: 10.1.71.175, 10.1.71.173, 10.1.71.176, 10.1.71.174
        // citrix: 10.1.71.201, 10.1.71.126
        // intel:  10.1.71.167, 10.1.71.170
        // now we have an "insecure" policy 
        // spawn a thread for each connection and connect again, look for errors; expect none
    }
    
}

