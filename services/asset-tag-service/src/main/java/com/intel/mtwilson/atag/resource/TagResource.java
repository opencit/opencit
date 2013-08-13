/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.Derby;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.SQLException;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
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
public class TagResource extends ServerResource {
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
    
    @Get("json")
    public Tag existingTag() {
        String uuid = getAttribute("id");
        return dao.findByUuid(UUID.valueOf(uuid));
    }

    @Delete
    public void deleteTag() {
        String uuid = getAttribute("id");
        Tag tag = dao.findByUuid(UUID.valueOf(uuid));
        dao.delete(tag.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Put("json") // previously was: text/plain
    public Tag updateTag(Tag tag) throws SQLException {
        String uuid = getAttribute("id");
        Tag existingTag = dao.findByUuid(UUID.valueOf(uuid));
        dao.update(existingTag.getId(), tag.getName(), tag.getOid());
        if( tag.getValues() != null ) {
            tagValueDao.deleteAll(existingTag.getId());
            tagValueDao.insert(existingTag.getId(), tag.getValues());
        }
        return tag;
    }
}
