/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tpm.endorsement.jaxrs.resource;

import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementCollection;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementFilterCriteria;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementLocator;
import com.intel.mtwilson.tpm.endorsement.repository.TpmEndorsementRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 * Example registration of tpm endorsement certificate:
 * 
 * <pre>
 * </pre>
 * 
 * @author ssbangal
 */
@V2
@Path("/tpm-endorsements")
public class TpmEndorsementResource extends AbstractJsonapiResource<com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement, TpmEndorsementCollection, TpmEndorsementFilterCriteria, NoLinks<com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement>, TpmEndorsementLocator> {

    private TpmEndorsementRepository repository;
    
    public TpmEndorsementResource() {
        repository = new TpmEndorsementRepository();
    }
    
    @Override
    protected TpmEndorsementRepository getRepository() { return repository; }

    
    @Override
    protected TpmEndorsementCollection createEmptyCollection() {
        return new TpmEndorsementCollection();
    }
      
}
