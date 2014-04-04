/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class Oss extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Oss(URL url) throws Exception{
        super(url);
    }

    public Oss(Properties properties) throws Exception {
        super(properties);
    }
    
    public OsCollection searchOss(OsFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        OsCollection objCollection = getTargetPathWithQueryParams("oss", criteria).request(MediaType.APPLICATION_JSON).get(OsCollection.class);
        return objCollection;
    }
    
    public Os retrieveOs(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Os obj = getTarget().path("oss/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Os.class);
        return obj;
    }

    public Os createOs(Os os) {
        log.debug("target: {}", getTarget().getUri().toString());
        Os newObj = getTarget().path("oss").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(os), Os.class);
        return newObj;
    }

    public Os editOs(Os obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        Os newObj = getTarget().path("oss/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Os.class);
        return newObj;
    }

    public void deleteOs(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("oss/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
