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
public class OemLocator implements Locator<Oem>{

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(Oem item) {
        item.setId(id);
    }
    
}
