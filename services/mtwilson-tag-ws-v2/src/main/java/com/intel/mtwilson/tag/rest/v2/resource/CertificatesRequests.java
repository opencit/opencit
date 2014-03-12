/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestCollection;
import com.intel.mtwilson.tag.model.CertificateRequestFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateRequestLocator;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRequestRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/certificate_requests")
public class CertificatesRequests extends AbstractJsonapiResource<CertificateRequest, CertificateRequestCollection, CertificateRequestFilterCriteria, NoLinks<CertificateRequest>, CertificateRequestLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private CertificateRequestRepository repository;
    
    public CertificatesRequests() {
        repository = new CertificateRequestRepository();
    }
    
    @Override
    protected CertificateRequestCollection createEmptyCollection() {
        return new CertificateRequestCollection();
    }

    @Override
    protected CertificateRequestRepository getRepository() {
        return repository;
    }
        
}
