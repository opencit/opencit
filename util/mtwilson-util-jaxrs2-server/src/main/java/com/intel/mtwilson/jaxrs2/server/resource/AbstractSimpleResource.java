/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.jaxrs2.server.PATCH;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.Faults;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.jaxrs2.AbstractDocument;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.BeanParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
public abstract class AbstractSimpleResource<T extends AbstractDocument, C extends DocumentCollection<T>, F extends FilterCriteria<T>, P extends PatchLink<T>, L extends Locator<T>> implements Faults {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSimpleResource.class);
    private ObjectMapper mapper = new ObjectMapper(); // for debugging only
    
    private ArrayList<Fault> faults = new ArrayList<>();

    @Override
    public List<Fault> getFaults() {
        return faults;
    }
    
    /**
     * Add a fault to the list of faults that will be returned with an error status
     * @param fault 
     */
    protected void fault(Fault fault) {
        faults.add(fault);
    }
    
/*
    private SimpleRepository<T,C,F,L> repository;
    
    public void setRepository(SimpleRepository<T,C,F,L> repository) {
        this.repository = repository;
    } 
    
    protected SimpleRepository<T,C,F,L> getRepository() { return repository; }
    */
    protected abstract DocumentRepository<T,C,F,L> getRepository();
    
    @GET
    public C searchCollection(@BeanParam F selector) {
        try { log.debug("searchCollection: {}", mapper.writeValueAsString(selector)); } catch(JsonProcessingException e) { log.debug("searchCollection: cannot serialize selector: {}", e.getMessage()); }
        ValidationUtil.validate(selector); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return getRepository().search(selector);
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
    public T createOne(@BeanParam L locator, T item, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        try { log.debug("createOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("createOne: cannot serialize locator: {}", e.getMessage()); }
        locator.copyTo(item);
        ValidationUtil.validate(item); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        if (item.getId() == null) {
            item.setId(new UUID());
        }
        getRepository().create(item);
        httpServletResponse.setStatus(Status.CREATED.getStatusCode());
        return item;
    }

    // the delete method is on a specific resource id and because we don't return any content it's the same whether its simple object or json api 
    // jersey automatically returns status code 204 No Content (successful) to the client because
    // we have a void return type
    @Path("/{id}")
    @DELETE
    public void deleteOne(@BeanParam L locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
        try { log.debug("deleteOne: {}", mapper.writeValueAsString(locator)); } catch(JsonProcessingException e) { log.debug("deleteOne: cannot serialize locator: {}", e.getMessage()); }
        T item = getRepository().retrieve(locator); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        getRepository().delete(locator);
        httpServletResponse.setStatus(Status.NO_CONTENT.getStatusCode());
        /*
        T item = getRepository().retrieve(id); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        getRepository().delete(id);*/
                /*
//        C collection = getRepository().search(selector);
//        if( collection.getDocuments().isEmpty() ) {            
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
//        }
//        T item = collection.getDocuments().get(0);
        
//        getRepository().delete(item.getId().toString());
* */
    }
    
    @DELETE
    public void deleteCollection(@BeanParam F selector) {
        try { log.debug("deleteCollection: {}", mapper.writeValueAsString(selector)); } catch(JsonProcessingException e) { log.debug("deleteCollection: cannot serialize selector: {}", e.getMessage()); }
        C collection = getRepository().search(selector);
        if( collection.getDocuments().isEmpty() ) {            
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        // Do the delete here after search
        getRepository().delete(selector);
        /*for(T item : collection.getDocuments()) {
            getRepository().delete();
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
    public T retrieveOne(@BeanParam L locator, @Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse) {
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
            httpServletResponse.setStatus(Status.NOT_FOUND.getStatusCode());
            return null;
//            throw new WebApplicationException(Response.Status.NOT_FOUND); 
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
