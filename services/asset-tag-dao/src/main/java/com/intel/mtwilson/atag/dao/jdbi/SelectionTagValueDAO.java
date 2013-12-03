/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jdbi;

import com.intel.mtwilson.atag.model.SelectionTagValue;
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
@RegisterMapper(SelectionTagValueResultMapper.class)
public interface SelectionTagValueDAO {
    @SqlUpdate("create table selection_tag_value (id bigint primary key generated always as identity, selectionId bigint, tagId bigint, tagValueId bigint)")
    void create();
    
    @SqlUpdate("insert into selection_tag_value (selectionId,tagId,tagValueId) values (:selectionId, :tagId, :tagValueId)")
    @GetGeneratedKeys
    long insert(@Bind("selectionId") long selectionId, @Bind("tagId") long tagId, @Bind("tagValueId") long tagValueId);

    @SqlBatch("insert into selection_tag_value (selectionId,tagId,tagValueId) values (:selectionId, :tagId, :tagValueId)")
    @BatchChunkSize(1000)
    int[] insert(@Bind("selectionId") long selectionId, @Bind("tagId") List<Long> tagId, @Bind("tagValueId") List<Long> tagValueId); // return value is same size as input list;  each element in the int[] array is the number of rows modified by the corresponding insert.. which would either be 1 or 0.   so you can just tally up the 1s to see if all the rows were inserted or not.   unfortunately, the api does not have a mechanism for us to get the auto-generated id's for the batch-inserted rows. 
    
    @SqlQuery("select id,selectionId,tagId,tagValueId from selection_tag_value where id=:id")
    String findById(@Bind("id") long id);

    // this one returns the records but they are purely relational... you'd have to make separate queries to find the tags and tag values being referenced
    @SqlQuery("select id,selectionId,tagId,tagValueId from selection_tag_value where selectionId=:selectionId")
    List<SelectionTagValue> findBySelectionId(@Bind("selectionId") long selectionId);

    // this one returns the records WITH associated information (so tag id, but also tag name and oid, and tag value id, but also tag value text)
    @SqlQuery("select selection_tag_value.id,selection_tag_value.selectionId,selection_tag_value.tagId,selection_tag_value.tagValueId,tag.uuid,tag.name,tag.oid,tag_value.value from selection_tag_value,tag,tag_value where selection_tag_value.selectionId=:selectionId and tag.id=selection_tag_value.tagId and tag_value.id=selection_tag_value.tagValueId")
    List<SelectionTagValue> findBySelectionIdWithValues(@Bind("selectionId") long selectionId);
    
    @SqlQuery("select id,selectionId,tagId,tagValueId from selection_tag_value where tagId=:tagId")
    List<SelectionTagValue> findByTagId(@Bind("tagId") long tagId);
    
    @SqlQuery("select id,selectionId,tagId,tagValueId from selection_tag_value where tagValueId=:tagValueId")
    List<SelectionTagValue> findByTagValueId(@Bind("tagValueId") String tagValueId);

    @SqlUpdate("delete from selection_tag_value where selectionId=:selectionId")
    void deleteAll(@Bind("selectionId") long selectionId);
    
    void close();
}
