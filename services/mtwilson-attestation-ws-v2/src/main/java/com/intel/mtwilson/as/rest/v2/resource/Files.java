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
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ejb.Stateless;
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
@Stateless
@Path("/files")
public class Files extends AbstractResource<File,FileCollection,FileFilterCriteria,NoLinks<File>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
    @Override
    protected FileCollection search(FileFilterCriteria criteria) {
        FileCollection files = new FileCollection();
        File file = new File();
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent(null); // XXX the search query should NOT include the content; search is only to show "search results" from which the client can select a file to download using /files/{id}/content 
        files.getFiles().add(file);
        return files;
    }

    @Override
    protected File retrieve(String id) {
        if( !UUID.isValid(id) ) { return null; } // XXX TODO or a localizable input validation error via throw exception (if we don't validate here, UUID.valueOf() will throw IllegalArgumentException if it's not a valid uuid)
        File file = new File();
        file.setId(UUID.valueOf(id));
        file.setName("testfile");
        file.setContentType("text/plain");
        file.setContent(null); // XXX  content should be linked from the links section;  meaning our select 1 query should NOT return the content to us from the db, that would be wasteful 
        file.getMeta().put("content_href", String.format("/files/%s/content", id)); // XXX  or meta.links = [ { rel: content, href: ... } ]  or links.content = "/files/{id}/content"  but nothing in the "linked" section 
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

    /*
    @Override
    protected FileFilterCriteria createFilterCriteriaWithId(String id) {
        FileFilterCriteria criteria = new FileFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    */
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
    public Response retrieveFileContent(@PathParam("id") String id) {
        log.debug("retrieve file content");
        File file = retrieve(id); // XXX  don't call retrieve(id) because it won't return the content... use a separate SQL  select contentType,content from file where id=?    to get the contenttype and content for this function.
        if (file == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        return Response.ok(null /* content as byte array */, file.getContentType()).build();
    }
    
}
