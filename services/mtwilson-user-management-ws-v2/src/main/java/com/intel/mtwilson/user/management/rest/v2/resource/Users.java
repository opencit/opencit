/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.resource;

import com.intel.mtwilson.user.management.rest.v2.model.User;
import com.intel.mtwilson.user.management.rest.v2.model.UserCollection;
import com.intel.mtwilson.user.management.rest.v2.model.UserFilterCriteria;
import com.intel.mtwilson.user.management.rest.v2.model.UserLocator;
import com.intel.mtwilson.user.management.rest.v2.repository.UserRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;

import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users")
public class Users extends AbstractJsonapiResource<User, UserCollection, UserFilterCriteria, NoLinks<User>, UserLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Users.class);
    private UserRepository repository;
    
    public Users() {
        repository = new UserRepository();
    }
    
    @Override
    protected UserCollection createEmptyCollection() {
        return new UserCollection();
    }

    @Override
    protected UserRepository getRepository() {
        return repository;
    }
        
}
