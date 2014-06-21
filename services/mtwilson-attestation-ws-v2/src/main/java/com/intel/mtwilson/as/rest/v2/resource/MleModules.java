/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleModuleLocator;
import com.intel.mtwilson.as.rest.v2.repository.MleModuleRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/mles/{mle_id}/modules")
public class MleModules extends AbstractJsonapiResource<MleModule, MleModuleCollection, MleModuleFilterCriteria, NoLinks<MleModule>, MleModuleLocator> {

    private MleModuleRepository repository;
    
    public MleModules() {
        repository = new MleModuleRepository();
    }

    @Override
    protected MleModuleCollection createEmptyCollection() {
        return new MleModuleCollection();
    }

    @Override
    protected MleModuleRepository getRepository() {
        return repository;
    }
    
}
