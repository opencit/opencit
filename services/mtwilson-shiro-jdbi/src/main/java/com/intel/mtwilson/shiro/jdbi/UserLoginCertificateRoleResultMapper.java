/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificateRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class UserLoginCertificateRoleResultMapper implements ResultSetMapper<UserLoginCertificateRole> {

    @Override
    public UserLoginCertificateRole map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        UserLoginCertificateRole role = new UserLoginCertificateRole();
        role.setLoginCertificateId(UUID.valueOf(rs.getString("login_certificate_id")));
        role.setRoleId(UUID.valueOf(rs.getString("role_id")));
        return role;
    }
    
}
