/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.UserRole;
import com.intel.mtwilson.as.rest.v2.model.UserRoleCollection;
import java.security.cert.CertificateException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class UserRoleTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileTest.class);

    private static UserRoles client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new UserRoles(My.configuration().getClientProperties());
    }
       
    @Test
    public void testRetrieve() throws CertificateException {
        UserRoleCollection roles = client.retrieveUserRoles();
        log.debug(roles.getUserRoles().toString());
    }

}
