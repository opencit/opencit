/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostLocator;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.rest.v2.repository.HostRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import javax.ws.rs.Path;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/hosts")
public class Hosts extends AbstractJsonapiResource<Host, HostCollection, HostFilterCriteria, NoLinks<Host>, HostLocator> {

    private HostRepository repository;
    
    public Hosts() {
        repository = new HostRepository();
    }
    
    @Override
    protected HostCollection createEmptyCollection() {
        return new HostCollection();
    }

    @Override
    protected HostRepository getRepository() {
        return repository;
    }
    
}
