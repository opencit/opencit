/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionCollection;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.RolePermissionLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.RolePermissionRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;

import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/roles/{role_id}/permissions")
public class RolePermissions extends AbstractJsonapiResource<RolePermission, RolePermissionCollection, RolePermissionFilterCriteria, NoLinks<RolePermission>, RolePermissionLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RolePermissions.class);
    private RolePermissionRepository repository;
    
    public RolePermissions() {
        repository = new RolePermissionRepository();
    }
    
    @Override
    protected RolePermissionCollection createEmptyCollection() {
        return new RolePermissionCollection();
    }

    @Override
    protected RolePermissionRepository getRepository() {
        return repository;
    }
        
}
