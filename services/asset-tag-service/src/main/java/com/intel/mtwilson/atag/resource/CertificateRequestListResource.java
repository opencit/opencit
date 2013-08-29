/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.model.CertificateRequestSearchCriteria;
import com.intel.mtwilson.atag.model.CertificateRequestTagValue;
import com.intel.mtwilson.atag.model.CertificateRequest;
import com.intel.mtwilson.atag.model.TagValue;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestTagValueDAO;
import com.intel.mtwilson.atag.dao.jdbi.CertificateRequestDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.*;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;
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
public class CertificateRequestListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private CertificateRequestDAO certificateRequestDao = null;
    private CertificateRequestTagValueDAO certificateRequestTagValueDao = null;
    private TagDAO tagDao = null;
    private TagValueDAO tagValueDao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            certificateRequestDao = Derby.certificateRequestDao();
            certificateRequestTagValueDao = Derby.certificateRequestTagValueDao();
            tagDao = Derby.tagDao();
            tagValueDao = Derby.tagValueDao();
            jooq = Derby.jooq();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        if (certificateRequestDao != null) {
            certificateRequestDao.close();
        }
        if( certificateRequestTagValueDao != null ) { certificateRequestTagValueDao.close(); }
        super.doRelease();
    }

    /**
     * Input certificate requests provide a certificate subject and one or more tags where each tag
     * is a pair of (name,value) or (oid,value).
     * We look up the tagId and tagValueId for each tag and populate them in the CertificateRequestTagValue
     * object. Then we can insert it to the database.
     */
