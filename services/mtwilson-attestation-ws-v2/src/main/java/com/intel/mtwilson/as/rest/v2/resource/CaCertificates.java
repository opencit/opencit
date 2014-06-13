/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.CaCertificate;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateLocator;
import com.intel.mtwilson.as.rest.v2.repository.CaCertificateRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractCertificateJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/ca-certificates")
public class CaCertificates extends AbstractCertificateJsonapiResource<CaCertificate, CaCertificateCollection, CaCertificateFilterCriteria, NoLinks<CaCertificate>, CaCertificateLocator> {

    private CaCertificateRepository repository;
    
    public CaCertificates() {
        repository = new CaCertificateRepository();
    }
    
    @Override
    protected CaCertificateCollection createEmptyCollection() {
        return new CaCertificateCollection();
    }

    @Override
    protected CaCertificateRepository getRepository() {
        return repository;
    }
    
}
