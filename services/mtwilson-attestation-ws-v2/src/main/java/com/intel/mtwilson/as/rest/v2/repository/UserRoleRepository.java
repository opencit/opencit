/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mtwilson.as.rest.v2.model.UserRole;
import com.intel.mtwilson.as.rest.v2.model.UserRoleCollection;
import com.intel.mtwilson.as.rest.v2.model.UserRoleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserRoleLocator;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class UserRoleRepository implements SimpleRepository<UserRole, UserRoleCollection, UserRoleFilterCriteria, UserRoleLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public UserRoleCollection search(UserRoleFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UserRole retrieve(UserRoleLocator locator) {
        UserRole obj = new UserRole();
        obj.setRoles(new Role[] { Role.Security, Role.Whitelist, Role.Attestation, Role.Report, Role.Audit, Role.AssetTagManagement });
        return obj;
    }

    @Override
    public void store(UserRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(UserRole item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(UserRoleLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(UserRoleFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
