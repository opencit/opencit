/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

/**
 * References:
 * http://jdbi.org/sql_object_api_argument_binding/
 * http://skife.org/jdbi/java/library/sql/2011/03/16/jdbi-sql-objects.html  (map result set to object)
 * @author jbuhacoff
 */
public interface SimpleObjectDAO {
    @SqlUpdate("create table simpleobject (id int primary key, name varchar(100))")
    void create();
    
    @SqlUpdate("insert into simpleobject (id,name) values (:id, :name)")
    void insert(@Bind("id") int id, @Bind("name") String name);

    @SqlQuery("select name from simpleobject where id=:id")
    String findNameById(@Bind("id") int id);
    
    void close();
}
