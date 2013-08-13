/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.CertificateRequestTagValue;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * References:
 * http://www.jdbi.org/five_minute_intro/
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * http://www.cowtowncoder.com/blog/archives/2010/04/entry_391.html
 * http://jdbi.org/sql_object_api_batching/   (batching)
 * 
 * @author jbuhacoff
 */
@RegisterMapper(CertificateRequestTagValueResultMapper.class)
public interface CertificateRequestTagValueDAO {
    @SqlUpdate("create table certificate_request_tag_value (id bigint primary key generated always as identity, certificateRequestId bigint, tagId bigint, tagValueId bigint)")
    void create();
    
    @SqlUpdate("insert into certificate_request_tag_value (certificateRequestId,tagId,tagValueId) values (:certificateRequestId, :tagId, :tagValueId)")
    @GetGeneratedKeys
    long insert(@Bind("certificateRequestId") long certificateRequestId, @Bind("tagId") long tagId, @Bind("tagValueId") long tagValueId);

    @SqlBatch("insert into certificate_request_tag_value (certificateRequestId,tagId,tagValueId) values (:certificateRequestId, :tagId, :tagValueId)")
    @BatchChunkSize(1000)
    int[] insert(@Bind("certificateRequestId") long certificateRequestId, @Bind("tagId") List<Long> tagId, @Bind("tagValueId") List<Long> tagValueId); // return value is same size as input list;  each element in the int[] array is the number of rows modified by the corresponding insert.. which would either be 1 or 0.   so you can just tally up the 1s to see if all the rows were inserted or not.   unfortunately, the api does not have a mechanism for us to get the auto-generated id's for the batch-inserted rows. 
    
    @SqlQuery("select id,certificateRequestId,tagId,tagValueId from certificate_request_tag_value where id=:id")
    String findById(@Bind("id") long id);

    // this one returns the records but they are purely relational... you'd have to make separate queries to find the tags and tag values being referenced
    @SqlQuery("select id,certificateRequestId,tagId,tagValueId from certificate_request_tag_value where certificateRequestId=:certificateRequestId")
    List<CertificateRequestTagValue> findByCertificateRequestId(@Bind("certificateRequestId") long certificateRequestId);

    // this one returns the records WITH associated information (so tag id, but also tag name and oid, and tag value id, but also tag value text)
    @SqlQuery("select certificate_request_tag_value.id,certificate_request_tag_value.certificateRequestId,certificate_request_tag_value.tagId,certificate_request_tag_value.tagValueId,tag.name,tag.oid,tag_value.value from certificate_request_tag_value,tag,tag_value where certificate_request_tag_value.certificateRequestId=:certificateRequestId and tag.id=certificate_request_tag_value.tagId and tag_value.id=certificate_request_tag_value.tagValueId")
    List<CertificateRequestTagValue> findByCertificateRequestIdWithValues(@Bind("certificateRequestId") long certificateRequestId);
    
    @SqlQuery("select id,certificateRequestId,tagId,tagValueId from certificate_request_tag_value where tagId=:tagId")
    List<CertificateRequestTagValue> findByTagId(@Bind("tagId") long tagId);
    
    @SqlQuery("select id,certificateRequestId,tagId,tagValueId from certificate_request_tag_value where tagValueId=:tagValueId")
    List<CertificateRequestTagValue> findByTagValueId(@Bind("tagValueId") String tagValueId);

    @SqlUpdate("delete from certificate_request_tag_value where certificateRequestId=:certificateRequestId")
    void deleteAll(@Bind("certificateRequestId") long certificateRequestId);
    
    void close();
}
