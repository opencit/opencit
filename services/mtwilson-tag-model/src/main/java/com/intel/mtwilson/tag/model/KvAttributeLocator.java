/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class KvAttributeLocator implements Locator<KvAttribute> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(KvAttribute item) {
        item.setId(id);
    }
    
}
