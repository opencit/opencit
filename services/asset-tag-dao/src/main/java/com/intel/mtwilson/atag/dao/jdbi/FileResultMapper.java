/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class FileResultMapper implements ResultSetMapper<File> {

    @Override
    public File map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
        UUID uuid = UUID.valueOf(rs.getString("uuid")); // use this when uuid is a char type in database
        File file = new File();
        file.setId(rs.getLong("id"));
        file.setUuid(uuid);
        file.setName(rs.getString("name"));
        file.setContent(rs.getBytes("content"));
        file.setContentType(rs.getString("contentType")); 
        return file;
    }
    
}
