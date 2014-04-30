/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.tag.model.CertificateRequest;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class CertificateResultMapper implements ResultSetMapper<Certificate> {

    @Override
    public Certificate map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
        byte[] content = rs.getBytes("certificate");
        Sha1Digest sha1 = Sha1Digest.valueOfHex(rs.getString("sha1"));
        Sha256Digest sha256 = Sha256Digest.valueOfHex(rs.getString("sha256"));
//        Sha1Digest pcrEvent = Sha1Digest.valueOfHex(rs.getString("pcrEvent"));
        Certificate certificate = new Certificate();
        certificate.setId(UUID.valueOf(rs.getString("id")));
        certificate.setCertificate(content);
        certificate.setSha1(sha1);
        certificate.setSha256(sha256);
//        certificate.setPcrEvent(pcrEvent);
        certificate.setSubject(rs.getString("subject"));
        certificate.setIssuer(rs.getString("issuer"));
        certificate.setNotBefore(rs.getTimestamp("notBefore"));
        certificate.setNotAfter(rs.getTimestamp("notAfter"));
        certificate.setRevoked(rs.getBoolean("revoked"));
        return certificate;
    }
    
}
