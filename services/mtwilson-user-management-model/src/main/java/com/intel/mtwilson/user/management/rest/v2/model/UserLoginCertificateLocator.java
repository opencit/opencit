/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.repository.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class UserLoginCertificateLocator implements Locator<UserLoginCertificate> {

    @PathParam("id")
    public UUID id;
    @PathParam("user_id")
    public UUID userId;

    @Override
    public void copyTo(UserLoginCertificate item) {
        if( id != null ) {
            item.setId(id);
        }
        if( userId != null ) {
            item.setUserId(userId);
        }
    }
    
}
