/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Selection;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import com.intel.mtwilson.jdbi.util.UUIDArgument;

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
@RegisterMapper(SelectionResultMapper.class)
public interface SelectionDAO extends Closeable{
    @SqlUpdate("create table mw_tag_selection (id char(36) primary key, name varchar(255), description varchar(255))")
    void create();

    @SqlUpdate("insert into mw_tag_selection (id, name, description) values (:id, :name, :description)")
    void insert(@Bind("id") UUID id, @Bind("name") String name, @Bind("description") String description);

    @SqlUpdate("insert into mw_tag_selection (id, name, description) values (:id, :name, :description)")
    void insert(@BindBean Selection selection); // automatically usess javabean attribute names as query placeholder names

    @SqlUpdate("update mw_tag_selection set description=:description where id=:id")
    void update(@Bind("id") UUID id, @Bind("description") String description);

    @SqlUpdate("delete from mw_tag_selection where id=:id")
    void delete(@BindBean Selection selection);
    
    @SqlUpdate("delete from mw_tag_selection where id=:id")
    void deleteById(@Bind("id") UUID id);

    @SqlUpdate("delete from mw_tag_selection where name=:name")
    void deleteByName(@Bind String name); // if no paramter is passed to the annotation the parameter name itself is used "name"

    @SqlQuery("select id, name, description from mw_tag_selection where id=:id")
    Selection findById(@Bind("id") UUID id);
    
    @SqlQuery("select id, name, description from mw_tag_selection where name=:name")
    Selection findByName(@Bind("name") String name);
    
    @Override
    void close();
}
