/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
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
        item.setId(id);
        if( subject != null && !subject.isEmpty() && item.getSubject() == null ) {
            item.setSubject(subject);
        }
    }
    
}
