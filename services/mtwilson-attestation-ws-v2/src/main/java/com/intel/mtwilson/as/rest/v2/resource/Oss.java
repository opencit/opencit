/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OsLocator;
import com.intel.mtwilson.as.rest.v2.repository.OsRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/oss")
public class Oss extends AbstractJsonapiResource<Os, OsCollection, OsFilterCriteria, NoLinks<Os>, OsLocator>{

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private OsRepository repository;
    
    public Oss() {
        repository = new OsRepository();
    }
    
    @Override
    protected OsCollection createEmptyCollection() {
        return new OsCollection();
    }

    @Override
    protected SimpleRepository<Os, OsCollection, OsFilterCriteria, OsLocator> getRepository() {
        return repository;
    }
        
}
