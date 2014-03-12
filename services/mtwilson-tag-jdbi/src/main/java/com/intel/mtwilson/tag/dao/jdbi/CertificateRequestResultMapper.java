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
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
//        UUID uuid = UUID.valueOf(rs.getString("uuid")); // use this when uuid is a char type in database
        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setSubject(rs.getString("subject"));
        certificateRequest.setStatus(rs.getString("status"));
        certificateRequest.setCertificateId(UUID.valueOf(rs.getString("certificateId")));
        return certificateRequest;
    }
    
}
