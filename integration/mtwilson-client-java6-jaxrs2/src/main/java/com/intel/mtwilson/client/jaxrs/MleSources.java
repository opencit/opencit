/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
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
public class MleSources extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MleSources(URL url) {
        //super(url);
    }

    public MleSources(Properties properties) throws Exception {
        super(properties);
    }
    
    public MleSourceCollection searchMleSources(MleSourceFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", criteria.mleUuid);
        MleSourceCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/source", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleSourceCollection.class);
        return objCollection;
    }
    
    public MleSource retrieveMleSource(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid);
        MleSource obj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleSource.class);
        return obj;
    }

    public MleSource createMleSource(MleSource obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid());
        MleSource newObj = getTarget().path("mles/{mle_id}/source").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MleSource.class);
        return newObj;
    }

    public MleSource editMleSource(MleSource obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        map.put("id", obj.getId().toString()); // Even though this id is not needed, the framework expects it to be there.
        MleSource newObj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MleSource.class);
        return newObj;
    }

    public void deleteMleSource(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid); // Even though this id is not needed, the framework expects it to be there.
        Response obj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
