/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class HostTlsCertificateLocator implements Locator<HostTlsCertificate> {
    
    @PathParam("id")
    public UUID id;
    @PathParam("sha1")
    public String sha1;


    @Override
    public void copyTo(HostTlsCertificate item) {
        item.setHostUuid(id.toString());
        item.setSha1(sha1);
    }
    
}
