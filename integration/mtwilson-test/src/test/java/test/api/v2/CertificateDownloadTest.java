/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.io.IOException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

/**
 *
 * @author jbuhacoff
 */
public class CertificateDownloadTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateDownloadTest.class);
    
    @Test
    public void testBaseURL() throws IOException {
        log.debug("MtWilson URL {} configured from ", At.baseurl(), My.configuration().getSource("mtwilson.api.url"));
    }
    
    @Test
    public void testDownloadPem() throws IOException {
//      String pem = At.userCertificates(UUID.valueOf("7f2a647d-8172-44a6-b15a-30eaa42580e7")).get(MediaType.TEXT_PLAIN).getText(); // one liner doesn't work, server returns 406 Unacceptable
        ClientResource resource = At.userCertificates(UUID.valueOf("7f2a647d-8172-44a6-b15a-30eaa42580e7"));
//        resource.accept(MediaType.ALL); // when we say accept */* we happen to get the YAML -   but it's really up to the server so it might change 
        resource.accept(MediaType.TEXT_PLAIN); // let's get more specific and say text/plain
        String pem = resource.get().getText();
        log.debug("PEM: {}", pem);
    }
}
