/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.RolePermission;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class RolePermissionResultMapper implements ResultSetMapper<RolePermission> {
    
    @Override
    public RolePermission map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(UUID.valueOf(rs.getString("role_id")));
//        rolePermission.setRoleId(UUID.valueOf(rs.getBytes("role_id"))); // would work for mysql if using binary(16) for uuid field
//        rolePermission.setRoleId(UUID.valueOf((java.util.UUID)rs.getObject("role_id"))); // works for postgresql  when using uuid field
        rolePermission.setPermitDomain(rs.getString("permit_domain"));
        rolePermission.setPermitAction(rs.getString("permit_action"));
        rolePermission.setPermitSelection(rs.getString("permit_selection"));
        return rolePermission;
    }
    
}
