/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.UserLoginPassword;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.user.management.rest.v2.model.Status;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
 * 
 * @author jbuhacoff
 */
public class UserLoginPasswordResultMapper implements ResultSetMapper<UserLoginPassword> {

    @Override
    public UserLoginPassword map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        UserLoginPassword userLoginPassword = new UserLoginPassword();
//        role.setId(UUID.valueOf(rs.getBytes("id"))); // would work for mysql if using binary(16) for uuid field
        userLoginPassword.setId(UUID.valueOf(rs.getString("id"))); // works for postgresql  when using uuid field
//        role.setUserId(UUID.valueOf(rs.getBytes("user_id"))); // would work for mysql if using binary(16) for uuid field
        userLoginPassword.setUserId(UUID.valueOf(rs.getString("user_id"))); // works for postgresql  when using uuid field
        userLoginPassword.setPasswordHash(rs.getBytes("password_hash"));
        userLoginPassword.setSalt(rs.getBytes("salt"));
        userLoginPassword.setIterations(rs.getInt("iterations"));
        userLoginPassword.setAlgorithm(rs.getString("algorithm"));
        userLoginPassword.setExpires(rs.getTimestamp("expires"));
        userLoginPassword.setEnabled(rs.getBoolean("enabled"));
        userLoginPassword.setStatus(Status.valueOf(rs.getString("status")));
        userLoginPassword.setComment(rs.getString("comment"));
        return userLoginPassword;
    }
    
}
