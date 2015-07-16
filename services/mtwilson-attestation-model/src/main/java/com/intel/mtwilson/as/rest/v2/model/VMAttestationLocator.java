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
public class VMAttestationLocator implements Locator<VMAttestation> {

    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(VMAttestation item) {
        if( id != null ) {
            item.setId(id);
        }
    }
    
}
