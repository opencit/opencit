/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.resource;

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
import com.intel.mtwilson.jaxrs2.Patch;
import com.intel.mtwilson.jaxrs2.PatchLink;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
//@Stateless
//@Path("/hosts")
public abstract class AbstractResource<T extends Document, C extends DocumentCollection<T>, F extends FilterCriteria<T>, L extends PatchLink<T>> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractResource.class);

    /**
     * Given criteria encapsulated in a POJO, returns a collection of items
     * matching the criteria.
     *
     * @param criteria
     * @return
     */
    protected abstract C search(F criteria);

    /**
     * Given an item identifier, returns the specified item if it exists or null
     * if the item doesn't exist.
     * 
     * Subclass is responsible for validating the id in whatever manner it needs to.
     * Most will return null if !UUID.isValid(id)  but we don't do it here because
     * a resource might want to allow using something other than uuid as the url
     * key, for example a Host resource might accept uuid OR hostname as {id}
     *
     * @param id
     * @return
     */
    protected abstract T retrieve(String id);

    /**
     * Given an item instance, stores the item into permanent storage replacing
     * any existing item with the same id.
     *
     * Pre-Condition: Item should already exist.
     *
     * The item will already have a client-provided or auto-generated UUID.
     *
     * @param id
     * @param item
     */
    protected abstract void store(T item);

    /**
     * Creates a new instance and stores it in the DB.
     *
     * The item will already have a client-provided or auto-generated UUID.
     *
     * @param id
     * @param item
     */
    protected abstract void create(T item);

    /**
     * Given an item identifier, delete the corresponding item from permanent
     * storage.
     *
     * If the item does not already exist then no action should be taken - it is
     * not an error to request deletion of an non-existent item.
     *
     * @param id
     */
    protected abstract void delete(String id);

//    protected abstract F createFilterCriteriaWithId(String id); // hopefully just a temporary helper so we can make the json api work ok with our generics; looking for another way to do this but at least the implementations for this will be really easy 2-liners

    protected abstract C createEmptyCollection(); 
    
    /**
     * Search for items. Input Content-Type is not applicable because GET
     * requests do not have a request body. Output Content-Type is any of
     * application/json, application/xml, application/yaml, or text/yaml
     * depending on the client's Accept header.
     *
     * The result is a collection of items, even if only one item was found. If
     * no items were found the result is an empty collection.
     *
     * This is the only method where the result for application/vnd.api+json is
     * equivalent to the result for application/json.
     *
     * @param criteria
     * @return
     */
    /*
     @GET
     @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML, OtherMediaType.APPLICATION_YAML, OtherMediaType.TEXT_YAML})
     public List<T> searchList(@BeanParam F criteria) {
     log.debug("searchCollection");
     C collection = search(criteria);
     return collection.getDocuments();
     }*/
    /**
     * Search for items. Input Content-Type is not applicable because GET
     * requests do not have a request body. Output Content-Type is any of
     * application/vnd.api+json, application/json, application/xml,
     * application/yaml, or text/yaml
     *
     * depending on the client's Accept header.
     *
     * The result is a collection of items, even if only one item was found. If
     * no items were found the result is an empty collection.
     *
     * This is the only method where the result for application/vnd.api+json is
     * equivalent to the result for application/json.
     *
     * @param criteria
     * @return S
     */
    @GET
