/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RoleTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleTest.class);

    private static Roles client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Roles(My.configuration().getClientProperties());
    }
    
    @Test
    public void testRole() {
        
        UUID roleId = new UUID();
        
        Role createRole = new Role();
        createRole.setId(roleId);
        createRole.setRoleName("Admin999");
        createRole.setDescription("Admin role");
        client.createRole(createRole);
        
        Role retrievRole = client.retrieveRole(createRole.getId().toString());
        log.debug("Retrieved role name is {}, and description is {}", retrievRole.getRoleName(), retrievRole.getDescription());
        
        createRole.setDescription("Updated Admin role description.");
        client.editRole(createRole);
        
        RoleFilterCriteria criteria = new RoleFilterCriteria();
        criteria.filter = false;
        RoleCollection users = client.searchRoles(criteria);
        for(Role user : users.getRoles()) {
            log.debug("Searched role name is {}, and description is {}", user.getRoleName(), user.getDescription());
        }
        
        client.deleteRole(roleId.toString());
    }
 
    @Test
    public void testRoleDeleteSearchCriteria() throws Exception {

        RoleFilterCriteria criteria = new RoleFilterCriteria();
        criteria.nameContains = "Developer";
        client.deleteRole(criteria);
        
    }
    
}
