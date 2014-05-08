/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.security.rest.v2.model.UserKeystore;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class UserKeystoreResultMapper implements ResultSetMapper<UserKeystore> {

    @Override
    public UserKeystore map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("id")); // use this when uuid is a binary(mysql) or uuid(postgresql) type in database
//        UUID uuid = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        UserKeystore userKeystore = new UserKeystore();
        userKeystore.setId(UUID.valueOf(rs.getString("id")));
//        role.setId(UUID.valueOf(rs.getBytes("id"))); // would work for mysql if using binary(16) for uuid field
//        userKeystore.setId(UUID.valueOf((java.util.UUID)rs.getObject("id"))); // works for postgresql  when using uuid field
//        role.setUserId(UUID.valueOf(rs.getBytes("user_id"))); // would work for mysql if using binary(16) for uuid field
//        userKeystore.setUserId(UUID.valueOf((java.util.UUID)rs.getObject("user_id"))); // works for postgresql  when using uuid field
        userKeystore.setUserId(UUID.valueOf(rs.getString("user_id")));
        userKeystore.setKeystore(rs.getBytes("keystore"));
        userKeystore.setKeystoreFormat(rs.getString("keystore_format"));
        userKeystore.setComment(rs.getString("comment"));
        return userKeystore;
    }
    
}
