/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.as.rest.v2.model.File;
import com.intel.mtwilson.as.rest.v2.model.FileFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.FileCollection;
import com.intel.mtwilson.jersey.NoLinks;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.dcsg.cpg.io.UUID;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

/**
 *
 * @author jbuhacoff
 */
@Stateless
@Path("/v2/files")
public class Files extends AbstractResource<File,FileCollection,FileFilterCriteria,NoLinks<File>> {

    @Override
    protected FileCollection search(FileFilterCriteria criteria) {
        FileCollection files = new FileCollection();
        File file = new File();
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent("hello world");
        files.getFiles().add(file);
        return files;
    }

    @Override
    protected File retrieve(String id) {
        File file = new File();
        file.setId(UUID.valueOf(id));
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent("hello world");
        return file;
    }

    @Override
    protected void create(File item) {
        // store it...
    }
    @Override
    protected void store(File item) {
        // store it...
    }

    @Override
    protected void delete(String id) {
    }

    @Override
    protected FileFilterCriteria createFilterCriteriaWithId(String id) {
        FileFilterCriteria criteria = new FileFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    
}
