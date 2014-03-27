/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.RdfTriple;
import com.intel.dcsg.cpg.io.UUID;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
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
@RegisterMapper(RdfTripleResultMapper.class)
public interface RdfTripleDAO {
    @SqlUpdate("create table rdf_triple (id bigint primary key generated always as identity, uuid char(36), subject varchar(255), predicate varchar(255), object varchar(255))")
    void create();
    
    @SqlUpdate("insert into rdf_triple (uuid,subject,predicate,object) values (:uuid, :subject, :predicate, :object)")
    @GetGeneratedKeys
    long insert(@Bind("uuid") UUID uuid, @Bind("subject") String subject, @Bind("predicate") String predicate, @Bind("object") String object);

    @SqlUpdate("update rdf_triple set subject=:subject, predicate=:predicate, object=:object where id=:id")
    void update(@Bind("id") long id, @Bind("subject") String subject, @Bind("predicate") String predicate, @Bind("object") String object);

    @SqlUpdate("delete from rdf_triple where id=:id")
    void delete(@Bind("id") long id);

    
    @SqlQuery("select id, uuid, subject, predicate, object from rdf_triple where uuid=:uuid")
    RdfTriple findByUuid(@Bind("uuid") UUID uuid);
    
    @SqlQuery("select name from rdf_triple where subject=:subject")
    RdfTriple[] findBySubject(@Bind("subject") String subject);

    @SqlQuery("select name from rdf_triple where object=:object")
    RdfTriple[] findByObject(@Bind("object") String object);
    
    void close();
}
