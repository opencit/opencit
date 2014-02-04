/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.mtwilson.as.rest.v2.resource.*;
import com.intel.mtwilson.as.rest.v2.model.File;
import com.intel.mtwilson.as.rest.v2.model.FileFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.FileCollection;
import com.intel.mtwilson.as.rest.v2.model.FileLocator;
import com.intel.mtwilson.jersey.resource.SimpleRepository;

/**
 *
 * @author jbuhacoff
 */
public class FileRepository implements SimpleRepository<File,FileCollection,FileFilterCriteria,FileLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    private ObjectMapper mapper;
    
    public FileRepository() {
         mapper = new ObjectMapper();
         mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }
    
    @Override
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
        file.setContent(null); // XXX the search query should NOT include the content; search is only to show "search results" from which the client can select a file to download using /files/{id}/content 
        files.getFiles().add(file);
        return files;
    }

    @Override
    public File retrieve(FileLocator locator) {
//        String id = locator.id;
        log.debug("File retrieve: {}", locator.id);
//        if( !UUID.isValid(id) ) { return null; } // XXX TODO or a localizable input validation error via throw exception (if we don't validate here, UUID.valueOf() will throw IllegalArgumentException if it's not a valid uuid)
        File file = new File();
//        file.setId(UUID.valueOf(id));
        file.setId(locator.id);
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent(null); // XXX  content should be linked from the links section;  meaning our select 1 query should NOT return the content to us from the db, that would be wasteful 
        file.getMeta().put("content_href", String.format("/files/%s/content", locator.id.toString())); // XXX  or meta.links = [ { rel: content, href: ... } ]  or links.content = "/files/{id}/content"  but nothing in the "linked" section 
        return file;
    }
    
    public File retrieveWithContent(FileLocator locator) {
//        String id = locator.id;
        log.debug("File retrieve: {}", locator.id);
//        if( !UUID.isValid(id) ) { return null; } // XXX TODO or a localizable input validation error via throw exception (if we don't validate here, UUID.valueOf() will throw IllegalArgumentException if it's not a valid uuid)
        File file = new File();
//        file.setId(UUID.valueOf(id));
        file.setId(locator.id);
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent("simulated content here"); // XXX  content should be linked from the links section;  meaning our select 1 query should NOT return the content to us from the db, that would be wasteful 
        file.getMeta().put("content_href", String.format("/files/%s/content", locator.id.toString())); // XXX  or meta.links = [ { rel: content, href: ... } ]  or links.content = "/files/{id}/content"  but nothing in the "linked" section 
        return file;        
    }

    @Override
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
    public void delete(FileLocator locator) {
        log.debug("File delete: {}", locator.id.toString());
    }

    @Override
    public void delete(FileFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
