/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ApiResponse;
import com.intel.mtwilson.datatypes.HostLocation;
import com.intel.mtwilson.datatypes.TagDataType;
import java.io.IOException;
import java.security.SignatureException;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JavaAPIClientTest {
    protected static final ObjectMapper mapper = new ObjectMapper();

    // copy of ApiClient.toJSON
    private String toJSON(Object value) throws IOException {
        return mapper.writeValueAsString(value);        
    }

    // copy of ApiClient.fromJSON
    private <T> T fromJSON(String document, Class<T> valueType) throws IOException, ApiException {
        try {
            return mapper.readValue(document, valueType);
        }
        catch(com.fasterxml.jackson.core.JsonParseException e) {
           
            throw new ApiException("Cannot parse response", e);
        }
    }
    
    @Test
    public void testApacheHttpClientGET() throws IOException, ApiException {
        String requestURL = "http://10.1.71.207:9999/tags?oidEqualTo=1.3.6.1.4.1.99999.3";
        HttpGet request = new HttpGet(requestURL);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);      
        String str = IOUtils.toString(response.getEntity().getContent());
        System.out.println("str " + str);        
        TagDataType[] tag = fromJSON(str, TagDataType[].class);
        System.out.println("name equals " + tag[0].name);
        httpClient.getConnectionManager().shutdown();
    }



//    @Test
    public void testApacheHttpClientPUT() throws IOException {
        String requestURL = "https://10.1.71.81:8181/AttestationService/resources/hosts/trust?hostname=1.2.3.4";
        //String clientId = "new_component@server.com";
       // String secretKey = "secret key 128 bytes long created in previous code snippet";
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
