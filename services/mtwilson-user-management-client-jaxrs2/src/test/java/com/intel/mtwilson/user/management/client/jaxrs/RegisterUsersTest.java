/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RegisterUsersTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterUsersTest.class);

    private static RegisterUsers client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new RegisterUsers(My.configuration().getClientProperties());
    }
    
    @Test
    public void testRegisterUserWithoutCert() {

        User createUser = new User();
        createUser.setUsername("Testing111");
        createUser.setComment("Access needed for testing");

        RegisterUserWithCertificate rpcUserWithCert = new RegisterUserWithCertificate(); 
        rpcUserWithCert.setUser(createUser);
        boolean registerUserWithCertificate = client.registerUserWithCertificate(rpcUserWithCert);
        log.debug("Status of user registration is {}.", registerUserWithCertificate);
    }

    @Test
    public void testRegisterUserWithCert() throws Exception {
        
        String userName = "superadmin99";
        User user = new User();
        user.setUsername(userName);
        user.setLocale(Locale.US);
        user.setComment("Need to manage user accounts."); 
        
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        X509Certificate certificate = X509Builder.factory().selfSigned(String.format("CN=%s", userName), keyPair).expires(365, TimeUnit.DAYS).build();
        
        UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
        userLoginCertificate.setCertificate(certificate.getEncoded());
                
        RegisterUserWithCertificate rpcUserWithCert = new RegisterUserWithCertificate(); 
        rpcUserWithCert.setUser(user);
        rpcUserWithCert.setUserLoginCertificate(userLoginCertificate);
        boolean registerUserWithCertificate = client.registerUserWithCertificate(rpcUserWithCert);
        log.debug("Status of user registration is {}.", registerUserWithCertificate);
    }    
    
}
