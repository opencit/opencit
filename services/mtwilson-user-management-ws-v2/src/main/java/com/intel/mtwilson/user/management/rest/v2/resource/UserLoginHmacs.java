/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmac;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmacLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginHmacRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;

import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users/{user_id}/login-hmacs")
public class UserLoginHmacs extends AbstractJsonapiResource<UserLoginHmac, UserLoginHmacCollection, UserLoginHmacFilterCriteria, NoLinks<UserLoginHmac>, UserLoginHmacLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginHmacs.class);
    private UserLoginHmacRepository repository;
    
    public UserLoginHmacs() {
        repository = new UserLoginHmacRepository();
    }
    
    @Override
    protected UserLoginHmacCollection createEmptyCollection() {
        return new UserLoginHmacCollection();
    }

    @Override
    protected UserLoginHmacRepository getRepository() {
        return repository;
    }
        
}
