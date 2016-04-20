/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.rpc.RegisterUserWithCertificateRunnable;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.intel.dcsg.cpg.util.shiro.Login;
import org.junit.Test;
import org.junit.BeforeClass;


/**
 *
 * @author ssbangal
 */
public class RegisterUserWithCertTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterUserWithCertTest.class);
    
    @BeforeClass
    public static void login() throws Exception {
        Login.superuser();
    }
    
    @Test
    public void testRegisterUserWithCert() throws Exception {
        
        KeyPair keyPair;
        X509Certificate certificate;
        
        UUID userId = new UUID();        
        UUID userCertId = new UUID();
        String userName = "superadmin2";
        
        User user = new User();
        user.setId(userId);
        user.setUsername(userName);
        user.setLocale(Locale.US);
        user.setComment("Need to manage user accounts."); 
        
        keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        certificate = X509Builder.factory().selfSigned(String.format("CN=%s", userName), keyPair).expires(365, TimeUnit.DAYS).build();
        
        UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
        userLoginCertificate.setId(userCertId);
        userLoginCertificate.setUserId(userId);
        userLoginCertificate.setCertificate(certificate.getEncoded());
        userLoginCertificate.setComment("Self signed cert.");
        userLoginCertificate.setExpires(certificate.getNotAfter());
        userLoginCertificate.setSha1Hash(Sha1Digest.digestOf(certificate.getEncoded()).toByteArray());
        userLoginCertificate.setSha256Hash(Sha256Digest.digestOf(certificate.getEncoded()).toByteArray());
                
        RegisterUserWithCertificateRunnable rpcRunnable = new RegisterUserWithCertificateRunnable();
        rpcRunnable.setUser(user);
        rpcRunnable.setUserLoginCertificate(userLoginCertificate);
        rpcRunnable.run();        
    }    
    
}
