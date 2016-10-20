/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
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
 * The "import" link next to each certificate in the UI calls this RPC
 * 
 * @author ssbangal
 */
//@RPC("mtwilson-import-tag-certificate")
@JacksonXmlRootElement(localName="mtwilson_import_tag_certificate")
public class MtWilsonImportTagCertificate { //implements Runnable{
//    
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MtWilsonImportTagCertificate.class);
//
//       
//    private UUID certificateId;
//
//    public UUID getCertificateId() {
//        return certificateId;
//    }
//
//    public void setCertificateId(UUID certificateId) {
//        this.certificateId = certificateId;
//    }
//    
//    @Override
//    @RequiresPermissions("tag_certificates:import")         
//    public void run() {
//        log.debug("RPC:MtWilsonImportTagCertificate - Got request to deploy certificate with ID {}.", certificateId);  
//        CertificateLocator locator = new CertificateLocator();
//        locator.id = certificateId;
//        
//        try (CertificateDAO dao = TagJdbi.certificateDao()) {
//        
//            Certificate obj = dao.findById(certificateId);
//            if (obj != null) 
//            {
//                log.debug("RPC:MtWilsonImportTagCertificate - Sha1 of the certificate about to be deployed is {}.", obj.getSha1());
//                AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
//                request.setCertificate(obj.getCertificate());
//                Global.mtwilson().importAssetTagCertificate(request);
//                log.info("RPC:MtWilsonImportTagCertificate - Certificate with id {} has been deployed successfully.");
//            } else {
//                log.error("RPC:MtWilsonImportTagCertificate - Specified Certificate with id {} is not valid.", certificateId);
//                throw new RepositoryInvalidInputException(locator);
//            }
//
//        } catch (RepositoryException re) {
//            throw re;            
//        } catch (Exception ex) {
//            log.error("RPC:MtWilsonImportTagCertificate - Error during certificate deployment.", ex);
//            throw new RepositoryException(ex);
//        } 
//        
//    }
    
}
