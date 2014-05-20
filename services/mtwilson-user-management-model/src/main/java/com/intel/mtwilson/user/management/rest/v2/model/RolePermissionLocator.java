/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class RolePermissionLocator implements Locator<RolePermission> {

    @PathParam("role_id")
    public UUID roleId;

    @Override
    public void copyTo(RolePermission item) {
        item.setRoleId(roleId);
    }
    
}
