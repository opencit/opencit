/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class TlsPolicyResultMapper implements ResultSetMapper<TlsPolicyRecord> {

    @Override
    public TlsPolicyRecord map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        TlsPolicyRecord tlsPolicyRecord = new TlsPolicyRecord();
        tlsPolicyRecord.setId(UUID.valueOf(rs.getString("id")));
        tlsPolicyRecord.setName(rs.getString("name"));
        tlsPolicyRecord.setPrivate(rs.getBoolean("private"));
        tlsPolicyRecord.setContentType(rs.getString("content_type"));
        tlsPolicyRecord.setContent(rs.getBytes("content"));
        tlsPolicyRecord.setComment(rs.getString("comment"));
        return tlsPolicyRecord;
    }
    
}
