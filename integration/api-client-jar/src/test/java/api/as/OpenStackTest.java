/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.ApiRequest;
import com.intel.mtwilson.ApiResponse;
import com.intel.mtwilson.ClientException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import java.util.ArrayList;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.ws.rs.core.MediaType;
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
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://10.1.71.95:8080"), null, null, null);
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
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://10.1.71.95:8080"), null, null, null);
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
