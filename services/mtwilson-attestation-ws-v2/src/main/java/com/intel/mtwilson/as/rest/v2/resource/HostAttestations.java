/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationLocator;
import com.intel.mtwilson.as.rest.v2.repository.HostAttestationRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/host-attestations")
public class HostAttestations extends AbstractJsonapiResource<HostAttestation, HostAttestationCollection, HostAttestationFilterCriteria, NoLinks<HostAttestation>, HostAttestationLocator> {
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    private HostAttestationRepository repository;

    public HostAttestations() {
        repository = new HostAttestationRepository();
    }
    

    @Override
    protected HostAttestationCollection createEmptyCollection() {
        return new HostAttestationCollection();
    }

    @Override
    protected HostAttestationRepository getRepository() {
        return repository;
    }
    
}
