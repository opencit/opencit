/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Selection;
import com.intel.mtwilson.atag.dao.jdbi.SelectionDAO;
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
public class SelectionResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private SelectionDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.selectionDao();
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
    public Selection existingSelection() {
        String uuid = getAttribute("id");
        return dao.findByUuid(UUID.valueOf(uuid));
    }

    @Delete
    public void deleteSelection() {
        String uuid = getAttribute("id");
        Selection selection = dao.findByUuid(UUID.valueOf(uuid));
        dao.delete(selection.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    // XXX TODO:  update selected tags and update selected hosts (subjects)  ... for selections this is allowed.
    // XXX TODO:  also probably allow one-at-a-time add/remove of selected tags and selected hosts from a selection.
    //            for example the UI could use this to save changes as they are made and not require the user to hit a "save" button
    /*
    @Put("text/plain")
    public Selection updateSelection(Selection updatedSelection) throws SQLException {
        String uuid = getAttribute("id");
        Selection existingSelection = dao.findByUuid(UUID.valueOf(uuid));
        dao.update(existingSelection.getId(), updatedSelection.getSubject(), updatedSelection.getPredicate(), updatedSelection.getObject());
        return updatedSelection;
    }
    */
}
