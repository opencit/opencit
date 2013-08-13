/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.TagValue;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class TagValueResultMapper implements ResultSetMapper<TagValue> {

    @Override
    public TagValue map(int row, ResultSet rs, StatementContext sc) throws SQLException {
        // figure out if the result set has a uuid column
        boolean hasUuid = false;
        int columns = rs.getMetaData().getColumnCount();
        for(int i=1; i<=columns; i++) {
            if( rs.getMetaData().getColumnName(i).toLowerCase().equals("uuid") ) {
                hasUuid = true;
                break;
            }
        }
        // if uuid is present then provide it to the constructor.  we assume everything is present, for now.
        if( hasUuid ) {
//            UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // when UUID is binary
            UUID uuid = UUID.valueOf(rs.getString("uuid"));
            return new TagValue(rs.getLong("id"), uuid, rs.getLong("tagId"), rs.getString("value"));
        }
        else {
            return new TagValue(rs.getLong("id"), rs.getLong("tagId"), rs.getString("value"));            
        }
    }
    
}
