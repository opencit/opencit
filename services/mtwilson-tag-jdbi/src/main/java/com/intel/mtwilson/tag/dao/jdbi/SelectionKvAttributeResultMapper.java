/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.dao.JdbcUtil;
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
//public class SelectionKvAttributeResultMapper implements ResultSetMapper<SelectionTagValue> {
//    private Logger log = LoggerFactory.getLogger(getClass());
//
////    @Override
////    public SelectionTagValue map(int i, ResultSet rs, StatementContext sc) throws SQLException {
////        log.debug("mapping row {}", i);
////        JdbcUtil.describeResultSet(rs);
////        SelectionTagValue crtv = new SelectionTagValue(rs.getLong("id"), rs.getLong("selectionId"), rs.getLong("tagId"), rs.getLong("tagValueId"));
////        if( rs.getMetaData().getColumnCount() > 4 ) { // 4 is id, certificate request id, tag id, tag value id;  7 adds tag name, tag oid, and tag value
////            crtv.setTagName(rs.getString("name"));
////            crtv.setTagOid(rs.getString("oid"));
////            crtv.setTagValue(rs.getString("value"));
////            crtv.setTagUuid(UUID.valueOf(rs.getString("uuid")));
////        }
////        return crtv;
////    }
//}
