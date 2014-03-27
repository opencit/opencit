/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.model.Certificate;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@RPC("revoke_tag_certificate")
@JacksonXmlRootElement(localName="revoke_tag_certificate")
public class RevokeTagCertificate implements Runnable{
    
    private Logger log = LoggerFactory.getLogger(getClass().getName());
       
    private UUID certificateId;

    public UUID getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(UUID certificateId) {
        this.certificateId = certificateId;
    }
    
    @Override
    public void run() {
        log.debug("Got request to revocation of certificate with ID {}.", certificateId);        
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
        
            Certificate obj = dao.findById(certificateId);
            if (obj != null) 
            {
                log.debug("Sha1 of the certificate about to be revoked is {}.", obj.getSha1());
                dao.updateRevoked(certificateId, true);                
                AssetTagCertRevokeRequest request = new AssetTagCertRevokeRequest();
                request.setSha1OfAssetCert(obj.getSha1().toByteArray());
                Global.mtwilson().revokeAssetTagCertificate(request);
                log.info("Certificate with id {} has been revoked successfully.");
            }

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate revocation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } 
        
    }
    
}
