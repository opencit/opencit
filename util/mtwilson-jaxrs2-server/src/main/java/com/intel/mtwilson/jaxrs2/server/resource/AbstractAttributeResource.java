/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.jaxrs2.server.PATCH;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.repository.Locator;
import com.intel.mtwilson.jaxrs2.Patch;
import com.intel.mtwilson.jaxrs2.PatchLink;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.BeanParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * An attribute resource is an extended attribute of some other primary resource.
 * It does not have its own identifier - the identifier belongs to the primary
 * resource.
 * 
 * Attribute resources are singular - there can only be one.  Therefore
 * only retrieve and store methods are supported.   Search is not supported.
 * Delete and post are not supported either because an extended
 * attribute can only be get or set - not removed or created. 
 * 
 * Subclasses of AbstractAttributeResource should be annotated with
 * @Path("/primary-resource-name/{id}/attribute-name") where {id} refers
 * to the primary resource's id.
 * 
 * There cannot be a collection of extended attributes - a collection means
 * the client must be able to distinguish one item from another, which 
 * in turn means they must have their own identifiers. That would make them
 * not extended attributes but related resources and they should subclass
 * AbstractSimpleResource or AbstractJsonapiResource instead.
 * 
 *
 * @author jbuhacoff
 */
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
public abstract class AbstractAttributeResource<T extends Document, C extends DocumentCollection<T>, F extends FilterCriteria<T>, P extends PatchLink<T>, L extends Locator<T>> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractAttributeResource.class);
    private ObjectMapper mapper = new ObjectMapper(); // for debugging only
/*
    private SimpleRepository<T,C,F,L> repository;
    
    public void setRepository(SimpleRepository<T,C,F,L> repository) {
        this.repository = repository;
    } 
    
    protected SimpleRepository<T,C,F,L> getRepository() { return repository; }
    */
    protected abstract DocumentRepository<T,C,F,L> getRepository();
    

    /**
     * Retrieve the extended attribute.
     * Input Content-Type is not
     * applicable. Output Content-Type is any of application/json,
     * application/xml, application/yaml, or text/yaml
     *
     * The output represents a single item NOT wrapped in a collection.
     *
     * @param id
     * @return
     */
//    @Path("/{id}") //
    @GET
    public T retrieveOne(@BeanParam L locator) {
        try { log.debug("retrieveOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("retrieveOne: cannot serialize locator: {}", e.getMessage()); }
        /*
        T item = getRepository().retrieve(id); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }*/
        /*
//        C collection = getRepository().search(selector);
//        if( collection.getDocuments().isEmpty() ) {            
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
//        }
//        T item = collection.getDocuments().get(0);
* */
        T existing = getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (existing == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        return existing;
    }

    /**
     * Replace an extended attribute. 
     * Input Content-Type is any of
     * application/json, application/xml, application/yaml, or text/yaml Output
     * Content-Type is any of application/json, application/xml,
     * application/yaml, or text/yaml
     *
     * The input is a single item NOT wrapped in a collection. The output is the
     * single item NOT wrapped in a collection.
     *
     * @param id
     * @param item
     * @return
     */
//    @Path("/{id}")
    @PUT
    public T storeOne(@BeanParam L locator, T item) {
        try { log.debug("storeOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("storeOne: cannot serialize locator: {}", e.getMessage()); }
        ValidationUtil.validate(item);
//        item.setId(UUID.valueOf(id));
        locator.copyTo(item);
        T existing = getRepository().retrieve(locator); // subclass is responsible for validating id
        if (existing == null) {
            getRepository().create(item);
        } else {
            getRepository().store(item);
        }

        return item;
    }

    // the patch method only accepts the patch content type 
    /**
     * Update an extended attribute. Input Content-Type is a special patch
     * document format. Output Content-Type is any of application/json,
     * application/xml, application/yaml, or text/yaml.
     *
     * The input is a patch format for a single item. The output is a single
     * item after applying the patch, NOT wrapped in a collection.
     *
     * @param id
     * @return
     */
//    @Path("/{id}")
    @PATCH
    @Consumes({DataMediaType.APPLICATION_RELATIONAL_PATCH_JSON})
    public T patchOne(@BeanParam L locator, Patch<T, F, P>[] patchArray) {
        try { log.debug("patchOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("patchOne: cannot serialize locator: {}", e.getMessage()); }
        T item = getRepository().retrieve(locator); // subclass is responsible for validating id
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        locator.copyTo(item);
        ValidationUtil.validate(patchArray);
        for (int i = 0; i < patchArray.length; i++) {
            log.debug("Processing patch #{} of {}", i + 1, patchArray.length);
            if (false /* error during processing */) {
                // 400 bad request or 500 internal server error
                return null;
            }

        }
        //return new Host();
//        return patch(null);
        return null; 
    }

}
