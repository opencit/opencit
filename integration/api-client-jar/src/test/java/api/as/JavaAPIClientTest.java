/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JavaAPIClientTest {
//    @Test
    public void testApacheHttpClientGET() throws IOException {
        String requestURL = "https://10.1.71.81:8181/AttestationService/resources/hosts/trust?hostname=1.2.3.4";
        String clientId = "new_component@server.com";
        String secretKey = "secret key 128 bytes long created in previous code snippet";
        // create the request
        HttpGet request = new HttpGet(requestURL);
        // add authorization header
//        RequestAuthorization signer = new RequestAuthorization(clientId, secretKey);
//        String authorizationHeader = signer.getAuthorizationQuietly(request.getMethod(), request.getRequestLine().getUri());
//        request.addHeader("Authorization", authorizationHeader);
        // send the request and print the response
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }

//    @Test
    public void testApacheHttpClientPUT() throws IOException {
        String requestURL = "https://10.1.71.81:8181/AttestationService/resources/hosts/trust?hostname=1.2.3.4";
        String clientId = "new_component@server.com";
        String secretKey = "secret key 128 bytes long created in previous code snippet";
        // create the request
        HttpGet request = new HttpGet(requestURL);
        // add authorization header
//        RequestAuthorization signer = new RequestAuthorization(clientId, secretKey);
//        String authorizationHeader = signer.getAuthorizationQuietly(request.getMethod(), request.getRequestLine().getUri());
//        request.addHeader("Authorization", authorizationHeader);
        // send the request and print the response
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        System.out.println(response.getStatusLine());
        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        httpClient.getConnectionManager().shutdown();
    }

}
