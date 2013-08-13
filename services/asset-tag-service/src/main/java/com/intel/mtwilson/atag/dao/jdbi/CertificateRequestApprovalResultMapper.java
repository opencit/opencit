/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.JdbcUtil;
import com.intel.mtwilson.atag.model.CertificateRequestApproval;
import java.sql.ResultSet;
import java.sql.SQLException;
//import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX TODO  Probably don't need this  now that certificates are posted directly as an approval. 
 * @deprecated
 * 
 * @author jbuhacoff
 */
public class CertificateRequestApprovalResultMapper implements ResultSetMapper<CertificateRequestApproval> {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CertificateRequestApproval map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        log.debug("mapping row {}", i);
        JdbcUtil.describeResultSet(rs);
        CertificateRequestApproval approval = new CertificateRequestApproval(rs.getLong("id"), rs.getLong("certificateRequestId"), rs.getBoolean("approved"), rs.getString("authorityName"));
        return approval;
    }
}
