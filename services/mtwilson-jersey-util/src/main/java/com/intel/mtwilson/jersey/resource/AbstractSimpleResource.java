/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.jersey.http.OtherMediaType;
import com.intel.mtwilson.jersey.http.PATCH;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import com.intel.mtwilson.jersey.Document;
import com.intel.mtwilson.jersey.DocumentCollection;
import com.intel.mtwilson.jersey.FilterCriteria;
import com.intel.mtwilson.jersey.Locator;
import com.intel.mtwilson.jersey.Patch;
import com.intel.mtwilson.jersey.PatchLink;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.BeanParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Reference: https://jersey.java.net/documentation/latest/user-guide.html
 * https://jersey.java.net/documentation/latest/media.html
 * https://jersey.java.net/documentation/latest/deployment.html
 * https://wikis.oracle.com/display/Jersey/Overview+of+JAX-RS+1.0+Features
 * http://docs.oracle.com/cd/E19776-01/820-4867/6nga7f5o5/index.html
 *
 * When listing the media types in
 *
 * @Produces annotations, it's important to put application/json before
 * application/vnd.api+json so it will be chosen as the default if the browser
 * doesn't specify an Accept header. If application/json is first, accessing
 * /hosts will display the JSON output in the browser. If
 * application/vnd.api+json is first, the browser will not recognize that
 * content type (even though it has +json) and will download it as a file. The
 * .json file extension maps to application/json specifically. JSON API clients
 * must set the Accept header if they want to receive the output with
 * Content-Type: application/vnd.api+json
 *
 *
 * Example simple JSON output:
 *
 *
 * {"hosts":[{"id":"06285da4-e170-4322-a843-480f3a55feec","name":"hostabc","connection_url":"http://1.2.3.4","description":"test
 * host","bios_mle":"bios-4.3.2"}]}
 *
 * Example XML output:
 * * 
<host_collection><hosts><host><id>bd7094d2-2ed3-468e-9c16-40999f9e4b8c</id><name>hostabc</name><connectionUrl>http://1.2.3.4</connectionUrl><description>test
 * host</description><biosMLE>bios-4.3.2</biosMLE></host></hosts></host_collection>
 *
 * This abstract class defines the following HTTP interface:
 *
 * GET /collection -> application/vnd.api+json, application/json,
 * application/xml, application/yaml, text/yaml
 *
 * POST /collection Content-Type: application/json, application/xml,
 * application/yaml, text/yaml -> application/json, application/xml,
 * application/yaml, text/yaml
 *
 *
 *
 * @author jbuhacoff
 */
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
public abstract class AbstractSimpleResource<T extends Document, C extends DocumentCollection<T>, F extends FilterCriteria<T>, P extends PatchLink<T>, L extends Locator<T>> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSimpleResource.class);
    private ObjectMapper mapper = new ObjectMapper(); // for debugging only
    private SimpleRepository<T,C,F,L> repository;
    
    public void setRepository(SimpleRepository<T,C,F,L> repository) {
        this.repository = repository;
    } 
    
    protected SimpleRepository<T,C,F,L> getRepository() { return repository; }
    
    
    // TODO:   searchCollection  which @Produces   OtherMediaType.APPLICATION_VND_API_JSON  
    //       must be implemented in a subclass...
    @GET
    public C searchCollection(@BeanParam F selector) {
        try { log.debug("searchCollection: {}", mapper.writeValueAsString(selector)); } catch(JsonProcessingException e) { log.debug("searchCollection: cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(selector); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return repository.search(selector);
    }

    /**
     * Add an item to the collection. Input Content-Type is any of
     * application/json, application/xml, application/yaml, or text/yaml Output
     * Content-Type is any of application/json, application/xml,
     * application/yaml, or text/yaml
     *
     * The input must represent a single item NOT wrapped in a collection.
     *
     * @param item
     * @return
     */
    @POST
    public T createOne(@BeanParam L locator, T item) {
        try { log.debug("createOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize locator: {}", e.getMessage()); }
        locator.copyTo(item);
        ValidationUtil.validate(item); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        if (item.getId() == null) {
            item.setId(new UUID());
        }
        repository.create(item);
        return item;
    }

    // the delete method is on a specific resource id and because we don't return any content it's the same whether its simple object or json api 
    // jersey automatically returns status code 204 No Content (successful) to the client because
    // we have a void return type
    @Path("/{id}")
    @DELETE
    public void deleteOne(@BeanParam L locator) {
        try { log.debug("deleteOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize locator: {}", e.getMessage()); }
        T item = repository.retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        repository.delete(locator);
        /*
        T item = repository.retrieve(id); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        repository.delete(id);*/
                /*
//        C collection = repository.search(selector);
//        if( collection.getDocuments().isEmpty() ) {            
//            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
//        }
//        T item = collection.getDocuments().get(0);
        
//        repository.delete(item.getId().toString());
* */
    }
    
    @DELETE
    public void deleteCollection(@BeanParam F selector) {
        try { log.debug("deleteCollection: {}", mapper.writeValueAsString(selector)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize selector: {}", e.getMessage()); }
        C collection = repository.search(selector);
        if( collection.getDocuments().isEmpty() ) {            
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        // Do the delete here after search
        repository.delete(selector);
        /*for(T item : collection.getDocuments()) {
            // TODO:  multi-threaded or parallel or have a repository method for multi-delete where we collect all the id's and then send a list
            repository.delete();
        }*/
        
    }

    /**
     * Retrieve an item from the collection. Input Content-Type is not
     * applicable. Output Content-Type is any of application/json,
     * application/xml, application/yaml, or text/yaml
     *
     * The output represents a single item NOT wrapped in a collection.
     *
     * @param id
     * @return
     */
    @Path("/{id}")
    @GET
    public T retrieveOne(@BeanParam L locator) {
        try { log.debug("retrieveOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize locator: {}", e.getMessage()); }
        /*
        T item = repository.retrieve(id); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }*/
        /*
//        C collection = repository.search(selector);
//        if( collection.getDocuments().isEmpty() ) {            
//            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
//        }
//        T item = collection.getDocuments().get(0);
* */
        T existing = repository.retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (existing == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        return existing;
    }

    /**
     * Replace an item in the collection. Input Content-Type is any of
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
    @Path("/{id}")
    @PUT
    public T storeOne(@BeanParam L locator, T item) {
        try { log.debug("storeOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize locator: {}", e.getMessage()); }
        ValidationUtil.validate(item);
//        item.setId(UUID.valueOf(id));
        locator.copyTo(item);
        T existing = repository.retrieve(locator); // subclass is responsible for validating id
        if (existing == null) {
            repository.create(item);
        } else {
            repository.store(item);
        }

        return item;
    }

    // the patch method only accepts the patch content type (TODO - DEFINE IT  - actually yaml might be a good choice because it's compact, uses multiple lines for display, and has language features that can help deserialize into java easier than json or xml)  
    /**
     * Update an item in the collection. Input Content-Type is a special patch
     * document format. Output Content-Type is any of application/json,
     * application/xml, application/yaml, or text/yaml.
     *
     * The input is a patch format for a single item. The output is a single
     * item after applying the patch, NOT wrapped in a collection.
     *
     * @param id
     * @return
     */
    @Path("/{id}")
    @PATCH
    @Consumes({OtherMediaType.APPLICATION_RELATIONAL_PATCH_JSON})
    public T patchOne(@BeanParam L locator, Patch<T, F, P>[] patchArray) {
        try { log.debug("patchOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize locator: {}", e.getMessage()); }
        T item = repository.retrieve(locator); // subclass is responsible for validating id
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); // TODO i18n
        }
        locator.copyTo(item);
        ValidationUtil.validate(patchArray);
        for (int i = 0; i < patchArray.length; i++) {
            log.debug("Processing patch #{} of {}", i + 1, patchArray.length);
            // XXX TODO check if patchArray[i].getSelect() == null  (expected) , and if it's not null then use abstract method to check that id == id   (like have a method that takes a string id and a filtercriteria and decides if they refer to the same record)
            // XXX TODO need an abstract method for applying patches for subclasses
            if (false /* error during processing */) {
                // 400 bad request or 500 internal server error
                return null;
            }

        }
        // XXX TODO wire up to repository...
        // look it up first, update whtever fields are specified for update by the patch format, then issue updates...
        //return new Host();
//        return patch(null);
        return null; // XXX TODO return final item with changes
    }

}
