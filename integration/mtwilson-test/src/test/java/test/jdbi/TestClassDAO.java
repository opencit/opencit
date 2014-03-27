/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import java.io.Closeable;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterArgumentFactory;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 *
 * @author jbuhacoff
 */
@RegisterArgumentFactory(UUIDArgument.class)
//@RegisterMapper(TestResultMapper.class)
public interface TestClassDAO extends Closeable {
//    @SqlUpdate("create table test_table (id char(36) primary key, name varchar(255), description varchar(255))")
    void create();

    @SqlUpdate("insert into mw_tag_selection (id, name, description) values (:id, :name, :description)")
//    @GetGeneratedKeys
//    void insert(@Bind("id") UUID id, @Bind("name") String name, @Bind("description") String description);
    void insert(@BindBean TestClass selection); // automatically usess javabean attribute names as query placeholder names

    @SqlUpdate("update mw_tag_selection set description=:description where id=:id")
    void update(@Bind("id") UUID uuid, @Bind("description") String description);

    @SqlUpdate("delete from mw_tag_selection where id=:id")
    void delete(@BindBean TestClass selection);
    @SqlUpdate("delete from mw_tag_selection where id=:id")
    void deleteById(@Bind("id") UUID id);
    @SqlUpdate("delete from mw_tag_selection where name=:name")
    void deleteByName(@Bind String name); // if no paramter is passed to the annotation the parameter name itself is used "name"

    @SqlQuery("select id, name, description from mw_tag_selection where id=:id")
    TestClass findById(@Bind("id") UUID id);
    
}
