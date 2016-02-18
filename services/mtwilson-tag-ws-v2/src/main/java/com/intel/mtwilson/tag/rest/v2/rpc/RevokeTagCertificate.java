/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateLocator;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 * This would be called from the "revoke" link next to each certificate in the
 * UI.
 * 
 * @author ssbangal
 */
@RPC("revoke-tag-certificate")
@JacksonXmlRootElement(localName="revoke_tag_certificate")
public class RevokeTagCertificate implements Runnable{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RevokeTagCertificate.class);

       
    private UUID certificateId;

    public UUID getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(UUID certificateId) {
        this.certificateId = certificateId;
    }
    
    @Override
    @RequiresPermissions("tag_certificates:delete")         
    public void run() {
        log.debug("RPC: RevokeTagCertificate - Got request to revocation of certificate with ID {}.", certificateId);        
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
            CertificateLocator locator = new CertificateLocator();
            locator.id = certificateId;
            
            Certificate obj = dao.findById(certificateId);
            if (obj != null) 
            {
                log.debug("RPC: RevokeTagCertificate - Sha1 of the certificate about to be revoked is {}.", obj.getSha1());
                dao.updateRevoked(certificateId, true);                
                AssetTagCertRevokeRequest request = new AssetTagCertRevokeRequest();
                request.setSha1OfAssetCert(obj.getSha1().toByteArray());
                Global.mtwilson().revokeAssetTagCertificate(request);
                log.info("RPC: RevokeTagCertificate - Certificate with id {} has been revoked successfully.");
            } else {
                log.error("RPC: RevokeTagCertificate - Certificate with id {} does not exist.", certificateId);
                throw new RepositoryInvalidInputException(locator);
            }

        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("RPC: RevokeTagCertificate - Error during certificate revocation.", ex);
            throw new RepositoryException(ex);
        } 
        
    }
    
}
