/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.UserCertificate;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateLocator;
import com.intel.mtwilson.as.rest.v2.repository.UserCertificateRepository;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.ApiClientStatus;
import com.intel.mtwilson.datatypes.ApiClientUpdateRequest;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractCertificateJsonapiResource;
import com.intel.mtwilson.jersey.resource.AbstractCertificateResource;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.business.ApiClientBO;
import com.intel.mtwilson.ms.common.MSException;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/user-certificates")
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
