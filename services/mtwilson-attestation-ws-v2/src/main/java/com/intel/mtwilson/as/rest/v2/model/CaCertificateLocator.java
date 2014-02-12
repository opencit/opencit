/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class CaCertificateLocator implements Locator<CaCertificate>{
    @PathParam("id")
    public String id;

    @Override
    public void copyTo(CaCertificate item) {
        item.setName(id);
    }
    
}
