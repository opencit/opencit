/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.UserRole;
import com.intel.mtwilson.as.rest.v2.model.UserRoleCollection;
import com.intel.mtwilson.as.rest.v2.model.UserRoleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserRoleLocator;
import com.intel.mtwilson.as.rest.v2.repository.UserRoleRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users/roles")
public class UserRoles extends AbstractJsonapiResource<UserRole, UserRoleCollection, UserRoleFilterCriteria, NoLinks<UserRole>, UserRoleLocator> {
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private UserRoleRepository repository;

    public UserRoles() {
        this.repository = new UserRoleRepository();
    }
        
    @Override
    protected UserRoleCollection createEmptyCollection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected UserRoleRepository getRepository() {
        return repository;
    }
    
}
