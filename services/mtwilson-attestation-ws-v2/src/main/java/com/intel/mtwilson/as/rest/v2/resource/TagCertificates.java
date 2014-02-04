/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwAssetTagCertificateJpaController;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.as.business.AssetTagCertBO;
import com.intel.mtwilson.as.rest.v2.repository.TagCertificateRepository;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractCertificateJsonapiResource;
import com.intel.mtwilson.jersey.resource.SimpleRepository;

import java.util.List;
//import javax.ejb.Stateless;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
//@Stateless
@Path("/tag-certificates/{id}")
public class TagCertificates extends AbstractCertificateJsonapiResource<TagCertificate, TagCertificateCollection, TagCertificateFilterCriteria, NoLinks<TagCertificate>, TagCertificateLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
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
