/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.My;
import java.io.IOException;
import org.junit.Test;
import org.restlet.resource.ClientResource;
import com.intel.mtwilson.v2.vm.attestation.model.ManifestSignature;
import com.intel.mtwilson.v2.vm.attestation.model.ManifestSignatureInput;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import org.restlet.data.MediaType;
/**
 *
 * @author jbuhacoff
 */
public class RpcTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RpcTest.class);
    
    @Test
    public void testBaseURL() throws IOException {
        log.debug("MtWilson URL {} configured from ", At.baseurl(), My.configuration().getSource("mtwilson.api.url"));
    }
    
    public static class AdderInput { public Integer x, y; }
    
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class AdderResult { public Integer result; }
    
    @Test
    public void testAddIntegersAsync() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        
        AdderInput input = new AdderInput();
        input.x = 1;
        input.y = 2;
        String inputJson = mapper.writeValueAsString(input);
        log.debug("Input: {}", inputJson);
//      String pem = At.userCertificates(UUID.valueOf("7f2a647d-8172-44a6-b15a-30eaa42580e7")).get(MediaType.TEXT_PLAIN).getText(); // one liner doesn't work, server returns 406 Unacceptable
        ClientResource resource = At.testAddIntegersAsync();
        Representation request = new StringRepresentation(inputJson, MediaType.APPLICATION_JSON);
//        resource.accept(MediaType.ALL); // when we say accept */* we happen to get the YAML -   but it's really up to the server so it might change 
        resource.accept(MediaType.APPLICATION_JSON);
        String statusJson = resource.post(request).getText();
        log.debug("Output raw: {}", statusJson);
        
        // the response from an async rpc call is the RpcStatus object
        Rpc status = mapper.readValue(statusJson, Rpc.class);
        log.debug("Status of rpc {} is {}  -- request id is {}", status.getName(), status.getStatus(), status.getId());
        
        // now make the call to get the status from the server
        String statusJson2 = At.testRpcStatus(status.getId()).get(MediaType.APPLICATION_JSON).getText();
        log.debug("Current status: {}", statusJson2);
        
        String outputJson = At.testRpcOutput(status.getId()).get(MediaType.APPLICATION_JSON).getText();
        AdderResult output = mapper.readValue(outputJson, AdderResult.class);
        log.debug("Output: {}", output.result);
        
    }

    @Test
    public void testAddIntegers() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        
        AdderInput input = new AdderInput();
        input.x = 1;
        input.y = 2;
        String inputJson = mapper.writeValueAsString(input);
        log.debug("Input: {}", inputJson);
//      String pem = At.userCertificates(UUID.valueOf("7f2a647d-8172-44a6-b15a-30eaa42580e7")).get(MediaType.TEXT_PLAIN).getText(); // one liner doesn't work, server returns 406 Unacceptable
        ClientResource resource = At.testAddIntegers();
        Representation request = new StringRepresentation(inputJson, MediaType.APPLICATION_JSON);
//        resource.accept(MediaType.ALL); // when we say accept */* we happen to get the YAML -   but it's really up to the server so it might change 
        resource.accept(MediaType.APPLICATION_JSON);
        String outputJson = resource.post(request).getText();
        log.debug("Output raw: {}", outputJson);
        
        // the response from a blocking rpc call is the rpc output
        AdderResult output = mapper.readValue(outputJson, AdderResult.class);
        log.debug("Output: {}", output.result);
        
    }

}
