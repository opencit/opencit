/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.CaCertificate;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateLinks;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateLocator;
import com.intel.mtwilson.as.rest.v2.repository.CaCertificateRepository;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.AbstractCertificateResource;
import com.intel.mtwilson.ms.common.MSConfig;
import com.intel.mtwilson.ms.common.MSException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.ws.rs.BeanParam;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class CaCertificates extends AbstractCertificateResource<CaCertificate, CaCertificateCollection, CaCertificateFilterCriteria, CaCertificateLinks, CaCertificateLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    private CaCertificateRepository repository;
    
    public CaCertificates() {
        super();
        repository = new CaCertificateRepository();
    }
    
    @Override
    public CaCertificateRepository getRepository() {
        return repository;
    }
}
