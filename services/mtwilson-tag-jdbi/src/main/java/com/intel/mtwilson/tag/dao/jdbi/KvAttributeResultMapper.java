/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.KvAttribute;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class KvAttributeResultMapper implements ResultSetMapper<KvAttribute> {

    @Override
    public KvAttribute map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
        UUID id = UUID.valueOf(rs.getString("id")); // use this when uuid is a char type in database
        KvAttribute obj = new KvAttribute();
        obj.setId(id);
        obj.setName(rs.getString("name"));
        obj.setValue(rs.getString("value"));
        return obj;
    }
    
}
