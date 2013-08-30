/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.RdfTriple;
import com.intel.mtwilson.atag.dao.jdbi.RdfTripleDAO;
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
public class RdfTripleResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private RdfTripleDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.rdfTripleDao();
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
    public RdfTriple existingRdfTriple() {
        String uuid = getAttribute("id");
        RdfTriple rdf = dao.findByUuid(UUID.valueOf(uuid));
        if( rdf == null ) {            
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return rdf;
    }

    @Delete
    public void deleteRdfTriple() {
        String uuid = getAttribute("id");
        RdfTriple rdf = dao.findByUuid(UUID.valueOf(uuid));
        if( rdf == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        dao.delete(rdf.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Put("json") // previously was: text/plain
    public RdfTriple updateRdfTriple(RdfTriple updatedTriple) throws SQLException {
        String uuid = getAttribute("id");
        RdfTriple existingRdfTriple = dao.findByUuid(UUID.valueOf(uuid));
        if( existingRdfTriple == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        dao.update(existingRdfTriple.getId(), updatedTriple.getSubject(), updatedTriple.getPredicate(), updatedTriple.getObject());
        return updatedTriple;
    }
}
