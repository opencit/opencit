/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tls.policy.jaxrs.resource;

import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyLocator;
import com.intel.mtwilson.tls.policy.repository.HostTlsPolicyRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/hosts/{host_id}/tls-policy") // Should the user specified parameter here match the filterCriteria??
public class HostTlsPolicyResource extends AbstractJsonapiResource<com.intel.mtwilson.tls.policy.model.HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, NoLinks<com.intel.mtwilson.tls.policy.model.HostTlsPolicy>, HostTlsPolicyLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private HostTlsPolicyRepository repository;
    
    public HostTlsPolicyResource() {
        repository = new HostTlsPolicyRepository();
    }
    
    @Override
    protected HostTlsPolicyRepository getRepository() { return repository; }

    
    @Override
    protected HostTlsPolicyCollection createEmptyCollection() {
        return new HostTlsPolicyCollection();
    }
      
}
