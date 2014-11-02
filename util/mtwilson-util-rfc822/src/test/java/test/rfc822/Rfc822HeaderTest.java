/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.rfc822;

import com.intel.dcsg.cpg.rfc822.Rfc822Header;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class Rfc822HeaderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Rfc822HeaderTest.class);
    
    @Test
    public void testParseKeyAlgorithmHeaderWithoutParameters() throws IOException {
        String header = "PBKDF2WithHmacSHA1";
        Map<String, String> parameters = Rfc822Header.getHeaderParameters(header);
        for(String name : parameters.keySet()) {
            log.debug("parameter {} = {}", name ,parameters.get(name));
        }
    }

    @Test
    public void testParseKeyAlgorithmHeaderWithEmptyParameters() throws IOException {
        String header = "PBKDF2WithHmacSHA1;";
        Map<String, String> parameters = Rfc822Header.getHeaderParameters(header);
        for(String name : parameters.keySet()) {
            log.debug("parameter {} = {}", name ,parameters.get(name));
        }
    }
    
    @Test
    public void testParseKeyAlgorithmHeaderWithParameters() throws IOException {
        String header = "PBKDF2WithHmacSHA1; iterations=1000; salt-bytes=8";
        Map<String, String> parameters = Rfc822Header.getHeaderParameters(header);
        for(String name : parameters.keySet()) {
            log.debug("parameter {} = {}", name ,parameters.get(name));
        }
    }

    @Test
    public void testParseContentTypeHeaderWithParameters() throws IOException {
        String header = "text/plain; charset=\"UTF-8\"";
        Map<String, String> parameters = Rfc822Header.getHeaderParameters(header);
        for(String name : parameters.keySet()) {
            log.debug("parameter {} = {}", name ,parameters.get(name));
        }
    }
    
    /**
     *  will not work because "." in the content type is a delimiter 
     * So then parameters becomes unparsed  .java; alg="AES"; key="keyId"; digest-alg="SHA-256" 
     * @throws IOException 
     */
    @Test(expected=IOException.class)
    public void testParseContentTypeDotHeaderWithParameters() throws IOException {
        String header = "application/encrypted.java; alg=\"AES\"; key=\"keyId\"; digest-alg=\"SHA-256\""; 
        Map<String, String> parameters = Rfc822Header.getHeaderParameters(header);
        for(String name : parameters.keySet()) {
            log.debug("parameter {} = {}", name ,parameters.get(name));
        }

    }
    
    // works:    token type -1 value application/encrypted-java+test   then alg=AES,  key=keyId,  digest-alg=SHA-256
    @Test
    public void testParseContentTypeHyphenPlusHeaderWithParameters() throws IOException {
        String header = "application/encrypted-java+test; alg=\"AES\"; key=\"keyId\"; digest-alg=\"SHA-256\"";  // will not work because "." in the content type is a delimiter 
        Map<String, String> parameters = Rfc822Header.getHeaderParameters(header);
        for(String name : parameters.keySet()) {
            log.debug("parameter {} = {}", name ,parameters.get(name));
        }
    }

}
