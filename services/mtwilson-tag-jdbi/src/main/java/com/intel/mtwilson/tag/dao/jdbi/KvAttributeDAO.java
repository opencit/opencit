/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.KvAttribute;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import org.skife.jdbi.v2.sqlobject.BindBean;

@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(KvAttributeResultMapper.class)
public interface KvAttributeDAO extends Closeable {
    @SqlUpdate("create table mw_tag_kvattribute (id char(36) primary key, name varchar(255), value varchar(255))")
//    @SqlUpdate("create table mw_tag_kvattribute (id char(16) for bit data primary key, name varchar(100), value varchar(255))")
    void create();
    
    @SqlUpdate("insert into mw_tag_kvattribute (id, name, value) values (:id, :name, :value)")
//    @GetGeneratedKeys
    void insert(@Bind("id") UUID id, @Bind("name") String name, @Bind("value") String value);
    //void insert(@Bind("id") String id, @Bind("name") String name, @Bind("value") String value);
    @SqlUpdate("insert into mw_tag_kvattribute (id, name, value) values (:id, :name, :value)")
//    @GetGeneratedKeys
    void insert(@BindBean KvAttribute kvattribute);

    @SqlUpdate("update mw_tag_kvattribute set name=:name, value=:value where id=:id")
    void update(@Bind("id") UUID id, @Bind("name") String name, @Bind("value") String value);

    @SqlUpdate("delete from mw_tag_kvattribute where id=:id")
    void delete(@Bind("id") UUID id);

    @SqlQuery("select id, name, value from mw_tag_kvattribute where id=:id")
    KvAttribute findById(@Bind("id") UUID id);
    
    @SqlQuery("select id, name, value from mw_tag_kvattribute where name=:name and value=:value")
    KvAttribute findByNameAndValue(@Bind("name") String name, @Bind("value") String value);

    // @SqlQuery("select id, uuid, name, oid from tag where uuid=:uuid")
    // Tag findByUuid(@Bind("uuid") UUID uuid);

    @SqlQuery("select id, name, value from mw_tag_kvattribute where name=:name")
    KvAttribute findByName(@Bind("name") String name);
    
//    @SqlQuery("select id, name, value from mw_tag_kvattribute where value=:value")
//    KvAttribute findByOid(@Bind("value") String value);
    
//    @SqlQuery("select id, name, oid from mw_tag_attribute where oid=:oid or name=:name")
//    KvAttribute findByOidOrName(@Bind("oid") String oid, @Bind("name") String name);
    
//    @SqlQuery("select name from mw_tag_attribute where id=:id")
//    String findNameById(@Bind("id") UUID id);
    
    @Override
    void close();
}
