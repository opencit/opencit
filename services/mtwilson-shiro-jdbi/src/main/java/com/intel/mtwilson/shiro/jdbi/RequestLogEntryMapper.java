/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi;

import com.intel.mtwilson.shiro.RequestLogEntry;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class RequestLogEntryMapper implements ResultSetMapper<RequestLogEntry> {

    @Override
    public RequestLogEntry map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        RequestLogEntry requestLogEntry = new RequestLogEntry();
        requestLogEntry.setInstance(rs.getString("instance"));
        requestLogEntry.setReceived(rs.getTimestamp("received"));
        requestLogEntry.setSource(rs.getString("source"));
        requestLogEntry.setDigest(rs.getString("digest"));
        if( hasColumn(rs, "content") ) {
            requestLogEntry.setContent(rs.getString("content"));
        }
        return requestLogEntry;
    }
    
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int max = meta.getColumnCount();
        boolean found = false;
        for(int i=1; i<=max; i++) {
            if( meta.getColumnName(i).equals(columnName)) {
                found = true;
                break;
            }
        }
        return found;
    }
}
