/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import java.io.File;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ClientRegistrationTest extends RemoteIntegrationTest {
    
    /**
     * NOTE: the configuration example below may be outdated with respect
     * to TLS Policy configuration; please refer to current product documentation
     * 
     * Register using V1 APIs. Before running this junit test, create the 
     * file C:\mtwilson\configuration\mtwilson.properties with content like
     * this:
     * 
mtwilson.api.username=jonathan
mtwilson.api.password=password
mtwilson.api.url=http\://10.1.71.134\:8080/mtwilson/v1
mtwilson.api.baseurl=http\://10.1.71.134\:8080/mtwilson/v1
mtwilson.default.tls.policy.id=TRUST_FIRST_CERTIFICATE
mtwilson.api.keystore=c\:/mtwilson/configuration/jonathan.jks
mtwilson.api.keystore.password=beXyfVzb5D8oSHucNErVyw\=\=
mtwilson.api.key.alias=CN\=jonathan
mtwilson.api.key.password=beXyfVzb5D8oSHucNErVyw\=\=
     * 
     * The resulting client keystore can then be used with the junit tests
     * in the ApiTest class.
     * 
     * Note that the mtwilson.api.keystore is the complete file path with .jks extension,
     * and that the property used for the password is mtwilson.api.keystore.password.
     * 
     * That's important because if you use mtwilson.api.username and mtwilson.api.password
     * only in the test.properties file, the client will use HTTP BASIC authentication.
     * But if you put BOTH then it will use X509 because it has higher priority.
     * 
     * So using code below, you would set mtwilson.api.keystore to something like (configuration path)/(value of mtwilson.api.username).jks
     * 
     * @throws Exception 
     */
    @Test
    public void registerWithConfiguration() throws Exception {
        File directory = My.configuration().getDirectory(); //new File(My.filesystem().getConfigurationPath());
        String username = testProperties.getProperty("mtwilson.api.username");
        String password = testProperties.getProperty("mtwilson.api.keystore.password");
        URL server = new URL(testProperties.getProperty("mtwilson.api.url")); // My.configuration().getMtWilsonURL();
        String[] roles = new String[] { "Attestation", "Whitelist" };
        KeystoreUtil.createUserInDirectory(directory, username, password, server, roles);
    }
    
    /**
     * Note the path you need in mtwilson.api.url ends in /mtwilson/v1 when running this test:
     * <pre>
     * mtwilson.api.url=https\://10.1.71.56\:8443/mtwilson/v1
     * </pre>
     * 
     * Example request:
     * <pre>
2014-06-26 01:27:49,680 DEBUG [main] o.a.h.i.c.DefaultClientConnection [DefaultClientConnection.java:268] Sending request: POST /mtwilson/v1/ManagementService/resources/apiclient/register HTTP/1.1
2014-06-26 01:27:49,681 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "POST /mtwilson/v1/ManagementService/resources/apiclient/register HTTP/1.1[\r][\n]"
2014-06-26 01:27:49,682 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Accept-Language: en-US;q=1, en;q=0.9[\r][\n]"
2014-06-26 01:27:49,683 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Date: Thu, 26 Jun 2014 01:27:42 PDT[\r][\n]"
2014-06-26 01:27:49,683 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Authorization: X509 fingerprint="AMK+09fsEr66j/EoT1IN6k9ppCMtVpzb+lwE3PNn7bA=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="gVEvcPK+3eDHNMZrAvq8veBS3WdhABPG3BROj5aF/PoeajbdJL0HPQcIEZJC+bXVTnw5WNBzrbZTqOxgwb1DUjD4nFwbelx7W2GpAmMY0EaI025TLIH8ANxGPT7ACEy++3B7txyi8LD2PfVs3UPpUTz0mCMY3J40ICgATBIrQH3ZN/Z7dayrM+mecjUqmNMh6m8w+Jt0omsCb2/m3GfnfwaPQ5V3CDJPRViLkYpvPVvJHNbypnEdnYBwhcPuom5tPl4GJBCMYsIrtXayR0TzSDaNHw8yNI6b5vtAW0ODZA/xwmETMW2XAg/p4BKgtgL0dFbFSEEFHux5AxxxXU2LjA=="[\r][\n]"
2014-06-26 01:27:49,683 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "X-Nonce: AAABRtdJlH8A83F/Un6A+kUymT35ZZ18[\r][\n]"
2014-06-26 01:27:49,683 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Content-Length: 1134[\r][\n]"
2014-06-26 01:27:49,683 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Content-Type: application/json; charset=UTF-8[\r][\n]"
2014-06-26 01:27:49,684 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Host: 10.1.71.56:8443[\r][\n]"
2014-06-26 01:27:49,684 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Connection: Keep-Alive[\r][\n]"
2014-06-26 01:27:49,684 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "[\r][\n]"
2014-06-26 01:27:49,687 DEBUG [main] o.a.h.wire [Wire.java:86]  >> "{"X509Certificate":"MIIDIjCCAgqgAwIBAgIIBDGoBkc2YjowDQYJKoZIhvcNAQELBQAwUTELMAkGA1UEBhMCVVMxHDAaBgNVBAoTE1RydXN0ZWQgRGF0YSBDZW50ZXIxEjAQBgNVBAsTCU10IFdpbHNvbjEQMA4GA1UEAxMHYWRtaW4yNTAeFw0xNDA2MjYwNjI4NDJaFw0yNDA2MjMwNjI4NDJaMFExCzAJBgNVBAYTAlVTMRwwGgYDVQQKExNUcnVzdGVkIERhdGEgQ2VudGVyMRIwEAYDVQQLEwlNdCBXaWxzb24xEDAOBgNVBAMTB2FkbWluMjUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCKdC0pMoLYu4d7TSUyA9j8KReV8Ryud34MTSBM8TE4Y6CwTyXP5r09tF5xEjQchZcB9gR44A/dTgqmF2aER5aYLwHLp9lq0T2ez/OXFUYqvM2SzAa2v6U7q/S2VN57RxOo8DZ7MqJrYa45zHToYxZa/JxYxOrVV43DDoljkWsULzq6Jh1wKrvwuXENV+YO6P0RlFnhdiWL7ywcnx0Y9lagRrmspHHeZeiF1tVgMxS5WIt3lQW79vwa7RzTNeJ3r8E/ZreYmMbGKcVwZvdIlEieRp7M2srPQt8FPniIZWc2c8pdDbyg31ZMheyrahE4If0gP/r1Wltjoru6B0uQP3flAgMBAAEwDQYJKoZIhvcNAQELBQADggEBADX7itFgUVTvm14tQwNWjbILetXthZxl4C2YIPweOZcTaBPKg2c6VZJBfWOOmyv8ix6ADLhHSW8wYyBGuxLeimrmZMDXjqRQ+Rn5eCGnwIli2Mj7pgOYzC7UV9PXAKmm5118XmlBDgRGT7GtF4F2UgGoIl9aqOy2CkzEQJQ47dFTCiECti7wPmrLJyeLDOHVitQmKTM7k8MsuymQA8TrWpuOYykQ8utJAITU0qX7R6cLh/bCSCfs44K5KoDyOgKnnNYrP+y0+rqa7xNlWUz5zuHhl0R721biIiOfisqoxF40hKdqgb30lLhoNhUr/dAhOGYYEazfm+Cvr+2/RjHqBiU=","Roles":["Attestation","Whitelist"]}"
     * </pre>
     * 
     * Example response:
     * <pre>
2014-06-26 01:27:49,924 DEBUG [main] o.a.h.wire [Wire.java:72]  << "HTTP/1.1 200 OK[\r][\n]"
2014-06-26 01:27:49,933 DEBUG [main] o.a.h.wire [Wire.java:72]  << "Server: Apache-Coyote/1.1[\r][\n]"
2014-06-26 01:27:49,934 DEBUG [main] o.a.h.wire [Wire.java:72]  << "Content-Type: text/plain[\r][\n]"
2014-06-26 01:27:49,934 DEBUG [main] o.a.h.wire [Wire.java:72]  << "Content-Length: 2[\r][\n]"
2014-06-26 01:27:49,935 DEBUG [main] o.a.h.wire [Wire.java:72]  << "Date: Thu, 26 Jun 2014 08:30:20 GMT[\r][\n]"
2014-06-26 01:27:49,936 DEBUG [main] o.a.h.wire [Wire.java:72]  << "[\r][\n]"
     * </pre>
     * 
     * @throws Exception 
     */
    @Test
    public void registerWithExistingKey() throws Exception {
        // configuration
        File directory = My.configuration().getDirectory(); //new File(My.filesystem().getConfigurationPath());
        String username = My.configuration().getClientProperties().getProperty("mtwilson.api.key.alias");
        String password = My.configuration().getClientProperties().getProperty("mtwilson.api.keystore.password");
        URL server = My.configuration().getMtWilsonURL();
        // certificate to register
        KeyStore keystore = KeystoreUtil.fromFilename(directory.getAbsolutePath()+File.separator+username+".jks" , password);
        Certificate certificate = keystore.getCertificate(username);
        // client (using same certificate or different certificate)
        ApiClient client = KeystoreUtil.clientForUserInDirectory(directory, username, password, server);
        ApiClientCreateRequest user = new ApiClientCreateRequest();
        user.setCertificate(certificate.getEncoded()); //CertificateEncodingException
        user.setRoles(new String[] {"Attestation","Whitelist"}); // roles to request - administrator can approve these or different roles
        client.register(user); //IOException
        
    }
}
