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
public class HostAikLocator implements Locator<HostAik> {

    @PathParam("host_id")
    public UUID hostUuid;
    @PathParam("aik")
    public String aik;
    
    @Override
    public void copyTo(HostAik item) {
        item.setHostUuid(hostUuid.toString());
        item.setAikSha1(aik);
    }
    
}
