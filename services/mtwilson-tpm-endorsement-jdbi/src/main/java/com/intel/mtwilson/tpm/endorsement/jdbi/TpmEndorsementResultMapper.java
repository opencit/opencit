/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tpm.endorsement.jdbi;

import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class TpmEndorsementResultMapper implements ResultSetMapper<TpmEndorsement> {

    @Override
    public TpmEndorsement map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        TpmEndorsement tlsPolicyRecord = new TpmEndorsement();
        tlsPolicyRecord.setId(UUID.valueOf(rs.getString("id")));
        tlsPolicyRecord.setHardwareUuid(rs.getString("hardware_uuid"));
        tlsPolicyRecord.setIssuer(rs.getString("issuer"));
        tlsPolicyRecord.setRevoked(rs.getBoolean("revoked"));
        tlsPolicyRecord.setCertificate(rs.getBytes("certificate"));
        tlsPolicyRecord.setComment(rs.getString("comment"));
        return tlsPolicyRecord;
    }
    
}
