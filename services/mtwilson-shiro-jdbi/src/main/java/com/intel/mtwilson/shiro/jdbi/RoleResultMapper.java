/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.Role;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class RoleResultMapper implements ResultSetMapper<Role> {

    @Override
    public Role map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        Role role = new Role();
        role.setId(UUID.valueOf(rs.getString("id")));
//        role.setId(UUID.valueOf(rs.getBytes("id"))); // would work for mysql if using binary(16) for uuid field
//        role.setId(UUID.valueOf((java.util.UUID)rs.getObject("id"))); // works for postgresql  when using uuid field
        role.setRoleName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        return role;
    }
    
}
