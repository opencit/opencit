/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.dao.JdbcUtil;
import com.intel.mtwilson.atag.model.CertificateRequestTagValue;
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
public class CertificateRequestTagValueResultMapper implements ResultSetMapper<CertificateRequestTagValue> {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CertificateRequestTagValue map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        log.debug("mapping row {}", i);
        JdbcUtil.describeResultSet(rs);
        CertificateRequestTagValue crtv = new CertificateRequestTagValue(rs.getLong("id"), rs.getLong("certificateRequestId"), rs.getLong("tagId"), rs.getLong("tagValueId"));
        if( rs.getMetaData().getColumnCount() > 4 ) { // 4 is id, certificate request id, tag id, tag value id;  7 adds tag name, tag oid, and tag value
            crtv.setName(rs.getString("name"));
            crtv.setOid(rs.getString("oid"));
            crtv.setValue(rs.getString("value"));
        }
        return crtv;
    }
}
