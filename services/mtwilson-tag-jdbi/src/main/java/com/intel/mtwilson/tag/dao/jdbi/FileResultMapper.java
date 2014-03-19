/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.File;
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
        File file = new File();
        file.setId(UUID.valueOf(rs.getString("id")));
        file.setName(rs.getString("name"));
        file.setContent(rs.getBytes("content"));
        file.setContentType(rs.getString("contentType")); 
        return file;
    }
    
}