//    @Produces({OtherMediaType.APPLICATION_VND_API_JSON})
    @Produces({DataMediaType.APPLICATION_VND_API_JSON, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public C searchCollection(@BeanParam F criteria) {
        log.debug("searchCollection");
        ValidationUtil.validate(criteria); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return search(criteria);
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public T createOne(T item) {
        log.debug("create");
        ValidationUtil.validate(item); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        if (item.getId() == null) {
            item.setId(new UUID());
        }
        create(item);
        return item;
    }

    // the delete method is on a specific resource id and because we don't return any content it's the same whether its simple object or json api 
    // jersey automatically returns status code 204 No Content (successful) to the client because
    // we have a void return type
    @Path("/{id}")
    @DELETE
    public void deleteOne(@PathParam("id") String id) {
        log.debug("delete");
        T item = retrieve(id); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        delete(id);
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public T retrieveOne(@PathParam("id") String id) {
        log.debug("retrieve");
        T item = retrieve(id); // subclass is responsible for validating the id in whatever manner it needs to;  most will return null if !UUID.isValid(id)  but we don't do it here because a resource might want to allow using something other than uuid as the url key, for example uuid OR hostname for hosts
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        return item;
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public T storeOne(@PathParam("id") String id, T item) {
        log.debug("store");
        ValidationUtil.validate(item);
        item.setId(UUID.valueOf(id));
        T existing = retrieve(id); // subclass is responsible for validating id
        if (existing == null) {
            create(item);
        } else {
            store(item);
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public T patchOne(@PathParam("id") String id, Patch<T, F, L>[] patchArray) {
        log.debug("patch");
        T item = retrieve(id); // subclass is responsible for validating id
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
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

    ///////////////////////////       JSON API      ///////////////////////////
    /**
     * Add an item to the collection. Input Content-Type is
     * application/vnd.api+json Output Content-Type is application/vnd.api+json
     *
     * The input must represent a collection of items to add, even if the
     * collection only contains a single item.
     *
     *
     * @param hosts
     * @return
     */
    @POST
    @Consumes({DataMediaType.APPLICATION_VND_API_JSON})
    @Produces({DataMediaType.APPLICATION_VND_API_JSON})
    public C createCollection(C collection) {
        log.debug("createCollection");
        ValidationUtil.validate(collection);
        // this behavior of autmoatically generating uuids if client didn't provide could be implemented in one place and reused in all create() methods...  the utility could accept a DocumentCollection and set the ids... 
        for (T item : collection.getDocuments()) {
            if (item.getId() == null) {
                item.setId(new UUID());
            }
            create(item); 
        }
        return collection;
    }

    /**
     * Retrieve an item from the collection. Input Content-Type is not
     * applicable. Output Content-Type is application/vnd.api+json
     *
     * The output item is always wrapped in a collection.
     *
     * @param id
     * @return
     */
    @Path("/{id}")
    @GET
    @Produces({DataMediaType.APPLICATION_VND_API_JSON})
    public C retrieveCollection(@PathParam("id") String id) { // misnomer, what we really mean is "retrieve one but wrapped ina  collection for jsonapi"
        log.debug("retrieveCollection");
        T item = retrieve(id); // subclass is responsible for validating id
        if (item == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND); 
        }
        C collection = createEmptyCollection();
        collection.getDocuments().add(item);
        return collection;
    }

    /**
     * Replace an item in the collection. Input Content-Type is
     * application/vnd.api+json Output Content-Type is application/vnd.api+json
     *
     * The input item must be wrapped in a collection. The output item is always
     * wrapped in a collection.
     *
     * @param id
     * @param hostCollection
     * @return
     */
    @Path("/{id}")
    @PUT
    @Consumes(DataMediaType.APPLICATION_VND_API_JSON)
    @Produces(DataMediaType.APPLICATION_VND_API_JSON)
    public C storeCollection(@PathParam("id") String id, C collection) {// misnomer, what we really mean is "store one but wrapped ina  collection for jsonapi"
        log.debug("storeCollection");
        ValidationUtil.validate(collection);
        List<T> list = collection.getDocuments();
        if (list == null || list.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST); 
        }
        T item = list.get(0);
        if (item == null) {
            create(item);
        } else {
            store(item);
        }
        return collection;
    }

    /**
     * Update an item in the collection. Input Content-Type is
     * application/vnd.api+json Output Content-Type is application/vnd.api+json
     *
     * The input is a JSON PATCH document. There are restrictions on what
     * operations are allowed because the back-end storage is a database schema
     * not a JSON document. The output is the modified item wrapped in a
     * collection.
     *
     * @param id
     * @return
     */
    @Path("/{id}")
    @PATCH
    @Consumes(DataMediaType.APPLICATION_RELATIONAL_PATCH_JSON)
    @Produces(DataMediaType.APPLICATION_VND_API_JSON)
    public C patchCollection(@PathParam("id") String id /*, PatchDocumentCollection patch */) {
        log.debug("patchCollection");
//        HostFilterCriteria criteria = new HostFilterCriteria();
//        criteria.id = UUID.valueOf(id);
//        return searchCollection(criteria);
        return null;
    }
    
    /*
    private void validate(Object input) {
        try {
            ValidationUtil.validate(input);
        }
        catch(Exception e) {
            throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());            
        }
    }
    */
}
