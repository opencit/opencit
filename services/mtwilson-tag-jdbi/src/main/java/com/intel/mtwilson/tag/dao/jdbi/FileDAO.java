/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.File;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
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
@RegisterArgumentFactory({UUIDArgument.class,MediaTypeArgument.class})
@RegisterMapper(FileResultMapper.class)
public interface FileDAO extends Closeable {
    @SqlUpdate("create table mw_file (id char(36) primary key, name varchar(255), contentType varchar(255), content blob)")
    void create();
    
    @SqlUpdate("insert into mw_file (id , name, contentType, content) values (:id, :name, :contentType, :content)")
//    @GetGeneratedKeys
    void insert(@Bind("id") UUID id, @Bind("name") String name, @Bind("contentType") String contentType, @Bind("content") byte[] content);

    @SqlUpdate("update mw_file set name=:name, contentType=:contentType, content=:content where id=:id")
    void update(@Bind("id") UUID id, @Bind("name") String name, @Bind("contentType") String contentType, @Bind("content") byte[] content);

    @SqlUpdate("delete from mw_file where id=:id")
    void delete(@Bind("id") UUID id);

    @SqlQuery("select id, name, contentType, content from mw_file where id=:id")
    File findById(@Bind("id") UUID id);
    
    @SqlQuery("select id, name, contentType, content from mw_file where name=:name")
    File findByName(@Bind("name") String name);

    @Override
    void close();
}
