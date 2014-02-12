/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api;

import com.intel.dcsg.cpg.io.ConfigurationUtil;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.Role;
import java.io.File;
import java.io.IOException;
import java.security.SignatureException;
import org.junit.Test;

/**
 *
 * @author rksavinx
 */
public class ApiTest {
    public ApiTest() { }
    
    @Test
    public void testMediaType() throws IOException, ApiException, SignatureException, ClientException {
//        String username = "rksavinx";
//        String password = "savinorules";
//        ByteArrayResource certResource = new ByteArrayResource();
//        SimpleKeystore keystore = new SimpleKeystore(certResource, password); // KeystoreUtil.createUserInResource(certResource, username, password, fullUrl, roles);

        ApiClientTest act = new ApiClientTest(ConfigurationUtil.fromPropertiesFile(new File("C:/Intel/CloudSecurity/RSATool.properties")));
        String[] roles = {Role.Whitelist.toString(), Role.Attestation.toString(), Role.Security.toString()};
        ApiClientCreateRequest user = new ApiClientCreateRequest();
        String testStr = "[\"X509Certificate\":\"AAAAAAAAAAAAAA==\",\"Roles\":[\"Whitelist\",\"Attestation\",\"Security\"]]";
        //String testStr="[\"X509Certificate\":\"test2\",\"Roles\":\"test4\"]";
//        RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(username, password);
//        System.out.println(new String(rsaCredential.getCertificate().getEncoded(), Charset.forName("UTF-8")));
        user.setCertificate(new byte[10]);
        user.setRoles(roles);
        
        act.testMediaType(user);

        
    }
}
