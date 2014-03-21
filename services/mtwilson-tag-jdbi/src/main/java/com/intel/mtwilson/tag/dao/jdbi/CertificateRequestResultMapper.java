/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.CertificateRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class CertificateRequestResultMapper implements ResultSetMapper<CertificateRequest> {

    @Override
    public CertificateRequest map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setId(UUID.valueOf(rs.getString("id")));
        certificateRequest.setSubject(rs.getString("subject"));
        certificateRequest.setContentType(rs.getString("contentType"));
        certificateRequest.setStatus(rs.getString("status"));
        certificateRequest.setContent(rs.getBytes("content"));
        return certificateRequest;
    }
    
}
