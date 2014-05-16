/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginHmac;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  hmac_key bytea NOT NULL,
  protection character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
 * 
 * @author jbuhacoff
 */
public class UserLoginHmacResultMapper implements ResultSetMapper<UserLoginHmac> {

    @Override
    public UserLoginHmac map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        UserLoginHmac userLoginHmac = new UserLoginHmac();
//        role.setId(UUID.valueOf(rs.getBytes("id"))); // would work for mysql if using binary(16) for uuid field
        userLoginHmac.setId(UUID.valueOf(rs.getString("id"))); // works for postgresql  when using uuid field
//        role.setUserId(UUID.valueOf(rs.getBytes("user_id"))); // would work for mysql if using binary(16) for uuid field
        userLoginHmac.setUserId(UUID.valueOf(rs.getString("user_id"))); // works for postgresql  when using uuid field
        userLoginHmac.setHmacKey(rs.getBytes("hmac_key"));
        userLoginHmac.setProtection(rs.getString("protection"));
        userLoginHmac.setExpires(rs.getTimestamp("expires"));
        userLoginHmac.setEnabled(rs.getBoolean("enabled"));
        return userLoginHmac;
    }
    
}
