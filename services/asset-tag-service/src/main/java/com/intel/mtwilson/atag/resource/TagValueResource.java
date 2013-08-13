/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.model.TagValue;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.Derby;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class TagValueResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private TagDAO dao = null;
    private TagValueDAO tagValueDao = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.tagDao();
            tagValueDao = Derby.tagValueDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( dao != null ) { dao.close(); }
        if( tagValueDao != null ) { tagValueDao.close(); }
        super.doRelease();
    }
    
    @Post("json:json")
//    public List<TagValue> addTagValues(String[] values) throws SQLException, ResourceException {
    public String[] addTagValues(String[] values) throws SQLException, ResourceException {
        String uuid = getAttribute("id"); // the tag uuid
        // first look up the tag
//        log.debug("looking up tag {}", uuid);
//        log.debug("incoming values length is {}", values.length);
        Tag tag = dao.findByUuid(UUID.valueOf(uuid));
        if( tag == null ) { throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Cannot find tag"); }
        if( values != null && values.length > 0 ) {
            tagValueDao.insert(tag.getId(), Arrays.asList(values));
            List<TagValue> updated = tagValueDao.findByTagId(tag.getId());
            log.debug("Got {} total values now", updated.size());
            String[] updatedValues = new String[updated.size()];
            for(int i=0; i<updatedValues.length; i++) {
                updatedValues[i] = updated.get(i).getValue();
            }
            return updatedValues;
        }
//        log.debug("values length is {} so exiting without changes", values.length);
        // XXX TODO what should we return from here? maybe nothing? just a link to the tag ?
        setStatus(Status.SUCCESS_CREATED);
        return new String[0];
//        tag.setId(tagId);
//        return Collections.EMPTY_LIST; // new ArrayList<TagValue>(); // nothing was updated, return empty list
    }

    @Put("json:json")
//    public List<TagValue> addTagValues(String[] values) throws SQLException, ResourceException {
    public String[] setTagValues(String[] values) throws SQLException, ResourceException {
        String uuid = getAttribute("id"); // the tag uuid
        // first look up the tag
//        log.debug("looking up tag {}", uuid);
//        log.debug("incoming values length is {}", values.length);
        Tag tag = dao.findByUuid(UUID.valueOf(uuid));
        if( tag == null ) { throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Cannot find tag"); }
        if( values != null  ) {
            // delete all existing tag values, replace with specified list
            tagValueDao.deleteAll(tag.getId());
            tagValueDao.insert(tag.getId(), Arrays.asList(values));
            List<TagValue> updated = tagValueDao.findByTagId(tag.getId());
            log.debug("Got {} total values now", updated.size());
            String[] updatedValues = new String[updated.size()];
            for(int i=0; i<updatedValues.length; i++) {
                updatedValues[i] = updated.get(i).getValue();
            }
            return updatedValues;
        }
//        log.debug("values length is {} so exiting without changes", values.length);
        // XXX TODO what do we return here?  nothing?  link to tag resource?
        setStatus(Status.SUCCESS_OK);
        return new String[0];
//        tag.setId(tagId);
//        return Collections.EMPTY_LIST; // new ArrayList<TagValue>(); // nothing was updated, return empty list
    }
    
}
