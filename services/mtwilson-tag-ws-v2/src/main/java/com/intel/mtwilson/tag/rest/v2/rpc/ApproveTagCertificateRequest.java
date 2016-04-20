/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.mtwilson.tag.model.CertificateRequestLocator;
import org.apache.shiro.authz.annotation.RequiresPermissions;


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
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApproveTagCertificateRequest.class);

       
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
    @RequiresPermissions({"tag_certificates:create","tag_certificate_requests:store"})         
    public void run() {
        log.debug("RPC: ApproveTagCertificateRequest - Got request to auto approve certificate request with ID {}.", certificateRequestId);
        CertificateRequestLocator locator = new CertificateRequestLocator();
        locator.id = certificateRequestId;
        
        try (CertificateRequestDAO certRequestdao = TagJdbi.certificateRequestDao();
                CertificateDAO certDao = TagJdbi.certificateDao()) {
        
            CertificateRequest obj = certRequestdao.findById(certificateRequestId);
            if (obj != null) {
                
                X509AttributeCertificate cert = X509AttributeCertificate.valueOf(certificate);
                log.debug("RPC: ApproveTagCertificateRequest - Received certificate: {}", String.format("issuer: %s  subject: %s  from: %s  to: %s  attrs: %s", 
                        cert.getIssuer(), cert.getSubject(), cert.getNotBefore().toString(), cert.getNotAfter().toString()));

                Certificate certificate = Certificate.valueOf(cert.getEncoded());
                UUID newCertId = new UUID();
                certificate.setId(newCertId);

                certDao.insert(certificate.getId(), certificate.getCertificate(), certificate.getSha1().toHexString(), certificate.getSha256().toHexString(), 
                        certificate.getSubject(), certificate.getIssuer(), certificate.getNotBefore(), certificate.getNotAfter());
                
                //certRequestdao.updateApproved(certificateRequestId.toString(), newCertId.toString()); // automatically sets status to 'Done' in db
                certRequestdao.updateStatus(certificateRequestId, "Done");
            } else {
                log.error("RPC: ApproveTagCertificateRequest - Certificate request id {} specified for auto approval is not valid.", certificateRequestId);
                throw new RepositoryInvalidInputException(locator);
            }

        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("RPC: ApproveTagCertificateRequest - Error during approval of certificate request.", ex);
            throw new RepositoryException(ex);
        } 
        
    }
    
}
