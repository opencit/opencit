/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.user.management.client.jaxrs.Users;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
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
    public void testSearchCollection() {
        UserFilterCriteria criteria = new UserFilterCriteria();
        //criteria.id = new UUID();
//        criteria.nameContains = "ibm";
        //criteria.nameEqualTo = "nameequalto";
        UserCollection users = client.searchUsers(criteria);
        for(User user : users.getUsers()) {
//            log.debug("User name {}", user.getName());
        }
    }
    
    @Test
    public void testCreate() {
        User user = new User();
//        user.setName("APIOEM");
//        user.setDescription("API Created OEM");
        User createUser = client.createUser(user);
        log.debug("New OEM created with UUID {}.", createUser.getId().toString());
    }
    
    @Test
    public void testRetrieve() {
        User retrieveUser = client.retrieveUser("27ae76f0-e678-4224-92fc-a91ebbf761b8");
//        log.debug(retrieveUser.getName() + ":::" + retrieveUser.getDescription());
    }

    @Test
    public void testEdit() {
        User user = new User();
//        user.setId(UUID.valueOf("27ae76f0-e678-4224-92fc-a91ebbf761b8"));
//        user.setDescription("Updated description");
//        user = client.editUser(user);
//        log.debug(user.getName() + "--" + user.getId().toString());
    }

    @Test
    public void testDelete() {
        client.deleteUser("27ae76f0-e678-4224-92fc-a91ebbf761b8");
        log.debug("Deleted the OEM successfully");
    }
    
}
