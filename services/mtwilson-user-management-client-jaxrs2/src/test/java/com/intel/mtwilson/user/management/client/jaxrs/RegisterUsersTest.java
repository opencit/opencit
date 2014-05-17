/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import java.util.LinkedHashMap;
import java.util.Locale;
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
    public void testRegisterUsers() {

        User createUser = new User();
        createUser.setUsername("Testing111");
        createUser.setComment("Access needed for testing");

        RegisterUserWithCertificate rpcUserWithCert = new RegisterUserWithCertificate(); 
        rpcUserWithCert.setUser(createUser);
        LinkedHashMap registerUserWithCertificate = client.registerUserWithCertificate(rpcUserWithCert);
        log.debug(registerUserWithCertificate.toString());
        
    }
     
}
