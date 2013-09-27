/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.Certificate;
import com.intel.mtwilson.atag.model.CertificateRequestApproval;
import com.intel.dcsg.cpg.io.UUID;
import java.util.Date;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
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
@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(CertificateResultMapper.class)
public interface CertificateDAO {
    @SqlUpdate("create table certificate (id bigint primary key generated always as identity, uuid char(36), certificate blob, sha256 char(64), pcrEvent char(40), subject varchar(255), issuer varchar(255), notBefore timestamp, notAfter timestamp, revoked boolean)")
    void create();
    
    @SqlUpdate("insert into certificate (uuid, certificate, sha256, pcrEvent, subject, issuer, notBefore, notAfter, revoked) values (:uuid, :certificate, :sha256, :pcrEvent, :subject, :issuer, :notBefore, :notAfter, false)")
    @GetGeneratedKeys
    long insert(@Bind("uuid") UUID uuid, @Bind("certificate") byte[] certificate, @Bind("sha256") String sha256, @Bind("pcrEvent") String pcrEvent, @Bind("subject") String subject, @Bind("issuer") String issuer, @Bind("notBefore") Date notBefore, @Bind("notAfter") Date notAfter);

    @SqlUpdate("update certificate set revoked=:revoked where id=:id")
    long updateRevoked(@Bind("id") long id, @Bind("revoked") boolean revoked);
    
    @SqlQuery("select id,uuid,certificate,sha256,pcrEvent,subject,issuer,notBefore,notAfter,revoked from certificate where id=:id")
    Certificate findById(@Bind("id") long id);

    @SqlQuery("select id,uuid,certificate,sha256,pcrEvent,subject,issuer,notBefore,notAfter,revoked from certificate where uuid=:uuid")
    Certificate findByUuid(@Bind("uuid") UUID uuid);

    @SqlQuery("select id,uuid,certificate,sha256,pcrEvent,subject,issuer,notBefore,notAfter,revoked from certificate where sha256=:sha256")
    Certificate findBySha256(@Bind("sha256") String sha256);
    
    @SqlUpdate("delete from certificate where id=:id")
    void delete(@Bind("id") long id);
    
    void close();
}
