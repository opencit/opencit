/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleLocator;
import com.intel.mtwilson.as.rest.v2.repository.MleRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/mles")
public class Mles extends AbstractJsonapiResource<Mle, MleCollection, MleFilterCriteria, NoLinks<Mle>, MleLocator> {

    private MleRepository repository;
    
    public Mles() {
        repository = new MleRepository();
    }
    
    @Override
    protected MleCollection createEmptyCollection() {
        return new MleCollection();
    }

    @Override
    protected MleRepository getRepository() {
        return repository;
    }
        
}
