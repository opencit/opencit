/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.user.management.rest.v2.model.Role;
import com.intel.mtwilson.user.management.rest.v2.model.RoleCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RoleLocator;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.repository.RolePermissionRepository;
import com.intel.mtwilson.user.management.rest.v2.repository.RoleRepository;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class RoleAndRolePermissionTest {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleAndRolePermissionTest.class);
    
    @Test
    public void testRoleAndRolePermissions() throws Exception {

        RoleRepository rRepo = new RoleRepository();
        RolePermissionRepository rpRepo = new RolePermissionRepository();
        UUID roleId = new UUID();
        UUID rolePermId = new UUID();

        Role role = new Role();
        role.setId(roleId);
        role.setRoleName("MTW_Manager");
        role.setDescription("Mt Wilson Manager role");
        rRepo.create(role);
        
        RoleFilterCriteria rCriteria = new RoleFilterCriteria();
        rCriteria.filter = false;
        RoleCollection rSearch = rRepo.search(rCriteria);
        for (Role roleObj : rSearch.getRoles())
            log.debug("Retrieved role with id {}, name {} and description {}.", roleObj.getId(), roleObj.getRoleName(), roleObj.getDescription());
        
        role.setDescription("Role description updated");
        rRepo.store(role);
        
        RoleLocator rLocator = new RoleLocator();
        rLocator.id = roleId;
        Role rRetrieve = rRepo.retrieve(rLocator);
        log.debug("Retrieved role with id {}, name {} and description {}.", rRetrieve.getId(), rRetrieve.getRoleName(), rRetrieve.getDescription());
        
        RolePermission rpObj = new RolePermission();
        rpObj.setRoleId(roleId);
        rpObj.setPermitDomain("role_permissions");
        rpObj.setPermitAction("store,search,create,retrieve");
        rpRepo.create(rpObj);

        rpObj.setPermitDomain("roles");
        rpObj.setPermitAction("store,search,create,retrieve");
        rpObj.setPermitSelection("*");
        rpRepo.create(rpObj);
        
        RolePermissionFilterCriteria rpCriteria = new RolePermissionFilterCriteria();
        rpCriteria.roleId = roleId;
        rpCriteria.filter = false;
        RolePermissionCollection search = rpRepo.search(rpCriteria);
        for (RolePermission rp : search.getRolePermissions()) {
            log.debug("Role Id {}, domain is {}, action is {} and selection is {}", rp.getRoleId(), rp.getPermitDomain(), rp.getPermitAction(), rp.getPermitSelection());
        }

        try {
            rRepo.delete(rLocator);
        } catch(Exception ex) {
            log.debug("Error details {}", ex);
        }
        
        rpRepo.delete(rpCriteria);
        rRepo.delete(rLocator);
        
    }    
    
}
