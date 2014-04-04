/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.model.CertificateRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For use by an external CA if one is configured. The external CA would
 * use the /tag-certificate-requests search API to find pending requests,
 * generate the certificates, and then post the certificates back using
 * this RPC.
 * 
 * @author ssbangal
 */
@RPC("approve-tag-certificate-request")
@JacksonXmlRootElement(localName="approve_tag_certificate_request")
public class ApproveTagCertificateRequest implements Runnable{
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
       
    private UUID certificateRequestId;
    private byte[] certificate;

    public UUID getCertificateRequestId() {
        return certificateRequestId;
    }

    public void setCertificateRequestId(UUID certificateRequestId) {
        this.certificateRequestId = certificateRequestId;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    
    @Override
    @RequiresPermissions({"certificates:create","certificate_requests:store"})         
    public void run() {
        log.debug("Got request to auto approve certificate request with ID {}.", certificateRequestId);        
        try (CertificateRequestDAO certRequestdao = TagJdbi.certificateRequestDao();
                CertificateDAO certDao = TagJdbi.certificateDao()) {
        
            CertificateRequest obj = certRequestdao.findById(certificateRequestId);
            if (obj != null) {
                
                X509AttributeCertificate cert = X509AttributeCertificate.valueOf(certificate);
                log.debug("Received certificate: {}", String.format("issuer: %s  subject: %s  from: %s  to: %s  attrs: %s", 
                        cert.getIssuer(), cert.getSubject(), cert.getNotBefore().toString(), cert.getNotAfter().toString()));

                Certificate certificate = Certificate.valueOf(cert.getEncoded());
                UUID newCertId = new UUID();
                certificate.setId(newCertId);

                certDao.insert(certificate.getId(), certificate.getCertificate(), certificate.getSha1().toHexString(), certificate.getSha256().toHexString(), 
                        certificate.getSubject(), certificate.getIssuer(), certificate.getNotBefore(), certificate.getNotAfter());
                
                // XXX TODO need to validate tags in the input certificate... that we have those tags defined & that values are known, or maybe automatically add new values to our list o fpre-defined values if they are not alraedy there (which means we need to maybe mark values with their source so we can tell if they are still in use ...)
                //certRequestdao.updateApproved(certificateRequestId.toString(), newCertId.toString()); // automatically sets status to 'Done' in db
                certRequestdao.updateStatus(certificateRequestId, "Done");
            } else {
                log.error("Certificate request id {} specified for auto approval is not valid.", certificateRequestId);
                throw new WebApplicationException("Certificate request id specified for auto approval is not valid.", Response.Status.NOT_FOUND);
            }

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during auto approve of certificate.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } 
        
    }
    
}
