/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author ssbangal
 */
public class MleModuleLocator implements Locator<MleModule> {

    @PathParam("mle_id")
    public UUID mleUuid;
    @QueryParam("id")
    public UUID moduleUuid;
    
    @Override
    public void copyTo(MleModule item) {
        item.setMleUuid(mleUuid.toString());
        item.setId(moduleUuid);
    }
    
}
