/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.TlsPolicy;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicyFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicyLocator;
import com.intel.mtwilson.as.rest.v2.repository.TlsPolicyRepository;
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
public class TlsPolicies extends AbstractJsonapiResource<TlsPolicy, TlsPolicyCollection, TlsPolicyFilterCriteria, NoLinks<TlsPolicy>, TlsPolicyLocator> {

    Logger log = LoggerFactory.getLogger(getClass().getName());

    public TlsPolicies() {
        super();
        setRepository(new TlsPolicyRepository());
    }

    
    @Override
    protected TlsPolicyCollection createEmptyCollection() {
        return new TlsPolicyCollection();
    }
      
}
