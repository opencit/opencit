/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.resource;

import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateCollection;
import com.intel.mtwilson.tag.model.CertificateFilterCriteria;
import com.intel.mtwilson.tag.model.CertificateLocator;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractCertificateJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.tag.rest.v2.repository.CertificateRepository;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/tag-certificates")
public class Certificates extends AbstractCertificateJsonapiResource<Certificate, CertificateCollection, CertificateFilterCriteria, NoLinks<Certificate>, CertificateLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Certificates.class);

    private CertificateRepository repository;
    
    public Certificates() {
        repository = new CertificateRepository();
    }
    
    @Override
    protected CertificateCollection createEmptyCollection() {
        return new CertificateCollection();
    }

    @Override
    protected CertificateRepository getRepository() {
        return repository;
    }
        
}
