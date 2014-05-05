/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.jdbi.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class UserLoginHmacLocator implements Locator<UserLoginHmac> {

    @PathParam("id")
    public UUID id;

    @Override
    public void copyTo(UserLoginHmac item) {
        item.setId(id);
    }
    
}
