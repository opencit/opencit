/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;
import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author ssbangal
 */
public class CaCertificateLocator implements Locator<CaCertificate>{
    @PathParam("id")
    public String id;

    @Override
    public void copyTo(CaCertificate item) {
        item.setId(UUID.valueOf(id));
    }
    
}
