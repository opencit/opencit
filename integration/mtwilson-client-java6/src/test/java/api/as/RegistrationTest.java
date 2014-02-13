/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class RegistrationTest {
    protected static final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void testEncodePemWithJson() throws IOException {
        String pem = "-----BEGIN CERTIFICATE REQUEST-----\n"+
"MIIBnTCCAQYCAQAwXTELMAkGA1UEBhMCU0cxETAPBgNVBAoTCE0yQ3J5cHRvMRIw\n"+
"EAYDVQQDEwlsb2NhbGhvc3QxJzAlBgkqhkiG9w0BCQEWGGFkbWluQHNlcnZlci5l\n"+
"eGFtcGxlLmRvbTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAr1nYY1Qrll1r\n"+
"uB/FqlCRrr5nvupdIN+3wF7q915tvEQoc74bnu6b8IbbGRMhzdzmvQ4SzFfVEAuM\n"+
"MuTHeybPq5th7YDrTNizKKxOBnqE2KYuX9X22A1Kh49soJJFg6kPb9MUgiZBiMlv\n"+
"tb7K3CHfgw5WagWnLl8Lb+ccvKZZl+8CAwEAAaAAMA0GCSqGSIb3DQEBBAUAA4GB\n"+
"AHpoRp5YS55CZpy+wdigQEwjL/wSluvo+WjtpvP0YoBMJu4VMKeZi405R7o8oEwi\n"+
"PdlrrliKNknFmHKIaCKTLRcU59ScA6ADEIWUzqmUzP5Cs6jrSRo3NKfg1bd09D1K\n"+
"9rsQkRc9Urv9mRBIsredGnYECNeRaK5R1yzpOowninXC\n"+
"-----END CERTIFICATE REQUEST-----";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, pem);
        System.out.println(out.toString());
        System.out.println(pem);
    }
}
