/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.dao.jdbi.ConfigurationDAO;
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
public class ConfigurationResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    private ConfigurationDAO dao = null;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.configurationDao();
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
    public Configuration existingConfiguration() {
        String uuidOrName = getAttribute("id");
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            return dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's name
            return dao.findByName(uuidOrName);
        }        
    }

    @Delete
    public void deleteConfiguration() {
        String uuidOrName = getAttribute("id");
        Configuration configuration;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            configuration = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's name
            configuration = dao.findByName(uuidOrName);
        }        
        dao.delete(configuration.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Put("json") // previously was: text/plain
    public Configuration updateConfiguration(Configuration updatedConfiguration) throws SQLException {
        String uuidOrName = getAttribute("id");
        Configuration existingConfiguration;
        try {
            UUID uuid = UUID.valueOf(uuidOrName);
            existingConfiguration = dao.findByUuid(uuid);
        }
        catch(Exception e) {
            // not a valid UUID - maybe it's name
            existingConfiguration = dao.findByName(uuidOrName);
        }
        if( existingConfiguration == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        log.debug("dao null? {}", dao == null ? "yes" : "no");
        log.debug("id ? {}", existingConfiguration.getId());
        log.debug("name null? {}", updatedConfiguration.getName() == null ? "yes" : "no");
        log.debug("content type null? {}", updatedConfiguration.getContentType() == null ? "yes" : "no");
        log.debug("content null? {}", updatedConfiguration.getContentText() == null ? "yes" : "no");
        dao.update(existingConfiguration.getId(), updatedConfiguration.getName(), updatedConfiguration.getContentType(), updatedConfiguration.getContentText());
        return updatedConfiguration;
    }
}
