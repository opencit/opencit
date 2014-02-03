/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyLocator;
import com.intel.mtwilson.as.rest.v2.repository.HostTlsPolicyRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Stateless
@Path("/hosts/{id}/tls-policies") // Should the user specified parameter here match the filterCriteria??
public class HostTlsPolicies extends AbstractJsonapiResource<HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, NoLinks<HostTlsPolicy>, HostTlsPolicyLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTlsPolicies() {
        super();
        setRepository(new HostTlsPolicyRepository());
    }

    
    @Override
    protected HostTlsPolicyCollection createEmptyCollection() {
        return new HostTlsPolicyCollection();
    }
      
}
