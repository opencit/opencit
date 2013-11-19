 /*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Selection;
import com.intel.mtwilson.atag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.dao.jdbi.SelectionTagValueDAO;
import com.intel.mtwilson.atag.model.SelectionTagValue;
import java.sql.SQLException;
import java.util.List;
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
    private SelectionTagValueDAO selectionTagValueDao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.selectionDao();
            selectionTagValueDao = Derby.selectionTagValueDao();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }


    @Override
    protected void doRelease() throws ResourceException {
        if( dao != null ) { dao.close(); }
        if( selectionTagValueDao != null ) {
            selectionTagValueDao.close();
        }
        super.doRelease();
    }
    
    @Get("xml")
    public String existingSelectionXml(){
        String uuid = getAttribute("id");
        Selection selection = dao.findByUuid(UUID.valueOf(uuid));
        if( selection == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        List<SelectionTagValue> selectionTagValues = selectionTagValueDao.findBySelectionIdWithValues(selection.getId());
        if( selectionTagValues == null || selectionTagValues.isEmpty() ) {
            log.error("No tags in selection");
        }else {
            selection.setTags(selectionTagValues);
        }
        StringBuilder str = new StringBuilder();
        str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                     "<selections xmlns=\"urn:intel-mtwilson-asset-tag-attribute-selections\">\n"+
                     "<selection>");
        if(selection.getTags() != null) {
            for(SelectionTagValue tag : selection.getTags()) {
                str.append("<attribute oid=\""+ tag.getTagOid() +"\">" + tag.getTagValue() + "</attribute>\n");
            }
        }
        str.append("</selection>\n</selections>");
        return str.toString();
    }
    
    @Get("json")
    public Selection existingSelection() {
        String uuid = getAttribute("id");
        Selection selection = dao.findByUuid(UUID.valueOf(uuid));
        if( selection == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        List<SelectionTagValue> selectionTagValues = selectionTagValueDao.findBySelectionIdWithValues(selection.getId());
        if( selectionTagValues == null || selectionTagValues.isEmpty() ) {
            log.error("No tags in selection");
        }else {
            selection.setTags(selectionTagValues);
        }
        return selection;
    }

    @Delete
    public void deleteSelection() {
        String uuid = getAttribute("id");
        Selection selection = dao.findByUuid(UUID.valueOf(uuid));
        if( selection == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
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
        if( existingSelection == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        dao.update(existingSelection.getId(), updatedSelection.getSubject(), updatedSelection.getPredicate(), updatedSelection.getObject());
        return updatedSelection;
    }
    */
}
