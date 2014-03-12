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
public class SelectionKvAttributeLocator implements Locator<SelectionKvAttribute> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(SelectionKvAttribute item) {
        item.setId(id);
    }
    
}
