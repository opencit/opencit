/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.AttributeOidAndValue;
import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.model.X509AttributeCertificate;
import com.intel.mtwilson.atag.model.CertificateRequest;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.atag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestApprovalDAO;
import com.intel.mtwilson.atag.Derby;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: 
 * 
 * http://restlet.org/learn/guide/2.2/core/resource/
 * 
 * http://restlet.org/learn/javadocs/2.1/jse/api/org/restlet/data/Reference.html
 *
 * @author jbuhacoff
 */
public class CertificateRequestApprovalResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateDAO certificateDao = null;
    private CertificateRequestDAO certificateRequestDao = null;
    private CertificateRequestApprovalDAO certificateApprovalDao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            certificateDao = Derby.certificateDao();
            certificateRequestDao = Derby.certificateRequestDao();
            certificateApprovalDao = Derby.certificateRequestApprovalDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( certificateDao != null ) { certificateDao.close(); }
        if( certificateRequestDao != null ) { certificateRequestDao.close(); }
        if( certificateApprovalDao != null ) { certificateApprovalDao.close(); }
        super.doRelease();
    }
    
    /**
     * Input format:   { certificate: bytes... }   do not need to provide any hashes, uuid, etc. they are all automatically generated
     * @param certificate
     * @return 
     */
    @Post("json:json")
    public CertificateRequest approveCertificateRequest(Certificate certificate) {
        String uuid = getAttribute("id");
        CertificateRequest certificateRequest =  certificateRequestDao.findByUuid(UUID.valueOf(uuid));
        certificate.setUuid(new UUID());
        certificate.setSha256(Sha256Digest.digestOf(certificate.getCertificate()));
        certificate.setPcrEvent(Sha1Digest.digestOf(certificate.getSha256().toByteArray()));  // pcrEvent = sha1(sha256(certificate))
        long certificateId = certificateDao.insert(certificate.getUuid(), certificate.getCertificate(), certificate.getSha256().toHexString(), certificate.getPcrEvent().toHexString(), certificate.getSubject(), certificate.getIssuer(), certificate.getNotBefore(), certificate.getNotAfter());
        certificateRequest.setCertificateId(certificateId);
        certificateRequest.setStatus("Done");
        // XXX TODO need to validate tags in the input certificate... that we have those tags defined & that values are known, or maybe automatically add new values to our list o fpre-defined values if they are not alraedy there (which means we need to maybe mark values with their source so we can tell if they are still in use ...)
        certificateRequestDao.updateApproved(certificateRequest.getId(), certificateRequest.getCertificateId()); // automatically sets status to 'Done' in db
//        certificateApprovalDao.insert(certificateApproval.getCertificateRequestId(), certificateApproval.isApproved(), certificateApproval.getAuthorityName());
        
        setStatus(Status.SUCCESS_CREATED);
        
        return certificateRequest;
    }

    @Post("bin:json")
    public CertificateRequest approveCertificateRequest(InputStream input) throws IOException {
//        String uuid = getAttribute("id");
        X509AttributeCertificate cert = X509AttributeCertificate.valueOf(IOUtils.toByteArray(input));
        String tags = ""; for(AttributeOidAndValue attr : cert.getTags()) { tags += attr.getOid()+": "+attr.getValue(); }
        log.debug("Received certificate: {}", String.format("issuer: %s  subject: %s  from: %s  to: %s  attrs: %s", cert.getIssuer(), cert.getSubject(), cert.getNotBefore().toString(), cert.getNotAfter().toString(), tags));
        Certificate record = new Certificate();
        record.setCertificate(cert.getEncoded());
        return approveCertificateRequest(record);
    }
}
