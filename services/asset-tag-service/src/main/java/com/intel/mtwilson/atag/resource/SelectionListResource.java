/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.model.SelectionSearchCriteria;
import com.intel.mtwilson.atag.model.SelectionTagValue;
import com.intel.mtwilson.atag.model.Selection;
import com.intel.mtwilson.atag.model.TagValue;
import com.intel.mtwilson.atag.dao.jdbi.SelectionTagValueDAO;
import com.intel.mtwilson.atag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.*;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class SelectionListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private SelectionDAO selectionDao = null;
    private SelectionTagValueDAO selectionTagValueDao = null;
    private TagDAO tagDao = null;
    private TagValueDAO tagValueDao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            selectionDao = Derby.selectionDao();
            selectionTagValueDao = Derby.selectionTagValueDao();
            tagDao = Derby.tagDao();
            tagValueDao = Derby.tagValueDao();
            jooq = Derby.jooq();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        if (selectionDao != null) {
            selectionDao.close();
        }
        if( selectionTagValueDao != null ) { selectionTagValueDao.close(); }
        super.doRelease();
    }

    /**
     * Input certificate requests provide a certificate subject and one or more tags where each tag
     * is a pair of (name,value) or (oid,value).
     * We look up the tagId and tagValueId for each tag and populate them in the SelectionTagValue
     * object. Then we can insert it to the database.
     */
