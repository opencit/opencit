/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class ConfigurationLocator implements Locator<Configuration> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(Configuration item) {
        item.setId(id);
    }
    
}
