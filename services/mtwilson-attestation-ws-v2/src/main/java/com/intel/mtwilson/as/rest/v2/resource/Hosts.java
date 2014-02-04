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
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/hosts/{id}")
public class Hosts extends AbstractJsonapiResource<Host, HostCollection, HostFilterCriteria, NoLinks<Host>, HostLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
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
