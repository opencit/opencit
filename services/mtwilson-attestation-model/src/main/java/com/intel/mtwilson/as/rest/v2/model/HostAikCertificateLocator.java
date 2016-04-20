/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class HostAikCertificateLocator implements Locator<HostAikCertificate> {
    
    @PathParam("host_id")
    public UUID hostUuid;
    @PathParam("id")
    public String aikSha1;
    
    @Override
    public void copyTo(HostAikCertificate item) {
        item.setHostUuid(hostUuid.toString());
        item.setAikSha1(aikSha1);
    }
    
}
