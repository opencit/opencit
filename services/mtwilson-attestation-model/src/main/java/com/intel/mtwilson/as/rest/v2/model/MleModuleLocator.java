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
public class MleModuleLocator implements Locator<MleModule> {

    @PathParam("mle_id")
    public UUID mleUuid;
    @PathParam("id")
    public UUID id;
    
    @Override
    public void copyTo(MleModule item) {
        if( mleUuid != null ) {
            item.setMleUuid(mleUuid.toString());
        }  
        if( id != null ) {
            item.setId(id);
        }
    }
    
}
