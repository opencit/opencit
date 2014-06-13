/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OemLocator;
import com.intel.mtwilson.as.rest.v2.repository.OemRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/oems")
public class Oems extends AbstractJsonapiResource<Oem, OemCollection, OemFilterCriteria, NoLinks<Oem>, OemLocator>{
    
    private OemRepository repository;
    
    public Oems() {
        repository = new OemRepository();
    }
    
    @Override
    protected OemCollection createEmptyCollection() {
        return new OemCollection();
    }

    @Override
    protected OemRepository getRepository() {
        return repository;
    }
    
}
