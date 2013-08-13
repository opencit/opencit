/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.CertificateRequest;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.atag.Derby;
import com.intel.dcsg.cpg.io.UUID;
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
public class CertificateRequestResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateRequestDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.certificateRequestDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( dao != null ) { dao.close(); }
        super.doRelease();
    }
    
    @Get("json")
    public CertificateRequest existingCertificateRequest() {
        String uuid = getAttribute("id");
        return dao.findByUuid(UUID.valueOf(uuid));
    }

    @Delete
    public void deleteCertificateRequest() {
        String uuid = getAttribute("id");
        CertificateRequest certificateRequest = dao.findByUuid(UUID.valueOf(uuid));
        dao.delete(certificateRequest.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    /*
    @Put("text/plain")
    public CertificateRequest updateCertificateRequest(CertificateRequest updatedCertificateRequest) throws SQLException {
        String uuid = getAttribute("id");
        CertificateRequest existingCertificateRequest = dao.findByUuid(UUID.valueOf(uuid));
        dao.update(existingCertificateRequest.getId(), updatedCertificateRequest.getSubject(), updatedCertificateRequest.getPredicate(), updatedCertificateRequest.getObject());
        return updatedCertificateRequest;
    }
    */
}
