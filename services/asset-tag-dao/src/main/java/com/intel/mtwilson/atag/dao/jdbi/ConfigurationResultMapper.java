/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.Configuration;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import java.io.IOException;
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
        if( rs.getString("content") != null ) {
            try {
                configuration.setXmlContent(rs.getString("content"));
            }
            catch(IOException e) {
                throw new SQLException("Cannot parse configuration content", e);
            }
        }
        return configuration;
    }
    
}
