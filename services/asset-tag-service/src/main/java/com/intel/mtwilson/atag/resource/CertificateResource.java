/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.atag.Derby;
import com.intel.mtwilson.atag.My;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.io.pem.Pem;
import java.sql.SQLException;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class CertificateResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.certificateDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( dao != null ) { dao.close(); }
        super.doRelease();
    }
    
    @Get("json|xml")
    public Certificate existingCertificate() {
        String uuid = getAttribute("id");
        return dao.findByUuid(UUID.valueOf(uuid));
    }
    
    @Get("bin")
    public byte[] existingCertificateContent() {
        String uuid = getAttribute("id");
        Certificate certificate = dao.findByUuid(UUID.valueOf(uuid)); 
        return certificate.getCertificate();
    }
    
    @Get("txt")
    public String existingCertificateContentPem() {
        String uuid = getAttribute("id");
        Certificate certificate = dao.findByUuid(UUID.valueOf(uuid)); 
        log.debug("resource url: {}", getRequest().getResourceRef().getIdentifier());
        Pem pem = new Pem("X509 ATTRIBUTE CERTIFICATE", certificate.getCertificate());
        pem.getHeaders().put("URL", My.config().getServerURL()+"/certificates/"+uuid);
        return pem.toString();
    }

    @Delete
    public void deleteCertificate() {
        String uuid = getAttribute("id");
        Certificate tag = dao.findByUuid(UUID.valueOf(uuid));
        dao.delete(tag.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

}
