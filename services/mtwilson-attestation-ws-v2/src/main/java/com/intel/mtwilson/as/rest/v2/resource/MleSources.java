/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.repository.MleSourceRepository;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLocator;
import com.intel.mtwilson.jersey.NoLinks;
import javax.ws.rs.Path;
/**
 *
 * @author ssbangal
 */
@V2
@Path("/mles/{mle_id}/source")
public class MleSources extends AbstractJsonapiResource<MleSource, MleSourceCollection, MleSourceFilterCriteria, NoLinks<MleSource>, MleSourceLocator>{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleSources.class);    
    private MleSourceRepository repository;

    public MleSources() {
        repository = new MleSourceRepository();
    }

    @Override
    protected MleSourceRepository getRepository() { 
        return repository; 
    }
    
    @Override
    protected MleSourceCollection createEmptyCollection() {
        return new MleSourceCollection();
    }
    
}
