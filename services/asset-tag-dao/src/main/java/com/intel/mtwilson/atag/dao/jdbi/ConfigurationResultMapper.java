/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.Configuration;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class ConfigurationResultMapper implements ResultSetMapper<Configuration> {

    @Override
    public Configuration map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
        UUID uuid = UUID.valueOf(rs.getString("uuid")); // use this when uuid is a char type in database
        Configuration configuration = new Configuration();
        configuration.setId(rs.getLong("id"));
        configuration.setUuid(uuid);
        configuration.setName(rs.getString("name"));
        configuration.setContent(rs.getString("content"));
        try {
            configuration.setContentType(Configuration.ContentType.valueOf(rs.getString("contentType")));
        }
        catch(Exception e) {
            configuration.setContentType(Configuration.ContentType.TEXT);
        }
        return configuration;
    }
    
}
