/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.file.resource;

import com.intel.mtwilson.v2.file.model.*;
import com.intel.mtwilson.jaxrs2.NoLinks;
import com.intel.mtwilson.jaxrs2.server.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
@V2
//@Stateless
@Path("/host-files")
public class Files extends AbstractJsonapiResource<File,FileCollection,FileFilterCriteria,NoLinks<File>,FileLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
    private FileRepository repository;
    
    public Files() {
        repository = new FileRepository();
    }
    
    @Override
    protected FileRepository getRepository() { return repository; }
    
    @Override
    protected FileCollection createEmptyCollection() {
        return new FileCollection();
    }
    
    /**
     * Returns the raw content of the file, using its own content type
     * which was used to upload it. 
     * 
     * @param id
     * @return 
     */
    @Path("/{id}/content")
    @GET
    @Produces(MediaType.WILDCARD)
    public Response retrieveFileContent(@BeanParam FileLocator locator) {
        log.debug("retrieve file content");
        File file = repository.retrieveWithContent(locator); 
        if (file == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        if( file.getContent() == null ) {
            return Response.noContent().build();
        }
        return Response.ok(file.getContent().getBytes() , file.getContentType()).build();
    }
    
}
