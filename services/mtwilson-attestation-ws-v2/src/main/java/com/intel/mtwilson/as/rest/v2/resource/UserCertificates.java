/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.UserCertificate;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateLocator;
import com.intel.mtwilson.as.rest.v2.repository.UserCertificateRepository;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractCertificateJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/users/{user_id}/certificates")
public class UserCertificates extends AbstractCertificateJsonapiResource<UserCertificate, UserCertificateCollection, UserCertificateFilterCriteria, NoLinks<UserCertificate>, UserCertificateLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private UserCertificateRepository repository;
    
    public UserCertificates() {
        repository = new UserCertificateRepository();
    }
    
    @Override
    protected UserCertificateCollection createEmptyCollection() {
        return new UserCertificateCollection();
    }

    @Override
    protected UserCertificateRepository getRepository() {
        return repository;
    }
    
}
