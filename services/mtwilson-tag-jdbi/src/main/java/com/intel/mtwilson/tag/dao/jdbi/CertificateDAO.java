/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.Certificate;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.DateArgument;
import java.io.Closeable;
import java.util.Date;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;

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
@RegisterArgumentFactory({UUIDArgument.class,DateArgument.class})
@RegisterMapper(CertificateResultMapper.class)
public interface CertificateDAO extends Closeable{
    @SqlUpdate("create table mw_tag_certificate (id char(36) primary key, certificate blob, sha1 char(40), sha256 char(64), subject varchar(255), issuer varchar(255), notBefore timestamp, notAfter timestamp, revoked boolean)")
    void create();
    
    @SqlUpdate("insert into mw_tag_certificate (id, certificate, sha1, sha256, subject, issuer, notBefore, notAfter, revoked) "
            + "values (:id, :certificate, :sha1, :sha256, :subject, :issuer, :notBefore, :notAfter, false)")
//    @GetGeneratedKeys
    void insert(@Bind("id") UUID id, @Bind("certificate") byte[] certificate, @Bind("sha1") String sha1, 
    @Bind("sha256") String sha256, @Bind("subject") String subject, @Bind("issuer") String issuer, @Bind("notBefore") Date notBefore, @Bind("notAfter") Date notAfter);

    @SqlUpdate("update mw_tag_certificate set revoked=:revoked where id=:id")
    void updateRevoked(@Bind("id") UUID id, @Bind("revoked") boolean revoked);
    
    @SqlQuery("select id,certificate,sha1,sha256,subject,issuer,notBefore,notAfter,revoked from mw_tag_certificate where id=:id")
    Certificate findById(@Bind("id") UUID id);

//    @SqlQuery("select id,uuid,certificate,sha1,sha256,subject,issuer,notBefore,notAfter,revoked from mw_tag_certificate where uuid=:uuid")
//    Certificate findByUuid(@Bind("uuid") UUID uuid);

    @SqlQuery("select id,certificate,sha1,sha256,subject,issuer,notBefore,notAfter,revoked from mw_tag_certificate where sha1=:sha1")
    Certificate findBySha1(@Bind("sha1") String sha1);

    @SqlQuery("select id,certificate,sha1,sha256,subject,issuer,notBefore,notAfter,revoked from mw_tag_certificate where sha256=:sha256")
    Certificate findBySha256(@Bind("sha256") String sha256);
    
    @SqlUpdate("delete from mw_tag_certificate where id=:id")
    void delete(@Bind("id") UUID id);
    
    @Override
    void close();
}
