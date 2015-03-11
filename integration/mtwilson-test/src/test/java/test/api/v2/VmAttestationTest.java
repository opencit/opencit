/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.My;
import java.io.IOException;
import org.junit.Test;
import org.restlet.resource.ClientResource;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 *
 * @author jbuhacoff
 */
public class VmAttestationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VmAttestationTest.class);
    
    @Test
    public void testBaseURL() throws IOException {
        log.debug("MtWilson URL {} configured from ", At.baseurl(), My.configuration().getSource("mtwilson.api.url"));
    }
    
    @Test
    public void testSignManifest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        
//        ManifestSignatureInput input = new ManifestSignatureInput();
//        input.setVmImageId("image123");;
//        input.setManifestHash("aaaaaaaaaaaaaaaa");
//        
//        String json = mapper.writeValueAsString(input);
//        
////      String pem = At.userCertificates(UUID.valueOf("7f2a647d-8172-44a6-b15a-30eaa42580e7")).get(MediaType.TEXT_PLAIN).getText(); // one liner doesn't work, server returns 406 Unacceptable
//        ClientResource resource = At.manifestSignature();
//        Representation request = new StringRepresentation(json, MediaType.APPLICATION_JSON);
////        resource.accept(MediaType.ALL); // when we say accept */* we happen to get the YAML -   but it's really up to the server so it might change 
//        resource.accept(MediaType.APPLICATION_JSON);
//        String output = resource.post(request).getText();
//        log.debug("Output raw: {}", output);
//        
//        
//        // do it again, this time get a pojo  instead of raw output:
//        ManifestSignature result = mapper.readValue(output, ManifestSignature.class); 
//        log.debug("Output POJO: {}", result.getSignature());
        
    }
}
