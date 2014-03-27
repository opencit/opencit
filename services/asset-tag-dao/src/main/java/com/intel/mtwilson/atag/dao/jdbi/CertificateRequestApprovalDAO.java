/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.CertificateRequestApproval;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;

/**
 * XXX TODO  Probably don't need this  now that certificates are posted directly as an approval. 
 * @deprecated
 * References:
 * http://www.jdbi.org/five_minute_intro/
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * http://www.cowtowncoder.com/blog/archives/2010/04/entry_391.html
 * http://jdbi.org/sql_object_api_batching/   (batching)
 * 
 * @author jbuhacoff
 */
@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(CertificateRequestApprovalResultMapper.class)
public interface CertificateRequestApprovalDAO {
    @SqlUpdate("create table certificate_request_approval (id bigint primary key generated always as identity, certificateRequestId bigint, approved boolean, authorityName varchar(255))")
    void create();
    
    @SqlUpdate("insert into certificate_request_approval (certificateRequestId,approved,authorityName) values (:certificateRequestId, :approved, :authorityName)")
    @GetGeneratedKeys
    long insert(@Bind("certificateRequestId") long certificateRequestId, @Bind("approved") boolean approved, @Bind("authorityName") String authorityName);

    @SqlQuery("select id,certificateRequestId,approved,authorityName from certificate_request_approval where id=:id")
    String findById(@Bind("id") long id);

    @SqlQuery("select id,certificateRequestId,approved,authorityName from certificate_request_approval where certificateRequestId=:certificateRequestId")
    List<CertificateRequestApproval> findByCertificateRequestId(@Bind("certificateRequestId") long certificateRequestId);

    @SqlQuery("select id,certificateRequestId,approved,authorityName from certificate_request_approval where authorityName=:authorityName")
    List<CertificateRequestApproval> findByAuthorityName(@Bind("authorityName") long authorityName);
    
    @SqlUpdate("delete from certificate_request_approval where certificateRequestId=:certificateRequestId")
    void deleteAll(@Bind("certificateRequestId") long certificateRequestId);
    
    void close();
}
