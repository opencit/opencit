/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.file.resource;

import com.intel.mtwilson.v2.file.resource.Files;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.v2.file.model.*;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
public class FileRepository implements DocumentRepository<File,FileCollection,FileFilterCriteria,FileLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    private ObjectMapper mapper;
    
    public FileRepository() {
         mapper = new ObjectMapper();
         mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }
    
    @Override
    @RequiresPermissions("files:search")    
    public FileCollection search(FileFilterCriteria criteria) {
        try {
            log.debug("File search: {}", mapper.writeValueAsString(criteria));
        }
        catch(Exception e) {
            log.debug("Error - cannot log file search", e);
        }
        FileCollection files = new FileCollection();
        File file = new File();
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent(null); 
        files.getFiles().add(file);
        return files;
    }

    @Override
    @RequiresPermissions("files:retrieve")    
    public File retrieve(FileLocator locator) {
//        String id = locator.id;
        log.debug("File retrieve: {}", locator.id);
//        if( !UUID.isValid(id) ) { return null; } 
        File file = new File();
//        file.setId(UUID.valueOf(id));
        file.setId(locator.id);
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent(null); 
        file.getMeta().put("content_href", String.format("/files/%s/content", locator.id.toString())); 
        return file;
    }
    
    @RequiresPermissions("files:retrieve")    
    public File retrieveWithContent(FileLocator locator) {
//        String id = locator.id;
        log.debug("File retrieve: {}", locator.id);
//        if( !UUID.isValid(id) ) { return null; } 
        File file = new File();
//        file.setId(UUID.valueOf(id));
        file.setId(locator.id);
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent("simulated content here"); 
        file.getMeta().put("content_href", String.format("/files/%s/content", locator.id.toString())); 
        return file;        
    }

    @Override
    @RequiresPermissions("files:create")    
    public void create(File item) {
        try {
            log.debug("File create: {}", mapper.writeValueAsString(item));
        }
        catch(Exception e) {
            log.debug("Error - cannot log file create", e);
        }
        // store it...
    }

    @Override
    @RequiresPermissions("files:store")    
    public void store(File item) {
        try {
            log.debug("File store: {}", mapper.writeValueAsString(item));
        }
        catch(Exception e) {
            log.debug("Error - cannot log file store", e);
        }
        // store it...
    }

    @Override
    @RequiresPermissions("files:delete")    
    public void delete(FileLocator locator) {
        log.debug("File delete: {}", locator.id.toString());
    }

    @Override
    @RequiresPermissions("files:delete")    
    public void delete(FileFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
