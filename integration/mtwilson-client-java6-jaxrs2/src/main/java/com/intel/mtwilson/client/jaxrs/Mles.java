/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
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
public class Mles extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Mles(URL url) {
        super(url);
    }

    public Mles(Properties properties) throws Exception {
        super(properties);
    }
    
    public MleCollection searchMles(MleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        MleCollection objCollection = getTargetPathWithQueryParams("mles", criteria).request(MediaType.APPLICATION_JSON).get(MleCollection.class);
        return objCollection;
    }
    
    public Mle retrieveMle(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Mle obj = getTarget().path("mles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Mle.class);
        return obj;
    }

    public Mle createMle(Mle obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Mle newObj = getTarget().path("mles").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Mle.class);
        return newObj;
    }

    public Mle editMle(Mle obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        Mle newObj = getTarget().path("mles/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Mle.class);
        return newObj;
    }

    public void deleteMle(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("mles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
