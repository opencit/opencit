/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyLocator implements Locator<HostTlsPolicy> {

    @PathParam("host_id")
    public UUID hostUuid;

    @Override
    public void copyTo(HostTlsPolicy item) {
        if( hostUuid != null ) {
            item.setHostUuid(hostUuid.toString());
        }        
    }
    
}
