/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.intel.mtwilson.repository.FilterCriteria;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Encapsulates a set of frequently-used APIs to create, store, search, 
 * retrieve, and delete resources. The server may not implement all of these
 * methods for each resource, but where implemented the methods here should
 * be a convenient short-hand for accessing them.
 * 
 * We expect that the server will provide localized error messages in the status line
 * or an Error header if it encounters a problem.
 * 
 * @author jbuhacoff
 */
public class JsonResource<T extends Document, C extends DocumentCollection<T>> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonResource.class);
    private Class<T> itemClass;
    private Class<C> collectionClass;
    private WebTarget target;

    protected JsonResource() {
        this.itemClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]; // itemClass;
        this.collectionClass = (Class<C>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1]; //collectionClass;
    }
    
    /**
     * 
     * @param target representing a URL with a resource path like /mtwilson/v2/reports
     */
    public JsonResource(WebTarget target/*, Class<T> itemClass, Class<C> collectionClass*/) {
        this();
        this.target = target;
    }
    
    /**
     * The path is appended to the target, so if the target represents a
     * URL like /mtwilson/v2 and path is "reports" the requests generated
     * by the resource class would go to /mtwilson/v2/reports
     * 
     * @param target representing a URL with a base path like /mtwilson/v2
     * @param path for example "users", "hosts", "reports" 
     */
    public JsonResource(WebTarget target, String path/*, Class<T> clazz, Class<C> collectionClass*/) {
        this(target.path(path));
    }

    public WebTarget getTarget() {
        return target;
    }

    
    
    public T create(T item) {
        log.debug("create target: {}", getTarget().getUri().toString());
        T createdItem = getTarget().request().accept(MediaType.APPLICATION_JSON).post(Entity.json(item), itemClass);
        return createdItem;
    }

    public T store(T item) {
        log.debug("store target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", item.getId().toString());
        T updatedItem = getTarget().path("{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(item), itemClass);
        return updatedItem;
    }

    public C search(FilterCriteria<T> criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        C itemCollection = JaxrsClient.addQueryParams(getTarget(), criteria).request(MediaType.APPLICATION_JSON).get(collectionClass);
        return itemCollection;
    }    
    
    public T retrieve(UUID uuid) {
        log.debug("retrieve target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        T item = getTarget().path("{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(itemClass);
        return item;
    }
    
    public void delete(UUID uuid) {
        log.debug("delete target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response response = getTarget().path("{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug("delete status: {} {}", response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
    }
    
}
