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
import com.intel.mtwilson.as.rest.v2.model.FileLocator;
import com.intel.mtwilson.as.rest.v2.repository.FileRepository;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
//import javax.ejb.Stateless;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("/files")
public class Files extends AbstractJsonapiResource<File,FileCollection,FileFilterCriteria,NoLinks<File>,FileLocator> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
    private FileRepository repository;
    
    public Files() {
//        super();
        repository = new FileRepository();
//        setRepository(repository);
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
        File file = repository.retrieveWithContent(locator); // XXX  don't call retrieve(id) because it won't return the content... use a separate SQL  select contentType,content from file where id=?    to get the contenttype and content for this function.
        if (file == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        if( file.getContent() == null ) {
            return Response.noContent().build();
        }
        return Response.ok(file.getContent().getBytes() /* TODO  probably File model should have byte[] content  not String content! */, file.getContentType()).build();
    }
    
}
