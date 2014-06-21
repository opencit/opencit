package com.intel.mtwilson.tls.policy.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

/**
 *
 * @author jbuhacoff
 */
@RegisterArgumentFactory({UUIDArgument.class})
@RegisterMapper({TlsPolicyResultMapper.class,})
public interface TlsPolicyDAO extends Closeable {
    @SqlUpdate("insert into mw_tls_policy (id, name, content_type, content, comment) values (:id, :name, :contentType, :content, :comment)")
     void insertTlsPolicy(@BindBean TlsPolicyRecord tlsPolicyRecord);
//    void insertTlsPolicy(@Bind("id") String id, @Bind("name") String name, @Bind("impl") String impl, @Bind("content_type") String contentType, @Bind("content") byte[] content, @Bind("comment") String comment);

    @SqlUpdate("update mw_tls_policy set name=:name, content_type=:contentType, content=:content, comment=:comment where id=:id")
    void updateTlsPolicy(@BindBean TlsPolicyRecord tlsPolicyRecord);
//    void updateTlsPolicy(@Bind("id") String id, @Bind("name") String name, @Bind("impl") String impl, @Bind("content_type") String contentType, @Bind("content") byte[] content, @Bind("comment") String comment);
    
    @SqlQuery("select id, name, content_type, content, comment from mw_tls_policy where id=:id")
    TlsPolicyRecord findTlsPolicyById(@Bind("id") UUID id);
    
    @SqlQuery("select id, name, content_type, content, comment from mw_tls_policy where name=:name")
    TlsPolicyRecord findTlsPolicyByName(@Bind("name") String name);
    
    @SqlUpdate("delete from mw_tls_policy where id=:id")
    void deleteTlsPolicyById(@Bind("id") UUID id);
    
}
