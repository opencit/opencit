/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * When an external CA is polling for certificate requests and then wants
 * to download a specific certificate request by UUID, it would call
 * /tag-certificate-requests/{id}  
 * 
 * When a provisioning agent sends a certificate request to the ProvisionTagCertificate
 * RPC it sends to a URL like /tag-certificate-requests-rpc/provision?subject=uuid
 * with the selection XML in the POST body.
 * 
 * For example, the UI would do:
 * POST /tag-certificate-requests-rpc/provision?subject=uuid
 * Content-Type: application/json
 * 
 * { selections: [ { id: "uuid" } ] }
 * 
 * Alternatively, the UI could send the selection name instead of id:
 * 
 * { selections: [ { name: "California Finance" } ] }
 * 
 * 
 * @author ssbangal
 */
public class CertificateRequestLocator implements Locator<CertificateRequest>{

    @PathParam("id")
    public UUID id;
    
    @QueryParam("subject")
    public String subject;

    @Override
    public void copyTo(CertificateRequest item) {
        if (id != null) {
            item.setId(id);
        }
        if( subject != null && !subject.isEmpty() && item.getSubject() == null ) {
            item.setSubject(subject);
        }
    }
    
}
