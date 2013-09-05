/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.mtwilson.atag.model.CertificateSearchCriteria;
import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.dao.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.*;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.iso8601.Iso8601Date;
import com.intel.mtwilson.atag.Global;
import com.intel.mtwilson.atag.X509AttrBuilder;
import com.intel.mtwilson.atag.dao.jdbi.*;
import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.model.Selection;
import com.intel.mtwilson.atag.model.SelectionTagValue;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class CertificateListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateDAO certificateDao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            certificateDao = Derby.certificateDao();
            jooq = Derby.jooq();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        if( certificateDao != null ) {
            certificateDao.close();
        }
//        if( certificateRequestTagValueDao != null ) { certificateRequestTagValueDao.close(); }
        super.doRelease();
    }

    /**
     * Use this to import certificates (for example if an external CA is downloading requests and uploading certificates)
     */
//    @Post("json:json")
    public Certificate insertCertificate(Certificate certificate) throws SQLException {
//        log.debug("insertCertificate for subject: {}", certificate.getSubject());
        certificate.setUuid(new UUID());
        // at this point we have a request for a subject (host uuid) and a specific selection of tags for that subject
        long certificateId = certificateDao.insert(certificate.getUuid(), certificate.getCertificate(), certificate.getSha256().toHexString(), certificate.getPcrEvent().toHexString(), certificate.getSubject(), certificate.getIssuer(), certificate.getNotBefore(), certificate.getNotAfter());
        certificate.setId(certificateId); // XXX of no use to the client, maybe remove this

        return certificate;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept Certificate[] certificates as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.acertificate.model.Certificate out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.acertificate.model.Certificate
     *
     * @param certificates
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public Certificate[] insertCertificates(Certificate[] certificates) throws SQLException {
        Certificate[] results = new Certificate[certificates.length];
        for (int i = 0; i < certificates.length; i++) {
            results[i] = insertCertificate(certificates[i]);
//            insertCertificate(certificates[i]);
        }
        return results;
//        return certificates;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because certificate values are in a separate table, if the client wants to find certificates that
     * have specific values, we need to search for those values first. 
     * 
     * find certificate where certificate.id = certificate_value.certificateId and certificate_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    
    @Get("json")
    public Certificate[] search() throws SQLException {
        CertificateSearchCriteria query = new CertificateSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.subjectEqualTo = getQuery().getFirstValue("subjectEqualTo");
        query.subjectContains = getQuery().getFirstValue("subjectContains");
        query.issuerEqualTo = getQuery().getFirstValue("issuerEqualTo");
        query.issuerContains = getQuery().getFirstValue("issuerContains");
        query.statusEqualTo = getQuery().getFirstValue("statusEqualTo");
        query.sha256 = Sha256Digest.valueOf(getQuery().getFirstValue("sha256")); // will return null if not a valid digest
        query.pcrEvent = Sha1Digest.valueOf(getQuery().getFirstValue("pcrEvent")); // will return null if not a valid digest
        if( getQuery().getFirstValue("validBefore") != null && !getQuery().getFirstValue("validBefore").isEmpty() ) {
            query.validBefore = Iso8601Date.valueOf(getQuery().getFirstValue("validBefore")).toDate();
        }
        if( getQuery().getFirstValue("validAfter") != null && !getQuery().getFirstValue("validAfter").isEmpty() ) {
        query.validAfter = Iso8601Date.valueOf(getQuery().getFirstValue("validAfter")).toDate();
            
        }
        if( getQuery().getFirstValue("validOn") != null && !getQuery().getFirstValue("validOn").isEmpty() ) {
        query.validOn = Iso8601Date.valueOf(getQuery().getFirstValue("validOn")).toDate();
            
        }
        if( getQuery().getFirstValue("revoked") != null && !getQuery().getFirstValue("revoked").isEmpty() ) {
            query.revoked = Boolean.valueOf(getQuery().getFirstValue("revoked"));
        }
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select()
                .from(CERTIFICATE) // .join(CERTIFICATE_TAG_VALUE)
                //.on(CERTIFICATE_TAG_VALUE.CERTIFICATEREQUESTID.equal(CERTIFICATE.ID)))
                .getQuery();
        if( query.id != null ) {
            sql.addConditions(CERTIFICATE.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.subjectEqualTo != null  && query.subjectEqualTo.length() > 0 ) {
            sql.addConditions(CERTIFICATE.SUBJECT.equal(query.subjectEqualTo));
        }
        if( query.subjectContains != null  && query.subjectContains.length() > 0  ) {
            sql.addConditions(CERTIFICATE.SUBJECT.equal(query.subjectContains));
        }
        if( query.issuerEqualTo != null  && query.issuerEqualTo.length() > 0 ) {
            sql.addConditions(CERTIFICATE.ISSUER.equal(query.issuerEqualTo));
        }
        if( query.issuerContains != null  && query.issuerContains.length() > 0  ) {
            sql.addConditions(CERTIFICATE.ISSUER.equal(query.issuerContains));
        }
        if( query.sha256 != null  ) {
            sql.addConditions(CERTIFICATE.SHA256.equal(query.sha256.toHexString()));
        }
        if( query.pcrEvent != null  ) {
            sql.addConditions(CERTIFICATE.PCREVENT.equal(query.pcrEvent.toHexString()));
        }
        if( query.validOn != null ) {
            sql.addConditions(CERTIFICATE.NOTBEFORE.lessOrEqual(new Timestamp(query.validOn.getTime())));
            sql.addConditions(CERTIFICATE.NOTAFTER.greaterOrEqual(new Timestamp(query.validOn.getTime())));
        }
        if( query.validBefore != null ) {
            sql.addConditions(CERTIFICATE.NOTAFTER.greaterOrEqual(new Timestamp(query.validBefore.getTime())));
        }
        if( query.validAfter != null ) {
            sql.addConditions(CERTIFICATE.NOTBEFORE.lessOrEqual(new Timestamp(query.validAfter.getTime())));
        }
        if( query.revoked != null   ) {
            sql.addConditions(CERTIFICATE.REVOKED.equal(query.revoked));
        }
        sql.addOrderBy(CERTIFICATE.ID);
        Result<Record> result = sql.fetch();
        Certificate[] certificates = new Certificate[result.size()];
        log.debug("Got {} records", certificates.length);
        int i = -1; //  index into the target array certificates
        long c = -1; // id of the current certificate request object built, used to detect when it's time to build the next one
        for(Record r : result) {
            if( r.getValue(CERTIFICATE.ID) != c ) {
                i++;
                c = r.getValue(CERTIFICATE.ID);
                try {
                    certificates[i] = Certificate.valueOf((byte[])r.getValue(CERTIFICATE.CERTIFICATE_));  // unlike other table queries, here we can get all the info from the certificate itself... except for the revoked flag
                    certificates[i].setId(r.getValue(CERTIFICATE.ID));
                    certificates[i].setUuid(UUID.valueOf(r.getValue(CERTIFICATE.UUID)));
                    if( r.getValue(CERTIFICATE.REVOKED) != null ) {
                        certificates[i].setRevoked(r.getValue(CERTIFICATE.REVOKED));
                    }
                }
                catch(Exception e) {
                    log.error("Cannot load certificate #{}", r.getValue(CERTIFICATE.ID));
                }
            }
        }
        sql.close();
        return certificates;
    }
}
