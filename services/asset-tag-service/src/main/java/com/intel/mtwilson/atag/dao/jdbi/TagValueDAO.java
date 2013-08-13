/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.TagValue;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

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
@RegisterMapper(TagValueResultMapper.class)
public interface TagValueDAO {
    @SqlUpdate("create table tag_value (id bigint primary key generated always as identity, tagId bigint, value varchar(255))")
    void create();
    
    @SqlUpdate("insert into tag_value (tagId,value) values (:tagId, :value)")
    @GetGeneratedKeys
    long insert(@Bind("tagId") long tagId, @Bind("value") String value);

    @SqlBatch("insert into tag_value (tagId,value) values (:tagId, :value)")
    @BatchChunkSize(1000)
    int[] insert(@Bind("tagId") long tagId, @Bind("value") List<String> value); // return value is same size as input list;  each element in the int[] array is the number of rows modified by the corresponding insert.. which would either be 1 or 0.   so you can just tally up the 1s to see if all the rows were inserted or not.   unfortunately, the api does not have a mechanism for us to get the auto-generated id's for the batch-inserted rows. 
    
    @SqlQuery("select id,tagId,value from tag_value where id=:id")
    TagValue findById(@Bind("id") long id);

    @SqlQuery("select id,tagId,value from tag_value where tagId=:tagId")
    List<TagValue> findByTagId(@Bind("tagId") long tagId);
    
    @SqlQuery("select id,tagId,value from tag_value where value=:value")
    List<TagValue> findByValueEquals(@Bind("value") String value);

    @SqlQuery("select id,tagId,value from tag_value where value LIKE :value")
    List<TagValue> findByValueContains(@Bind("value") String value);
    
    @SqlQuery("select id,tagId,value from tag_value where tagId=:tagId and value=:value")
    TagValue findByTagIdAndValueEquals(@Bind("tagId") long tagId, @Bind("value") String value);
            
    @SqlUpdate("delete from tag_value where tagId=:tagId")
    void deleteAll(@Bind("tagId") long tagId);
    
    void close();
}
