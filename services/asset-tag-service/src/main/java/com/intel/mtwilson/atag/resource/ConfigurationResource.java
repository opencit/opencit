/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.Global;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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
    public Configuration existingConfigurationJson() {
        String uuidOrName = getAttribute("id");
        Configuration configuration;
        if( UUID.isValid(uuidOrName) ) {
            log.debug("Loading configuration by UUID: {}", uuidOrName);
            UUID uuid = UUID.valueOf(uuidOrName);
            configuration = dao.findByUuid(uuid);            
        }
        else {
            log.debug("Loading configuration by name: {}", uuidOrName);
            configuration = dao.findByName(uuidOrName);
        }        
        if( configuration == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return configuration;
    }
    
    @Get("xml")
    public String existingConfigurationXml() throws IOException {
        String uuidOrName = getAttribute("id");
        Configuration configuration;
        if( UUID.isValid(uuidOrName) ) {
            log.debug("Loading xml configuration by UUID: {}", uuidOrName);
            UUID uuid = UUID.valueOf(uuidOrName);
            configuration = dao.findByUuid(uuid);            
        }
        else {
            log.debug("Loading xml configuration by name: {}", uuidOrName);
            configuration = dao.findByName(uuidOrName);
        }        
        if( configuration == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return configuration.getXmlContent();        
    }

    @Delete
    public void deleteConfiguration() {
        String uuidOrName = getAttribute("id");
        Configuration configuration;
        if( UUID.isValid(uuidOrName) ) {
            log.debug("Loading configuration by UUID: {}", uuidOrName);
            UUID uuid = UUID.valueOf(uuidOrName);
            configuration = dao.findByUuid(uuid);            
        }
        else {
            log.debug("Loading configuration by name: {}", uuidOrName);
            configuration = dao.findByName(uuidOrName);
        }        
        if( configuration == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        dao.delete(configuration.getId());
        setStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Put("json:json") // previously was: text/plain
    public Configuration updateConfigurationJson(Configuration updatedConfiguration) throws SQLException, IOException {
        String uuidOrName = getAttribute("id");
        Configuration existingConfiguration;
        if( updatedConfiguration == null ) {
            log.debug("Updated configuration is required");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }
        if( UUID.isValid(uuidOrName) ) {
            log.debug("Loading configuration by UUID: {}", uuidOrName);
            UUID uuid = UUID.valueOf(uuidOrName);
            existingConfiguration = dao.findByUuid(uuid);            
        }
        else {
            log.debug("Loading configuration by name: {}", uuidOrName);
            existingConfiguration = dao.findByName(uuidOrName);
        }        
        if( existingConfiguration == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        log.debug("name null? {}", updatedConfiguration.getName() == null ? "yes" : "no");
//        log.debug("content type null? {}", updatedConfiguration.getContentType() == null ? "yes" : "no");
        log.debug("content null? {}", updatedConfiguration.getContent() == null ? "yes" : "no");
        dao.update(existingConfiguration.getId(), updatedConfiguration.getName(), updatedConfiguration.getXmlContent());
        Global.reset(); // new configuration will take effect next time it is needed (if it's the active one)
        return updatedConfiguration;
    }
    
    /**
     * XXX TODO BUG currently trying a PUT to this method with some xml results in "unsupported media type" (http error 415).
     * not sure why that's happening. 
     * 
     * Note: we don't need to return the updated configuration when a client sends PUT with xml because if we return
     * a success code like 204 they know that it is fine.  Only certain JSON clients like to get their document back
     * in the response.
     * 
     * @param updatedConfigurationXml
     * @throws SQLException
     * @throws IOException 
     */
//    @Put("xml:xml") // previously was: text/plain
    @Put("xml")
    public void updateConfigurationXml(Representation updatedConfigurationXml) throws SQLException, IOException {
        String uuidOrName = getAttribute("id");
        Configuration existingConfiguration;
        if( updatedConfigurationXml == null ) {
            log.debug("Updated configuration is required");
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return; // null;
        }
        if( UUID.isValid(uuidOrName) ) {
            log.debug("Loading xml configuration by UUID: {}", uuidOrName);
            UUID uuid = UUID.valueOf(uuidOrName);
            existingConfiguration = dao.findByUuid(uuid);            
        }
        else {
            log.debug("Loading xml configuration by name: {}", uuidOrName);
            existingConfiguration = dao.findByName(uuidOrName);
        }        
        if( existingConfiguration == null ) {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return; // null;
        }
        String xml = IOUtils.toString(updatedConfigurationXml.getStream());
        updatedConfigurationXml.exhaust();
        updatedConfigurationXml.release();
//        String xml = updatedConfigurationXml;
        dao.update(existingConfiguration.getId(), existingConfiguration.getName(), xml);
        Global.reset(); // new configuration will take effect next time it is needed (if it's the active one)
//        Representation response = new StringRepresentation(xml, MediaType.APPLICATION_XML);
//        return response;
    }    
}