//    @Post("json:json")
    public Selection insertSelection(Selection selection) throws SQLException {
        log.debug("insertSelection for name: {}", selection.getName());
        selection.setUuid(new UUID());
        long selectionId = selectionDao.insert(selection.getUuid(), selection.getName());
        selection.setId(selectionId);
//        Selection result = new Selection(selectionId, selection.getUuid(), selection.getSubject());
        log.debug("inserted selection has id {}", selectionId);
        log.debug("inserted selection has uuid {}", selection.getUuid());
        // now insert all the tag values
        ArrayList<Long> tags = new ArrayList<Long>();
        ArrayList<Long> tagValues = new ArrayList<Long>();
        log.debug("insertSelection has {} tags", selection.getTags().size());
        for(SelectionTagValue crtv : selection.getTags()) {
            // look up tagId and tagValueId for (uuid,value) or (name,value) or (oid,value) pairs
            if( crtv.getUuid() != null ) { 
                log.debug("tag uuid: {}", crtv.getUuid());
                Tag byUuid = tagDao.findByUuid(crtv.getUuid()); 
                if( byUuid != null ) {
                    crtv.setTagId(byUuid.getId());
                }
            }
            if( crtv.getTagName() != null ) { 
                log.debug("tag name: {}", crtv.getTagName());
                Tag byName = tagDao.findByName(crtv.getTagName()); 
                if( byName != null ) {
                    crtv.setTagId(byName.getId());
                }
            }
            else if( crtv.getTagOid() != null ) { 
                log.debug("tag oid: {}", crtv.getTagOid());
                Tag byOid = tagDao.findByOid(crtv.getTagOid()); 
                if( byOid != null ) {
                    crtv.setTagId(byOid.getId());
                }
            }
            log.debug("tag value (required): {}", crtv.getTagValue());
            TagValue byValue = tagValueDao.findByTagIdAndValueEquals(crtv.getTagId(), crtv.getTagValue());
            if( byValue != null ) {
                crtv.setTagValueId(byValue.getId());
            }
            // set the tagId and tagValueId we found in the SelectionTagValue record
//            tags.add(crtv.getTagId());
//            tagValues.add(crtv.getTagValueId());
            log.debug("Inserting certificate request tag value: {}", String.format("req id: %d   tag id: %d   tag value id: %d", selectionId, crtv.getTagId(), crtv.getTagValueId()));
            selectionTagValueDao.insert(selectionId, crtv.getTagId(), crtv.getTagValueId());
        }
//        selectionTagValueDao.insert(selectionId, tags, tagValues); // the bulk insert is not working?
        return selection;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept Selection[] selections as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.aselection.model.Selection out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.aselection.model.Selection
     *
     * @param selections
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public Selection[] insertSelections(Selection[] selections) throws SQLException {
        Selection[] results = new Selection[selections.length];
        for (int i = 0; i < selections.length; i++) {
            results[i] = insertSelection(selections[i]);
//            insertSelection(selections[i]);
        }
        return results;
//        return selections;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because selection values are in a separate table, if the client wants to find selections that
     * have specific values, we need to search for those values first. 
     * 
     * find selection where selection.id = selection_value.selectionId and selection_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    
    @Get("json")
    public Selection[] search() throws SQLException {
        SelectionSearchCriteria query = new SelectionSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.nameEqualTo = getQuery().getFirstValue("nameEqualTo");
        query.nameContains = getQuery().getFirstValue("nameContains");
        query.subjectEqualTo = getQuery().getFirstValue("subjectEqualTo");
        query.subjectContains = getQuery().getFirstValue("subjectContains");
        query.tagNameEqualTo = getQuery().getFirstValue("tagNameEqualTo");
        query.tagNameContains = getQuery().getFirstValue("tagNameContains");
        query.tagOidEqualTo = getQuery().getFirstValue("tagOidEqualTo");
        query.tagOidContains = getQuery().getFirstValue("tagOidContains");
        query.tagValueEqualTo = getQuery().getFirstValue("tagValueEqualTo");
        query.tagValueContains = getQuery().getFirstValue("tagValueContains");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select()
                .from(SELECTION.join(SELECTION_TAG_VALUE)
                .on(SELECTION_TAG_VALUE.SELECTIONID.equal(SELECTION.ID))).getQuery();
        if( query.tagValueEqualTo != null || query.tagValueContains != null ) {
            log.debug("Selecting from tag-value");
            SelectQuery valueQuery = jooq.select(SELECTION_TAG_VALUE.ID)
                    .from(SELECTION_TAG_VALUE.join(TAG_VALUE).on(TAG_VALUE.ID.equal(SELECTION_TAG_VALUE.TAGVALUEID)))
                    .getQuery();
            if( query.tagValueEqualTo != null && query.tagValueEqualTo.length() > 0 ) {
                valueQuery.addConditions(TAG_VALUE.VALUE.equal(query.tagValueEqualTo));
            }
            if( query.tagValueContains != null  && query.tagValueContains.length() > 0 ) {
                valueQuery.addConditions(TAG_VALUE.VALUE.contains(query.tagValueContains));
            }
            sql.addConditions(SELECTION_TAG_VALUE.ID.in(valueQuery));
        }
        if( query.tagNameContains != null || query.tagNameEqualTo != null || query.tagOidContains != null || query.tagOidEqualTo != null ) {
            log.debug("Selecting from tag");
            SelectQuery tagQuery = jooq.select(SELECTION_TAG_VALUE.ID)
                    .from(SELECTION_TAG_VALUE.join(TAG).on(TAG.ID.equal(SELECTION_TAG_VALUE.TAGID)))
                    .getQuery();
            if( query.tagNameEqualTo != null  && query.tagNameEqualTo.length() > 0 ) {
                tagQuery.addConditions(TAG.NAME.equal(query.tagNameEqualTo));
            }
            if( query.tagNameContains != null  && query.tagNameContains.length() > 0 ) {
                tagQuery.addConditions(TAG.NAME.contains(query.tagNameContains));
            }
            if( query.tagOidEqualTo != null  && query.tagOidEqualTo.length() > 0 ) {
                tagQuery.addConditions(TAG.OID.equal(query.tagOidEqualTo));
            }
            if( query.tagOidContains != null  && query.tagOidContains.length() > 0 ) {
                tagQuery.addConditions(TAG.OID.contains(query.tagOidContains));
            }
            sql.addConditions(SELECTION_TAG_VALUE.ID.in(tagQuery));            
        }
        // XXX TODO implement the host selection... for that we would need a subject (host) table ... that would allow the sysadmin from the UI to select which hosts to apply a specific tag selection to, and save that selection.
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(SELECTION.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.nameEqualTo != null  && query.nameEqualTo.length() > 0 ) {
            sql.addConditions(SELECTION.NAME.equal(query.nameEqualTo));
        }
        if( query.nameContains != null  && query.nameContains.length() > 0  ) {
            sql.addConditions(SELECTION.NAME.contains(query.nameContains));
        }
        sql.addOrderBy(SELECTION.ID);
        Result<Record> result = sql.fetch();
        Selection[] selections = new Selection[result.size()]; // XXX TODO because of row folding due to the join (below where we combine values from a bunch of rows into one Selection record) there will be nulls in this array... should chane to an ArrayList instead and only add the folded records as we go,  then convert to an array later for the return value
        log.debug("Got {} records", selections.length);
        int i = -1; //  index into the target array selections
        long c = -1; // id of the current certificate request object built, used to detect when it's time to build the next one
        for(Record r : result) {
            if( r.getValue(SELECTION.ID) != c ) {
                i++;
                c = r.getValue(SELECTION.ID);
                selections[i] = new Selection();
                selections[i].setId(r.getValue(SELECTION.ID));
                selections[i].setUuid(UUID.valueOf(r.getValue(SELECTION.UUID)));
                selections[i].setName(r.getValue(SELECTION.NAME));
                selections[i].setTags(new ArrayList<SelectionTagValue>());
            }
            SelectionTagValue crtv = new SelectionTagValue(
                    r.getValue(SELECTION_TAG_VALUE.ID), 
                    r.getValue(SELECTION.ID), 
                    r.getValue(SELECTION_TAG_VALUE.TAGID), 
                    r.getValue(SELECTION_TAG_VALUE.TAGVALUEID));
            // XXX TODO inefficient to make two extra queries for the tag name and tag value... probably better to move this up to the big query but need to design the join properly
            Tag tag = tagDao.findById(crtv.getTagId());
            TagValue tagValue = tagValueDao.findById(crtv.getTagValueId());
            if( tag != null && tagValue != null ) {
                crtv.setTagName(tag.getName());
                crtv.setTagOid(tag.getOid());
                crtv.setTagValue(tagValue.getValue());
                crtv.setTagUuid(tag.getUuid());
                // TODO:  crtv.setTagValueUuid(tagValue.getUuid());
                selections[i].getTags().add(crtv);
            }
            else {
                log.debug("tag is null? {}", tag == null);
                log.debug("tag-value is null? {}", tagValue == null);
            }
        }
        sql.close();
        return selections;
    }
}
