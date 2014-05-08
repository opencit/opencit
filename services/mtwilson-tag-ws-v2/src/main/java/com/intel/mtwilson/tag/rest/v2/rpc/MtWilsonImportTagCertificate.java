/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.CertificateDAO;
import com.intel.mtwilson.tag.model.Certificate;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "import" link next to each certificate in the UI calls this RPC
 * 
 * @author ssbangal
 */
@RPC("mtwilson-import-tag-certificate")
@JacksonXmlRootElement(localName="mtwilson_import_tag_certificate")
public class MtWilsonImportTagCertificate implements Runnable{
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MtWilsonImportTagCertificate.class);

       
    private UUID certificateId;

    public UUID getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(UUID certificateId) {
        this.certificateId = certificateId;
    }
    
    @Override
    @RequiresPermissions("tag_certificates:import")         
    public void run() {
        log.debug("Got request to deploy certificate with ID {}.", certificateId);        
        try (CertificateDAO dao = TagJdbi.certificateDao()) {
        
            Certificate obj = dao.findById(certificateId);
            if (obj != null) 
            {
                log.debug("Sha1 of the certificate about to be deployed is {}.", obj.getSha1());
                AssetTagCertCreateRequest request = new AssetTagCertCreateRequest();
                request.setCertificate(obj.getCertificate());
                Global.mtwilson().importAssetTagCertificate(request);
                log.info("Certificate with id {} has been deployed successfully.");
            }

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during certificate deployment.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } 
        
    }
    
}
