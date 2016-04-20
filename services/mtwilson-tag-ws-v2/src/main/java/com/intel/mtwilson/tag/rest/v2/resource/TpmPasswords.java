/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import com.intel.mtwilson.tag.model.TpmPasswordLocator;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.TpmPasswordRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ssbangal
 */
@V2
@Path("/host-tpm-passwords")
public class TpmPasswords extends AbstractJsonapiResource<TpmPassword, TpmPasswordCollection, TpmPasswordFilterCriteria, NoLinks<TpmPassword>, TpmPasswordLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmPasswords.class);

    private TpmPasswordRepository repository;
    
    public TpmPasswords() {
        repository = new TpmPasswordRepository();
    }
    
    @Override
    protected TpmPasswordCollection createEmptyCollection() {
        return new TpmPasswordCollection();
    }

    @Override
    protected TpmPasswordRepository getRepository() {
        return repository;
    }
        
}
