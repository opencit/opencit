/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.resource;

import com.intel.mtwilson.jaxrs2.server.PATCH;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.jaxrs2.AbstractDocument;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.repository.Locator;
import com.intel.mtwilson.jaxrs2.PatchLink;
import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.BeanParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
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
 * This abstract class adds the following HTTP interface:
 *
 * GET /collection -> application/vnd.api+json
 *
 *
 * @author jbuhacoff
 */
public abstract class AbstractJsonapiResource<T extends AbstractDocument, C extends DocumentCollection<T>, F extends FilterCriteria<T>, P extends PatchLink<T>, L extends Locator<T>> extends AbstractSimpleResource<T,C,F,P,L> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractJsonapiResource.class);

    protected abstract C createEmptyCollection(); 
    /*
    public AbstractJsonapiResource() {
        super();
    }*/

    @GET
    @Produces(DataMediaType.APPLICATION_VND_API_JSON)
    public C searchJsonapiCollection(@BeanParam F criteria) {
        log.debug("searchJsonapiCollection");
        ValidationUtil.validate(criteria); // throw new MWException(e, ErrorCode.AS_INPUT_VALIDATION_ERROR, input, method.getName());
        return getRepository().search(criteria);
    }

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
    public C createJsonapiCollection(C collection) {
        log.debug("createCollection");
        ValidationUtil.validate(collection);
        // this behavior of autmoatically generating uuids if client didn't provide could be implemented in one place and reused in all create() methods...  the utility could accept a DocumentCollection and set the ids... 
        for (T item : collection.getDocuments()) {
            if (item.getId() == null) {
                item.setId(new UUID());
            }
            getRepository().create(item); 
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
    public C retrieveJsonapiCollection(@BeanParam L locator) { // misnomer, what we really mean is "retrieve one but wrapped ina  collection for jsonapi"
        log.debug("retrieveCollection");
        T item = getRepository().retrieve(locator); // subclass is responsible for validating id
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
    public C storeJsonapiCollection(@BeanParam L locator, C collection) {// misnomer, what we really mean is "store one but wrapped ina  collection for jsonapi"
        log.debug("storeCollection");
        ValidationUtil.validate(collection);
        List<T> list = collection.getDocuments();
        if (list == null || list.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST); 
        }
        T item = list.get(0);
        locator.copyTo(item);
        if (item == null) {
            getRepository().create(item);
        } else {
            getRepository().store(item);
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
    public C patchJsonapiCollection(@BeanParam L locator /*, PatchDocumentCollection patch */) {
        log.debug("patchCollection");
        // look it up first, update whtever fields are specified for update by the patch format, then issue updates...
//        HostFilterCriteria criteria = new HostFilterCriteria();
//        criteria.id = UUID.valueOf(id);
//        return searchCollection(criteria);
        return null;
    }
    
}
