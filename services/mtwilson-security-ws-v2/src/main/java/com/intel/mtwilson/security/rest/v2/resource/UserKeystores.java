/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.rest.v2.resource;

import com.intel.mtwilson.security.rest.v2.model.UserKeystore;
import com.intel.mtwilson.security.rest.v2.model.UserKeystoreCollection;
import com.intel.mtwilson.security.rest.v2.model.UserKeystoreFilterCriteria;
import com.intel.mtwilson.security.rest.v2.model.UserKeystoreLocator;
import com.intel.mtwilson.security.rest.v2.repository.UserKeystoreRepository;
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
@Path("/users/{user_id}/keystores")
public class UserKeystores extends AbstractJsonapiResource<UserKeystore, UserKeystoreCollection, UserKeystoreFilterCriteria, NoLinks<UserKeystore>, UserKeystoreLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserKeystores.class);
    private UserKeystoreRepository repository;
    
    public UserKeystores() {
        repository = new UserKeystoreRepository();
    }
    
    @Override
    protected UserKeystoreCollection createEmptyCollection() {
        return new UserKeystoreCollection();
    }

    @Override
    protected UserKeystoreRepository getRepository() {
        return repository;
    }
        
}
