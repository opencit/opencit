/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.KvAttribute;
import java.io.Closeable;
//import com.intel.mtwilson.tag.rest.v2.model.AttributeCollection;
import org.skife.jdbi.v2.sqlobject.Bind;
//import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(KvAttributeResultMapper.class)
public interface KvAttributeDAO extends Closeable {
    // Note:  if you change the table definition (for example uuid from binary to char) also check the KvAttributeResultMapper class that is used for jdbi queries
//    @SqlUpdate("create table tag (id bigint primary key generated always as identity, uuid char(16) for bit data, name varchar(100), oid varchar(255))")   // jooq tries to cast char(16) for bit data  into a blob for comparisons... don't know why. and it's not possible to search on blob contents (usually not implemented by rdbms because blobs by definition can be gigabytes long), so using char(36) instead to get the standard uuid format
    @SqlUpdate("create table mw_tag_kvattribute (id bigint primary key generated always as identity, uuid char(36), name varchar(100), oid varchar(255))")
    void create();
    
    @SqlUpdate("insert into mw_tag_kvattribute (id, name, value) values (:id, :name, :value)")
//    @GetGeneratedKeys
    KvAttribute insert(@Bind("id") UUID uuid, @Bind("name") String name, @Bind("value") String value);

    @SqlUpdate("update mw_tag_kvattribute set name=:name, value=:value where id=:id")
    void update(@Bind("id") UUID uuid, @Bind("name") String name, @Bind("value") String value);

    @SqlUpdate("delete from mw_tag_kvattribute where id=:id")
    void delete(@Bind("id") UUID uuid);

    @SqlQuery("select id, name, value from mw_tag_kvattribute where id=:id")
    KvAttribute findById(@Bind("id") UUID uuid);
    
    // @SqlQuery("select id, uuid, name, oid from tag where uuid=:uuid")
    // Tag findByUuid(@Bind("uuid") UUID uuid);

    @SqlQuery("select id, name, value from mw_tag_kvattribute where name=:name")
    KvAttribute findByName(@Bind("name") String name);
    
    @SqlQuery("select id, name, value from mw_tag_kvattribute where value=:value")
    KvAttribute findByOid(@Bind("value") String value);
    
//    @SqlQuery("select id, name, oid from mw_tag_attribute where oid=:oid or name=:name")
//    KvAttribute findByOidOrName(@Bind("oid") String oid, @Bind("name") String name);
    
//    @SqlQuery("select name from mw_tag_attribute where id=:id")
//    String findNameById(@Bind("id") UUID id);
    
    @Override
    void close();
}
