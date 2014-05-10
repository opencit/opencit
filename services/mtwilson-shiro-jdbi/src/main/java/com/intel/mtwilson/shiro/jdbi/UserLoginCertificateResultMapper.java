/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.user.management.rest.v2.model.Status;
import com.intel.mtwilson.user.management.rest.v2.model.UserLoginCertificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
  id uuid DEFAULT NULL,
  user_id uuid DEFAULT NULL,
  certificate bytea NOT NULL,
  sha1_hash bytea NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'Pending',
  comment text,
 * 
 * @author jbuhacoff
 */
public class UserLoginCertificateResultMapper implements ResultSetMapper<UserLoginCertificate> {

    @Override
    public UserLoginCertificate map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
        userLoginCertificate.setId(UUID.valueOf(rs.getString("id")));
//        role.setId(UUID.valueOf(rs.getBytes("id"))); // would work for mysql if using binary(16) for uuid field
//        userLoginCertificate.setId(UUID.valueOf((java.util.UUID)rs.getObject("id"))); // works for postgresql  when using uuid field
//        role.setUserId(UUID.valueOf(rs.getBytes("user_id"))); // would work for mysql if using binary(16) for uuid field
//        userLoginCertificate.setUserId(UUID.valueOf((java.util.UUID)rs.getObject("user_id"))); // works for postgresql  when using uuid field
        userLoginCertificate.setUserId(UUID.valueOf(rs.getString("user_id")));
        userLoginCertificate.setCertificate(rs.getBytes("certificate"));
        userLoginCertificate.setSha1Hash(rs.getBytes("sha1_hash"));
        userLoginCertificate.setSha256Hash(rs.getBytes("sha256_hash"));
        userLoginCertificate.setExpires(rs.getTimestamp("expires"));
        userLoginCertificate.setEnabled(rs.getBoolean("enabled"));
        userLoginCertificate.setStatus(Status.valueOf(rs.getString("status")));
        userLoginCertificate.setComment(rs.getString("comment"));
        return userLoginCertificate;
    }
    
}
