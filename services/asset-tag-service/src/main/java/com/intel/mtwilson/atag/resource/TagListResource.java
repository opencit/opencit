/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.model.TagSearchCriteria;
import com.intel.mtwilson.atag.model.TagValue;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.dao.Derby;
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
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
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
public class TagListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private TagDAO dao = null;
    private TagValueDAO tagValueDao;
    private DSLContext jooq = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            log.debug("doInit opening tag dao"); //System.out.println("doInit opening tag dao");
            dao = Derby.tagDao();
            log.debug("doInit opening tag-value dao"); //System.out.println("doInit opening tag-value dao");
            tagValueDao = Derby.tagValueDao(); 
            log.debug("doInit opening jooq"); //System.out.println("doInit opening jooq");
            jooq = Derby.jooq();
            log.debug("doInit success"); //System.out.println("doInit success");
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        if (dao != null) {
            dao.close();
        }
        if( tagValueDao != null ) { tagValueDao.close(); }
        super.doRelease();
    }

//    @Post("json:json")
    public Tag insertTag(Tag tag) throws SQLException {
        tag.setUuid(new UUID());
        log.debug("insertTag  uuid: {}", tag.getUuid());
        long tagId = dao.insert(tag.getUuid(), tag.getName(), tag.getOid());
        log.debug("insertTag  success, tagId: {}", tagId);
        if (tag.getValues() != null && !tag.getValues().isEmpty()) {
            tagValueDao.insert(tagId, tag.getValues());
        }
        tag.setId(tagId);
        return tag;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept Tag[] tags as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.atag.model.Tag out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.atag.model.Tag
     *
     * @param tags
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public Tag[] insertTags(Tag[] tags) throws SQLException {
        for (int i = 0; i < tags.length; i++) {
            tags[i] = insertTag(tags[i]);
        }
        return tags;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * http://www.jooq.org/doc/2.4/manual/JOOQ/ResultQuery/
     * http://comments.gmane.org/gmane.comp.java.jooq.user/1771
     * http://www.jooq.org/javadoc/2.3.x/org/jooq/impl/Factory.html
     * http://www.jooq.org/javadoc/latest/org/jooq/impl/DSL.html
     * 
     * Because tag values are in a separate table, if the client wants to find tags that
     * have specific values, we need to search for those values first. 
     * 
     * find tag where tag.id = tag_value.tagId and tag_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    @Get("json")
    public Tag[] search(/*TagSearchCriteria query*/) throws SQLException {
        TagSearchCriteria query = new TagSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.nameContains = getQuery().getFirstValue("nameContains");
        query.nameEqualTo = getQuery().getFirstValue("nameEqualTo");
        query.oidEqualTo = getQuery().getFirstValue("oidEqualTo");
        query.oidStartsWith = getQuery().getFirstValue("oidStartsWith");
        query.valueContains = getQuery().getFirstValue("valueContains");
        query.valueEqualTo = getQuery().getFirstValue("valueEqualTo");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql;         
        if( query.valueEqualTo != null || query.valueContains != null ) {
            log.debug("Selecting from tag-value");
            SelectQuery valueQuery = jooq.select(TAG_VALUE.TAGID).from(TAG_VALUE).getQuery();
            if( query.valueEqualTo != null && query.valueEqualTo.length() > 0 ) {
                valueQuery.addConditions(TAG_VALUE.VALUE.equal(query.valueEqualTo));
            }
            if( query.valueContains != null  && query.valueContains.length() > 0 ) {
                valueQuery.addConditions(TAG_VALUE.VALUE.contains(query.valueContains));
            }
            sql = jooq.select().from(TAG).getQuery();
            sql.addConditions(TAG.ID.in(valueQuery));
        }
        else {
            log.debug("Selecting from tag");
         sql = jooq.select().from(TAG).getQuery();
            
        }
        log.debug("Adding tag conditions");
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(TAG.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.nameEqualTo != null  && query.nameEqualTo.length() > 0 ) {
            sql.addConditions(TAG.NAME.equal(query.nameEqualTo));
        }
        if( query.nameContains != null  && query.nameContains.length() > 0 ) {
            sql.addConditions(TAG.NAME.contains(query.nameContains));
        }
        if( query.oidEqualTo != null  && query.oidEqualTo.length() > 0 ) {
            sql.addConditions(TAG.OID.equal(query.oidEqualTo));
        }
        if( query.oidStartsWith != null  && query.oidStartsWith.length() > 0 ) {
            sql.addConditions(TAG.OID.like(query.oidStartsWith+"%"));
        }
        log.debug("Opening tag-value dao");
        // XXX PERFORMANCE TODO probably can improve the way we are returning results... currently for each selected tag, we do a query to get all its values
        // this might be faster if we take the list of all selected tags and then do one query for all their values and then separate them (assign the set
        // of values applicable to each tag) here
        log.debug("Fetching records using JOOQ");
        Result<Record> result = sql.fetch();
        Tag[] tags = new Tag[result.size()];
        log.debug("Got {} records", tags.length);
        int i = 0;
        for(Record r : result) {
            tags[i] = new Tag();
            tags[i].setId(r.getValue(TAG.ID));
            log.debug("Got tag uuid: {}", r.getValue(TAG.UUID));
            tags[i].setUuid(UUID.valueOf(r.getValue(TAG.UUID)));
            tags[i].setName(r.getValue(TAG.NAME));
            tags[i].setOid(r.getValue(TAG.OID));
            //tags[i].setValues(null); // XXX TODO:   make a separate query to grab all the tag values ??
            List<TagValue> tagValues = tagValueDao.findByTagId(r.getValue(TAG.ID));
            ArrayList<String> values = new ArrayList<String>();
            for(TagValue tagValue : tagValues) { values.add(tagValue.getValue()); }
            tags[i].setValues(values);
            i++;
        }
        sql.close();
        log.debug("Closing tag-value dao");
        log.debug("Returning {} tags", tags.length);
        return tags;
    }
    
    
    /**
     * In order to allow cross-site requests (for example when you have the client html open from a local file)
     * we have to implement this options method on every resource that needs to accept such requests. 
     * 
     * Here is an example og entry of an options request by the browser:
2013-08-09      16:45:49        127.0.0.1       -       -       8080    OPTIONS /tags   -       405 487 0       174     http://localhost:8080   Mozilla/5.0 (Win
dows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36    -     * 
     * 
     * In this example, the origin is  http://localhost:8080/tags
     * 
     * When the client is a local HTML file, there will not be a referrer header.
     * 
     * Sample headers sent from prototype.js on Chrome:
Access-Control-Request-Headers:accept, x-prototype-version, origin, x-requested-with
Access-Control-Request-Method:GET
Origin:null
User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36
     * 
     * @param entity
     */
    /*
    @Options
    public void doOptions(Representation entity) {
//        log.debug("Origin: {}", getRequest().getOriginalRef().toString());
//        log.debug("Referer: {}", getRequest().getReferrerRef().toString());
        Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers"); 
        if (responseHeaders == null) { 
            responseHeaders = new Form(); 
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders); 
        } 
        responseHeaders.add("Access-Control-Allow-Origin", "*");  // XXX TODO SECURITY when we don't need to support cross-site requests any more, turn this off
        responseHeaders.add("Access-Control-Allow-Methods", "POST,GET,DELETE,PUT,PATCH,OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "accept, content-type, x-prototype-version, origin, x-requested-with"); 
        responseHeaders.add("Access-Control-Allow-Credentials", "false"); 
        responseHeaders.add("Access-Control-Max-Age", "60"); 
    }   */ 
}
