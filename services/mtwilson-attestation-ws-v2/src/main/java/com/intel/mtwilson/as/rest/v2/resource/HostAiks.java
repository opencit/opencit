/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.HostAik;
import com.intel.mtwilson.as.rest.v2.model.HostAikCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostAikLocator;
import com.intel.mtwilson.as.rest.v2.repository.HostAikRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/hosts/{host_id}/aiks")
public class HostAiks extends AbstractJsonapiResource<HostAik, HostAikCollection, HostAikFilterCriteria, NoLinks<HostAik>, HostAikLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAiks.class);

    private HostAikRepository repository;

    public HostAiks() {
        repository = new HostAikRepository();
    }

    @Override
    protected HostAikCollection createEmptyCollection() {
        return new HostAikCollection();
    }

    @Override
    protected HostAikRepository getRepository() {
        return repository;
    }
    
    
}
