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
public class MlePcrLocator implements Locator<MlePcr>{

    @PathParam("mle_id") // change id to mle_id
    public UUID mleUuid;
    @PathParam("id")
    public String pcrIndex;
    
    @Override
    public void copyTo(MlePcr item) {
        if( mleUuid != null ) {
            item.setMleUuid(mleUuid.toString());
        }  
        if (pcrIndex != null && !pcrIndex.isEmpty()) {
            item.setPcrIndex(pcrIndex);
        }
    }
    
}
