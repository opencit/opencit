/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.crypto.password.PasswordUtil;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class UserTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserTest.class);

    private static Users client = null;
    private static UserLoginPasswords passwordClient = null;
    private static UserLoginCertificates certificateClient = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Users(My.configuration().getClientProperties());
        passwordClient = new UserLoginPasswords(My.configuration().getClientProperties());
        certificateClient = new UserLoginCertificates(My.configuration().getClientProperties());
    }
    
    @Test
    public void testUser() throws NoSuchAlgorithmException, CertificateEncodingException {
        
        String userName = "TestUser999";
        
        User createUser = new User();
        createUser.setUsername(userName);
        createUser.setLocale(Locale.US);
        log.debug(LocaleUtil.toLanguageTag(createUser.getLocale()));
        createUser.setComment("Access needed for testing");
        createUser = client.createUser(createUser);
        
        User retrievUser = client.retrieveUser(createUser.getId().toString());
        log.debug("Retrieved user name is {}, locale is {} and comments is {}", retrievUser.getUsername(), retrievUser.getLocale().toString(), retrievUser.getComment());
        
        createUser.setComment("Access approved");
        createUser = client.editUser(createUser);
        
        UserFilterCriteria criteria = new UserFilterCriteria();
        criteria.filter = false;
        UserCollection users = client.searchUsers(criteria);
        for(User user : users.getUsers()) {
            log.debug("Searched user name is {}, locale is {} and comments is {}", user.getUsername(), user.getLocale().toString(), user.getComment());
        }
        
        // Create a login password entry for the user
        UserLoginPassword loginPasswordInfo = new UserLoginPassword();
        loginPasswordInfo.setUserId(createUser.getId());
        loginPasswordInfo.setAlgorithm("SHA256");
        loginPasswordInfo.setIterations(1);
        loginPasswordInfo.setSalt("password".getBytes(Charset.forName("UTF-8")));
        loginPasswordInfo.setPasswordHash(PasswordUtil.hash(("password".getBytes(Charset.forName("UTF-8"))), loginPasswordInfo));
        loginPasswordInfo = passwordClient.createUserLoginPassword(loginPasswordInfo);
        
        loginPasswordInfo.setEnabled(true);
        loginPasswordInfo.setStatus(Status.APPROVED);
        List<String> roleSet = new ArrayList<>(Arrays.asList("administrator", "tagadmin"));
        loginPasswordInfo.setRoles(roleSet);
        loginPasswordInfo = passwordClient.editUserLoginPassword(loginPasswordInfo);
        
        // Create a login certificate entry for the user
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        X509Certificate certificate = X509Builder.factory().selfSigned(String.format("CN=%s", userName), keyPair).expires(365, TimeUnit.DAYS).build();
        
        UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
        userLoginCertificate.setUserId(createUser.getId());
        userLoginCertificate.setCertificate(certificate.getEncoded());
        userLoginCertificate.setComment("Self signed cert.");
        userLoginCertificate = certificateClient.createUserLoginCertificate(userLoginCertificate);        

        userLoginCertificate.setEnabled(true);
        userLoginCertificate.setStatus(Status.APPROVED);
        userLoginCertificate.setRoles(roleSet);
        userLoginCertificate = certificateClient.editUserLoginCertificate(userLoginCertificate);
        
        client.deleteUser(createUser.getId().toString());
    }
    
    @Test
    public void testUserDeleteSearchCriteria() throws Exception {

        UserFilterCriteria criteria = new UserFilterCriteria();
        criteria.nameContains = "Developer";
        client.deleteUser(criteria);
        
    }
     
}
