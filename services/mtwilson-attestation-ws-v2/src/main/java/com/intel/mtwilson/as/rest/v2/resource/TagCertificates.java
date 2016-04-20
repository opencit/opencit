/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateLocator;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.rest.v2.repository.TagCertificateRepository;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractCertificateJsonapiResource;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/host-tag-certificates")
public class TagCertificates extends AbstractCertificateJsonapiResource<TagCertificate, TagCertificateCollection, TagCertificateFilterCriteria, NoLinks<TagCertificate>, TagCertificateLocator> {

    private TagCertificateRepository repository;
    
    public TagCertificates() {
        repository = new TagCertificateRepository();
    }
    
    @Override
    protected TagCertificateCollection createEmptyCollection() {
        return new TagCertificateCollection();
    }

    @Override
    protected TagCertificateRepository getRepository() {
        return repository;
    }
        
}
