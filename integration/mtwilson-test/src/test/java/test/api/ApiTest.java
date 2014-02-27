/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api;

import com.intel.dcsg.cpg.io.ConfigurationUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.KeystoreUtil;
import com.intel.mtwilson.My;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.model.Hostname;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.SignatureException;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Use registerWithConfiguration() in ClientRegistrationTest to create
 * your V1 client before running the junit tests in this class
 * @author rksavinx
 */
public class ApiTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiTest.class);
    private static ApiClient client;
    
    @BeforeClass
    public static void init() throws Exception {
        File directory = new File(My.filesystem().getConfigurationPath());
        String username = My.configuration().getClientProperties().getProperty("mtwilson.api.username");
        String password = My.configuration().getClientProperties().getProperty("mtwilson.api.password");
        URL server = My.configuration().getMtWilsonURL();
        client = KeystoreUtil.clientForUserInDirectory(directory, username, password, server);        
    }
    
    @Test
    public void testSaml() throws Exception {
        String saml = client.getSamlForHost(new Hostname("10.1.71.91"));
        TrustAssertion trust = client.verifyTrustAssertion(saml);
        assertNotNull(trust);
        log.debug("is trusted? {}", trust.isValid());
        if( !trust.isValid()) {
            log.debug("not trusted", trust.error());
            return;
        }
        log.debug("SAML Issuer: {}", trust.getIssuer());
        log.debug("SAML Issued On: {}", trust.getDate().toString());
        log.debug("SAML Subject: {}", trust.getSubject());
        for(String attr : trust.getAttributeNames()) {
            log.debug("Host {}: {}", attr, trust.getStringAttribute(attr));
        }
        assertNull(trust.getAikCertificate());
        log.debug("AIK Certificate: {}", trust.getAikCertificate() == null ? "null" : Base64.encodeBase64String(trust.getAikCertificate().getEncoded()));
    }
    
    @Test
    public void testMediaType() throws IOException, ApiException, SignatureException, ClientException {
        ApiClientTest act = new ApiClientTest(ConfigurationUtil.fromPropertiesFile(new File("C:/Intel/CloudSecurity/RSATool.properties")));
        String[] roles = {Role.Whitelist.toString(), Role.Attestation.toString(), Role.Security.toString()};
        ApiClientCreateRequest user = new ApiClientCreateRequest();
        String testStr = "[\"X509Certificate\":\"AAAAAAAAAAAAAA==\",\"Roles\":[\"Whitelist\",\"Attestation\",\"Security\"]]";
        user.setCertificate(new byte[10]);
        user.setRoles(roles);
        
        act.testMediaType(user);
    }
    
    @Test
    public void testGetLocales() throws IOException, ClientException, ApiException, SignatureException {
        try {
            ApiClientTest act = new ApiClientTest(ConfigurationUtil.fromPropertiesFile(new File("C:/Intel/CloudSecurity/RSATool.properties")));
            String[] locales = act.getLocales();
            System.out.println("LOCALES: " + Arrays.toString(locales));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
}
