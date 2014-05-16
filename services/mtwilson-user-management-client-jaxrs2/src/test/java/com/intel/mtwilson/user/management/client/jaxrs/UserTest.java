/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class UserTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserTest.class);

    private static Users client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Users(My.configuration().getClientProperties());
    }
    
    @Test
    public void testUser() {
        
        UUID userId = new UUID();
        String userName = "TestUser999";
        
        User createUser = new User();
        createUser.setId(userId);
        createUser.setUsername(userName);
        createUser.setLocale(Locale.US);
        createUser.setComment("Access needed for testing");
        client.createUser(createUser);
        
        User retrievUser = client.retrieveUser(userId.toString());
        log.debug("Retrieved user name is {}, locale is {} and comments is {}", retrievUser.getUsername(), retrievUser.getLocale().toString(), retrievUser.getComment());
        
        createUser.setComment("Access approved");
        client.editUser(createUser);
        
        UserFilterCriteria criteria = new UserFilterCriteria();
        criteria.filter = false;
        UserCollection users = client.searchUsers(criteria);
        for(User user : users.getUsers()) {
            log.debug("Searched user name is {}, locale is {} and comments is {}", user.getUsername(), user.getLocale().toString(), user.getComment());
        }
        
        client.deleteUser(userId.toString());
    }
     
}
