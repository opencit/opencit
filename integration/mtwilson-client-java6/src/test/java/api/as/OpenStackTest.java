/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.model.*;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;
/**
 *
 * @author jbuhacoff
 */
public class OpenStackTest {
    @Test
    public void pollHostsStandard() throws MalformedURLException, ClientException, IOException, ApiException, SignatureException {
        Properties config = new Properties();
        config.setProperty("mtwilson.api.baseurl", "http://10.1.71.95:8080");
        ApiClient mtwilson = new ApiClient(new MapConfiguration(config));
        ArrayList<Hostname> hosts = new ArrayList<Hostname>();
        hosts.add(new Hostname("127.0.0.1"));
        OpenStackHostTrustLevelReport report = mtwilson.pollHosts(hosts);
        System.out.println("Got "+report.pollHosts.size()+" hosts");
    }

    @Test
    public void pollHostsSingleElementArray() throws MalformedURLException, ClientException, IOException, ApiException, SignatureException, NoSuchAlgorithmException, KeyManagementException {
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://10.1.71.95:8080"), null, null, new InsecureTlsPolicy());
        ApiRequest request = new ApiRequest(MediaType.APPLICATION_JSON_TYPE, "{\"count\":0,\"hosts\":[\"127.0.0.1\"],\"pcrmask\":null}");
        ApiResponse response = client.post("http://10.1.71.95:8080/AttestationService/resources/PollHosts", request);
        System.out.println("Response: "+new String(response.content));
        /*
FINE: POST url: http://10.1.71.95:8080/AttestationService/resources/PollHosts
Dec 26, 2012 11:14:39 PM com.intel.mtwilson.ApacheHttpClient post
FINE: POST content: {"count":0,"hosts":["127.0.0.1"],"pcrmask":null}         * 
         */
    }
    
    @Test
    public void pollHostsSingleElementNonArray() throws MalformedURLException, ClientException, IOException, ApiException, SignatureException, NoSuchAlgorithmException, KeyManagementException {
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://10.1.71.95:8080"), null, null, new InsecureTlsPolicy());
        ApiRequest request = new ApiRequest(MediaType.APPLICATION_JSON_TYPE, "{\"count\":0,\"hosts\":\"127.0.0.1\",\"pcrmask\":null}");
        ApiResponse response = client.post("http://10.1.71.95:8080/AttestationService/resources/PollHosts", request);
        System.out.println("Response: "+new String(response.content));
        /*
FINE: POST url: http://10.1.71.95:8080/AttestationService/resources/PollHosts
Dec 26, 2012 11:14:39 PM com.intel.mtwilson.ApacheHttpClient post
FINE: POST content: {"count":0,"hosts":["127.0.0.1"],"pcrmask":null}         * 
         */
    }
}
