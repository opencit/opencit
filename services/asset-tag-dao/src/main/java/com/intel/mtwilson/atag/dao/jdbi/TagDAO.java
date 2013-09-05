/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * JDBI References:
 * http://www.jdbi.org/five_minute_intro/
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * http://www.cowtowncoder.com/blog/archives/2010/04/entry_391.html
 * http://blog.jooq.org/2012/04/15/jdbi-a-simple-convenience-layer-on-top-of-jdbc/
 * https://groups.google.com/forum/#!topic/jdbi/ZDqnfhK758g      (get generated keys from inserts)
 * https://github.com/brianm/jdbi/blob/master/src/test/java/org/skife/jdbi/v2/sqlobject/TestGetGeneratedKeys.java     (get generated keys from inserts)
 * Derby References:
 * http://db.apache.org/derby/docs/10.2/ref/rrefsqlj37836.html  about identity columns
 * http://db.apache.org/derby/docs/10.0/manuals/develop/develop132.html  about identity columns
 * CHAR(16) FOR BIT DATA  - this is the Derby type for fixed-length (16 bytes in this case) binary data 
 * 
 * INTEGER is 4 bytes and maps to java's int (java.lang.Integer.MAX_VALUE = 2,147,483,647  = 2.1x10^9 = 2 billion)
 * BIGINT is 8 bytes and maps to java's long (java.lang.Long.MAX_VALUE = 9,223,372,036,854,775,807 = 9.2x10^18 = 9 quintillion) (so be sure to return the appropriate type from the insert method)
 * @author jbuhacoff
 */
@RegisterArgumentFactory(UUIDArgument.class)
@RegisterMapper(TagResultMapper.class)
public interface TagDAO {
    // Note:  if you change the table definition (for example uuid from binary to char) also check the TagResultMapper class that is used for jdbi queries
//    @SqlUpdate("create table tag (id bigint primary key generated always as identity, uuid char(16) for bit data, name varchar(100), oid varchar(255))")   // jooq tries to cast char(16) for bit data  into a blob for comparisons... don't know why. and it's not possible to search on blob contents (usually not implemented by rdbms because blobs by definition can be gigabytes long), so using char(36) instead to get the standard uuid format
    @SqlUpdate("create table tag (id bigint primary key generated always as identity, uuid char(36), name varchar(100), oid varchar(255))")
    void create();
    
    @SqlUpdate("insert into tag (uuid,name,oid) values (:uuid, :name, :oid)")
    @GetGeneratedKeys
    long insert(@Bind("uuid") UUID uuid, @Bind("name") String name, @Bind("oid") String oid);

    @SqlUpdate("update tag set name=:name, oid=:oid where id=:id")
    void update(@Bind("id") long id, @Bind("name") String name, @Bind("oid") String oid);

    @SqlUpdate("delete from tag where id=:id")
    void delete(@Bind("id") long id);

    @SqlQuery("select id, uuid, name, oid from tag where id=:id")
    Tag findById(@Bind("id") long id);

    
    @SqlQuery("select id, uuid, name, oid from tag where uuid=:uuid")
    Tag findByUuid(@Bind("uuid") UUID uuid);

    @SqlQuery("select id, uuid, name, oid from tag where name=:name")
    Tag findByName(@Bind("name") String name);
    
    @SqlQuery("select id, uuid, name, oid from tag where oid=:oid")
    Tag findByOid(@Bind("oid") String oid);
    
    @SqlQuery("select id, uuid, name, oid from tag where oid=:oid or name=:name")
    Tag findByOidOrName(@Bind("oid") String oid, @Bind("name") String name);
    
    // XXX this one is not really used externally, it's only referenced by a very early junit test  in RepositoryTest
    @SqlQuery("select name from tag where id=:id")
    String findNameById(@Bind("id") long id);
    
    void close();
}
