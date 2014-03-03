/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.User;
import com.intel.mtwilson.as.rest.v2.model.UserCertificate;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserCollection;
import com.intel.mtwilson.as.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.datatypes.Role;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class UserTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTest.class);

    private static Users client = null;
    private static UserCertificates client2 = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Users(My.configuration().getClientProperties());
        client2 = new UserCertificates(My.configuration().getClientProperties());
    }
    
    @Test
    public void testSearchCollection() {
        UserFilterCriteria criteria = new UserFilterCriteria();
        criteria.nameContains = "d";
        UserCollection objCollection = client.searchUsers(criteria);
        for(User obj : objCollection.getUsers()) {
            log.debug("User name {}", obj.getName());
        }
    }
    
    @Test
    public void testCreate() {
        User obj = new User();
        obj.setName("ApiUser1");
        obj.setLocale("en");
        obj.setComments("Api client testing");
        User createUser = client.createUser(obj);
        log.debug("New User created with UUID {}.", createUser.getId().toString());
    }
    
    @Test
    public void testCreateUserAndUserCert() throws Exception {

        String username = "superadmin";
        String password = "password";
        String locale = "en";
        
        String[] roles = new String[] {Role.Security.toString(), Role.Whitelist.toString(), Role.Attestation.toString()}; 
        
        ByteArrayResource resource = new ByteArrayResource();
        SimpleKeystore keystore = new SimpleKeystore(resource, password); // KeyManagementException
        KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE); // NoSuchAlgorithmException
        X509Certificate certificate = RsaUtil.generateX509Certificate(username, keypair, RsaUtil.DEFAULT_RSA_KEY_EXPIRES_DAYS); 
        keystore.addKeyPairX509(keypair.getPrivate(), certificate, username, password); // KeyManagementException
        keystore.save(); 

        User userObj = new User();
        userObj.setName(username);
        userObj.setComments("Created by junit test");
        userObj.setLocale(locale);
        userObj.setKeystore(resource.toByteArray());
        User newUser = client.createUser(userObj);
        
        UserCertificate userCertObj = new UserCertificate();
        userCertObj.setCertificate(certificate.getEncoded());
        userCertObj.setRoles(roles);
        userCertObj.setUserUuid(newUser.getId());
        UserCertificate newUserCert = client2.createUserCertificate(userCertObj);
        
        newUser.setEnabled(Boolean.TRUE);
        newUser.setStatus("APPROVED");
        newUser = client.editUser(newUser);
        
        newUserCert.setEnabled(true);
        newUserCert.setStatus("APPROVED");
        newUserCert = client2.editUserCertificate(newUserCert);
        
    }
    
    @Test
    public void testRetrieve() {
        User retrieveUser = client.retrieveUser("56bb5400-64ea-47c7-99c4-958c6f721717");
        log.debug(retrieveUser.getName() + ":::" + retrieveUser.getStatus());
    }

    @Test
    public void testEdit() {
        User obj = new User();
        obj.setId(UUID.valueOf("56bb5400-64ea-47c7-99c4-958c6f721717"));
        obj.setEnabled(Boolean.TRUE);
        obj.setStatus("APPROVED");
        obj = client.editUser(obj);
        log.debug(obj.getName() + "--" + obj.getId().toString());
    }

    @Test
    public void testDelete() {
        client.deleteUser("56bb5400-64ea-47c7-99c4-958c6f721717");
        log.debug("Deleted the user successfully");
    }
    
    @Test
    public void testSearchUserCertCollection() {
        UserCertificateFilterCriteria criteria = new UserCertificateFilterCriteria();
        criteria.userUuid = UUID.valueOf("41696196-632f-4513-a0f5-bf687c125548");
        UserCertificateCollection objCollection = client2.searchUserCertificates(criteria);
        for(UserCertificate obj : objCollection.getUserCertificates()) {
            log.debug("User cert name {}", obj.getName());
        }
    }
    
    @Test
    public void testRetrieveUserCert() {
        UserCertificate obj = client2.retrieveUserCertificate("41696196-632f-4513-a0f5-bf687c125548", "d70d26fc-9776-46ca-bf28-00dbae8855fc");
        log.debug("User cert name {}", obj.getName());
    }
    
}