//    @Post("json:json")
    public CertificateRequest insertCertificateRequest(CertificateRequest certificateRequest) throws SQLException {
        log.debug("insertCertificateRequest for subject: {}", certificateRequest.getSubject());
        certificateRequest.setUuid(new UUID());
        long certificateRequestId = certificateRequestDao.insert(certificateRequest.getUuid(), certificateRequest.getSubject());
        certificateRequest.setId(certificateRequestId);
//        CertificateRequest result = new CertificateRequest(certificateRequestId, certificateRequest.getUuid(), certificateRequest.getSubject());
        log.debug("inserted request has id {}", certificateRequestId);
        log.debug("inserted request has uuid {}", certificateRequest.getUuid());
        // now insert all the tag values
        ArrayList<Long> tags = new ArrayList<Long>();
        ArrayList<Long> tagValues = new ArrayList<Long>();
        log.debug("insertCertificateRequest has {} tags", certificateRequest.getTags().size());
        for(CertificateRequestTagValue crtv : certificateRequest.getTags()) {
            // look up tagId and tagValueId for (name,value) or (oid,value) pairs
            if( crtv.getName() != null ) { 
                log.debug("tag name: {}", crtv.getName());
                Tag byName = tagDao.findByName(crtv.getName()); 
                if( byName != null ) {
                    crtv.setTagId(byName.getId());
                }
            }
            else if( crtv.getOid() != null ) { 
                log.debug("tag oid: {}", crtv.getOid());
                Tag byOid = tagDao.findByOid(crtv.getOid()); 
                if( byOid != null ) {
                    crtv.setTagId(byOid.getId());
                }
            }
            log.debug("tag value (required): {}", crtv.getValue());
            TagValue byValue = tagValueDao.findByTagIdAndValueEquals(crtv.getTagId(), crtv.getValue());
            if( byValue != null ) {
                crtv.setTagValueId(byValue.getId());
            }
            // set the tagId and tagValueId we found in the CertificateRequestTagValue record
//            tags.add(crtv.getTagId());
//            tagValues.add(crtv.getTagValueId());
            log.debug("Inserting certificate request tag value: {}", String.format("req id: %d   tag id: %d   tag value id: %d", certificateRequestId, crtv.getTagId(), crtv.getTagValueId()));
            certificateRequestTagValueDao.insert(certificateRequestId, crtv.getTagId(), crtv.getTagValueId());
        }
//        certificateRequestTagValueDao.insert(certificateRequestId, tags, tagValues); // the bulk insert is not working?
        return certificateRequest;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept CertificateRequest[] certificateRequests as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.acertificateRequest.model.CertificateRequest out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.acertificateRequest.model.CertificateRequest
     *
     * @param certificateRequests
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public CertificateRequest[] insertCertificateRequests(CertificateRequest[] certificateRequests) throws SQLException {
        CertificateRequest[] results = new CertificateRequest[certificateRequests.length];
        for (int i = 0; i < certificateRequests.length; i++) {
            results[i] = insertCertificateRequest(certificateRequests[i]);
//            insertCertificateRequest(certificateRequests[i]);
        }
        return results;
//        return certificateRequests;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because certificateRequest values are in a separate table, if the client wants to find certificateRequests that
     * have specific values, we need to search for those values first. 
     * 
     * find certificateRequest where certificateRequest.id = certificateRequest_value.certificateRequestId and certificateRequest_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    
    @Get("json")
    public CertificateRequest[] search() throws SQLException {
        CertificateRequestSearchCriteria query = new CertificateRequestSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.subjectEqualTo = getQuery().getFirstValue("subjectEqualTo");
        query.subjectContains = getQuery().getFirstValue("subjectContains");
        query.tagNameEqualTo = getQuery().getFirstValue("tagNameEqualTo");
        query.tagNameContains = getQuery().getFirstValue("tagNameContains");
        query.tagOidEqualTo = getQuery().getFirstValue("tagOidEqualTo");
        query.tagOidContains = getQuery().getFirstValue("tagOidContains");
        query.tagValueEqualTo = getQuery().getFirstValue("tagValueEqualTo");
        query.tagValueContains = getQuery().getFirstValue("tagValueContains");
        query.statusEqualTo = getQuery().getFirstValue("statusEqualTo");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select()
                .from(CERTIFICATE_REQUEST.join(CERTIFICATE_REQUEST_TAG_VALUE)
                .on(CERTIFICATE_REQUEST_TAG_VALUE.CERTIFICATEREQUESTID.equal(CERTIFICATE_REQUEST.ID))).getQuery();
        if( query.tagValueEqualTo != null || query.tagValueContains != null ) {
            log.debug("Selecting from tag-value");
            SelectQuery valueQuery = jooq.select(CERTIFICATE_REQUEST_TAG_VALUE.ID)
                    .from(CERTIFICATE_REQUEST_TAG_VALUE.join(TAG_VALUE).on(TAG_VALUE.ID.equal(CERTIFICATE_REQUEST_TAG_VALUE.TAGVALUEID)))
                    .getQuery();
            if( query.tagValueEqualTo != null && query.tagValueEqualTo.length() > 0 ) {
                valueQuery.addConditions(TAG_VALUE.VALUE.equal(query.tagValueEqualTo));
            }
            if( query.tagValueContains != null  && query.tagValueContains.length() > 0 ) {
                valueQuery.addConditions(TAG_VALUE.VALUE.contains(query.tagValueContains));
            }
            sql.addConditions(CERTIFICATE_REQUEST_TAG_VALUE.ID.in(valueQuery));
        }
        if( query.tagNameContains != null || query.tagNameEqualTo != null || query.tagOidContains != null || query.tagOidEqualTo != null ) {
            log.debug("Selecting from tag");
            SelectQuery tagQuery = jooq.select(CERTIFICATE_REQUEST_TAG_VALUE.ID)
                    .from(CERTIFICATE_REQUEST_TAG_VALUE.join(TAG).on(TAG.ID.equal(CERTIFICATE_REQUEST_TAG_VALUE.TAGID)))
                    .getQuery();
            if( query.tagNameEqualTo != null  && query.tagNameEqualTo.length() > 0 ) {
                tagQuery.addConditions(TAG.NAME.equal(query.tagNameEqualTo));
            }
            if( query.tagNameContains != null  && query.tagNameContains.length() > 0 ) {
                tagQuery.addConditions(TAG.NAME.contains(query.tagNameContains));
            }
            if( query.tagOidEqualTo != null  && query.tagOidEqualTo.length() > 0 ) {
                tagQuery.addConditions(TAG.OID.equal(query.tagOidEqualTo));
            }
            if( query.tagOidContains != null  && query.tagOidContains.length() > 0 ) {
                tagQuery.addConditions(TAG.OID.contains(query.tagOidContains));
            }
            sql.addConditions(CERTIFICATE_REQUEST_TAG_VALUE.ID.in(tagQuery));       
            // XXX TODO when should we close tagQuery?  it's probably not automatically closed by the "sql" selectquery.  same applies to any other list resources that use a subquery.
        }
        
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(CERTIFICATE_REQUEST.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.subjectEqualTo != null  && query.subjectEqualTo.length() > 0 ) {
            sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(query.subjectEqualTo));
        }
        if( query.subjectContains != null  && query.subjectContains.length() > 0  ) {
            sql.addConditions(CERTIFICATE_REQUEST.SUBJECT.equal(query.subjectContains));
        }
        if( query.statusEqualTo != null  && query.statusEqualTo.length() > 0 ) {
//            sql.addConditions(CERTIFICATE_REQUEST.STATUS.equal(query.statusEqualTo));
        }
        sql.addOrderBy(CERTIFICATE_REQUEST.ID);
        Result<Record> result = sql.fetch();
        CertificateRequest[] certificateRequests = new CertificateRequest[result.size()];
        log.debug("Got {} records", certificateRequests.length);
        int i = -1; //  index into the target array certificateRequests
        long c = -1; // id of the current certificate request object built, used to detect when it's time to build the next one
        for(Record r : result) {
            if( r.getValue(CERTIFICATE_REQUEST.ID) != c ) {
                i++;
                c = r.getValue(CERTIFICATE_REQUEST.ID);
                certificateRequests[i] = new CertificateRequest();
                certificateRequests[i].setId(r.getValue(CERTIFICATE_REQUEST.ID));
                certificateRequests[i].setUuid(UUID.valueOf(r.getValue(CERTIFICATE_REQUEST.UUID)));
                certificateRequests[i].setSubject(r.getValue(CERTIFICATE_REQUEST.SUBJECT));
                certificateRequests[i].setTags(new ArrayList<CertificateRequestTagValue>());
            }
            CertificateRequestTagValue crtv = new CertificateRequestTagValue(
                    r.getValue(CERTIFICATE_REQUEST_TAG_VALUE.ID), 
                    r.getValue(CERTIFICATE_REQUEST.ID), 
                    r.getValue(CERTIFICATE_REQUEST_TAG_VALUE.TAGID), 
                    r.getValue(CERTIFICATE_REQUEST_TAG_VALUE.TAGVALUEID));
            // XXX TODO inefficient to make two extra queries for the tag name and tag value... probably better to move this up to the big query but need to design the join properly
            Tag tag = tagDao.findById(crtv.getTagId());
            TagValue tagValue = tagValueDao.findById(crtv.getTagValueId());
            if( tag != null && tagValue != null ) {
                crtv.setName(tag.getName());
                crtv.setOid(tag.getOid());
                crtv.setValue(tagValue.getValue());
                certificateRequests[i].getTags().add(crtv);
            }
            else {
                log.debug("tag is null? {}", tag == null);
                log.debug("tag-value is null? {}", tagValue == null);
            }
        }
        sql.close();
        return certificateRequests;
    }
}
