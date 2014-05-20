/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPasswordLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserLoginPasswordRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;

import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users/{user_id}/login-passwords")
public class UserLoginPasswords extends AbstractJsonapiResource<UserLoginPassword, UserLoginPasswordCollection, UserLoginPasswordFilterCriteria, NoLinks<UserLoginPassword>, UserLoginPasswordLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserLoginPasswords.class);
    private UserLoginPasswordRepository repository;
    
    public UserLoginPasswords() {
        repository = new UserLoginPasswordRepository();
    }
    
    @Override
    protected UserLoginPasswordCollection createEmptyCollection() {
        return new UserLoginPasswordCollection();
    }

    @Override
    protected UserLoginPasswordRepository getRepository() {
        return repository;
    }
        
}
