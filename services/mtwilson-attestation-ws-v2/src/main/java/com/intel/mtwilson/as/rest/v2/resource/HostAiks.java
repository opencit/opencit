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
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractCertificateJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/hosts/{host_id}/aiks")
public class HostAiks extends AbstractCertificateJsonapiResource<HostAik, HostAikCollection, HostAikFilterCriteria, NoLinks<HostAik>, HostAikLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private HostAikRepository repository;

    public HostAiks(HostAikRepository repository) {
        this.repository = repository;
    }

    @Override
    protected HostAikCollection createEmptyCollection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HostAikRepository getRepository() {
        return repository;
    }
    
    
}
