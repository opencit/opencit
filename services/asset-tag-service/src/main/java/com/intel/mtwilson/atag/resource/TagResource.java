/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Tag;
import com.intel.mtwilson.atag.dao.jdbi.TagDAO;
import com.intel.mtwilson.atag.dao.jdbi.TagValueDAO;
import com.intel.mtwilson.atag.dao.Derby;
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
        String uuidOrName = getAttribute("id");
        Tag tag;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            tag = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's an oid or a name
            tag = dao.findByOidOrName(uuidOrName, uuidOrName);
        }        
        if( tag == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }        
        return tag;
    }

    @Delete
    public void deleteTag() {
        String uuidOrName = getAttribute("id");
        Tag tag;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            tag = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's an oid or a name
            tag = dao.findByOidOrName(uuidOrName, uuidOrName);
        }
        if( tag == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        dao.delete(tag.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Put("json") // previously was: text/plain
    public Tag updateTag(Tag updatedTag) throws SQLException {
        String uuidOrName = getAttribute("id");
        Tag existingTag;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            existingTag = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's an oid or a name
            existingTag = dao.findByOidOrName(uuidOrName, uuidOrName);
        }        
        if( existingTag == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        dao.update(existingTag.getId(), updatedTag.getName(), updatedTag.getOid());
        if( updatedTag.getValues() != null ) {
            tagValueDao.deleteAll(existingTag.getId());
            tagValueDao.insert(existingTag.getId(), updatedTag.getValues());
        }
        return updatedTag;
    }
}
