/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.mtwilson.security.rest.v2.model.Role;
import com.intel.mtwilson.security.rest.v2.model.RoleCollection;
import com.intel.mtwilson.security.rest.v2.model.RoleFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.RoleLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.RoleRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;

import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/roles")
public class Roles extends AbstractJsonapiResource<Role, RoleCollection, RoleFilterCriteria, NoLinks<Role>, RoleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Roles.class);
    private RoleRepository repository;
    
    public Roles() {
        repository = new RoleRepository();
    }
    
    @Override
    protected RoleCollection createEmptyCollection() {
        return new RoleCollection();
    }

    @Override
    protected RoleRepository getRepository() {
        return repository;
    }
        
}
