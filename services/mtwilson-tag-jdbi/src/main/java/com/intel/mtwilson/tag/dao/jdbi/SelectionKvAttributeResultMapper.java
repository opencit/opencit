/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.JdbcUtil;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import java.sql.ResultSet;
import java.sql.SQLException;
//import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jbuhacoff
 */
public class SelectionKvAttributeResultMapper implements ResultSetMapper<SelectionKvAttribute> {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public SelectionKvAttribute map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        log.debug("mapping row {}", i);
        if( log.isDebugEnabled() ) { JdbcUtil.describeResultSet(rs); }
        SelectionKvAttribute result = new SelectionKvAttribute();
        result.setId(UUID.valueOf(rs.getString("id")));
        result.setKvAttributeId(UUID.valueOf(rs.getString("kvAttributeId")));
        result.setSelectionId(UUID.valueOf(rs.getString("selectionId")));
        if( rs.getMetaData().getColumnCount() > 3 ) { // 
            result.setKvAttributeName(rs.getString("name"));
            result.setKvAttributeValue(rs.getString("value"));
//            result.setSelectionName(rs.getString(""))
        }
        return result;
    }
}
