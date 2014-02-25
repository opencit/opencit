/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
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
public class MleModules extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MleModules(URL url) {
        //super(url);
    }

    public MleModules(Properties properties) throws Exception {
        super(properties);
    }
    
    public MleModuleCollection searchMleModules(MleModuleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", criteria.mleUuid);
        MleModuleCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/modules", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleModuleCollection.class);
        return objCollection;
    }
    
    public MleModule retrieveMleModule(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid);
        MleModule obj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleModule.class);
        return obj;
    }

    public MleModule createMleModule(MleModule obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        MleModule newObj = getTarget().path("mles/{mle_id}/modules").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MleModule.class);
        return newObj;
    }

    public MleModule editMleModule(MleModule obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        map.put("id", obj.getId().toString()); 
        MleModule newObj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MleModule.class);
        return newObj;
    }

    public void deleteMleModule(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid); 
        Response obj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
