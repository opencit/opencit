/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.File;
import com.intel.mtwilson.atag.dao.jdbi.FileDAO;
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
public class FileResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private FileDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.fileDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( dao != null ) { dao.close(); }
        super.doRelease();
    }
    
    @Get("json")
    public File existingFile() {
        String uuidOrName = getAttribute("id");
        File file;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            file = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's name
            file = dao.findByName(uuidOrName);
        }        
        if( file == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return file;
    }

    @Delete
    public void deleteFile() {
        String uuidOrName = getAttribute("id");
        File file;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            file = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's name
            file = dao.findByName(uuidOrName);
        }        
        if( file == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        dao.delete(file.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Put("json") // previously was: text/plain
    public File updateFile(File updatedFile) throws SQLException {
        String uuidOrName = getAttribute("id");
        File existingFile;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            existingFile = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's name
            existingFile = dao.findByName(uuidOrName);
        }
        if( existingFile == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        if( updatedFile == null ) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        dao.update(existingFile.getId(), updatedFile.getName(), updatedFile.getContentType(), updatedFile.getContent());
        return updatedFile;
    }
}
