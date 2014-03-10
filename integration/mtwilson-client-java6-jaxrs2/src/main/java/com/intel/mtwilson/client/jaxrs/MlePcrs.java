/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
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
public class MlePcrs extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MlePcrs(URL url) {
        super(url);
    }

    public MlePcrs(Properties properties) throws Exception {
        super(properties);
    }
    
    public MlePcrCollection searchMlePcrs(MlePcrFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", criteria.mleUuid);
        MlePcrCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/pcrs", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MlePcrCollection.class);
        return objCollection;
    }
    
    public MlePcr retrieveMlePcr(String mleUuid, String pcrIndex) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", pcrIndex);
        MlePcr obj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MlePcr.class);
        return obj;
    }

    public MlePcr createMlePcr(MlePcr obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        MlePcr newObj = getTarget().path("mles/{mle_id}/pcrs").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MlePcr.class);
        return newObj;
    }

    public MlePcr editMlePcr(MlePcr obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        map.put("id", obj.getPcrIndex()); 
        MlePcr newObj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MlePcr.class);
        return newObj;
    }

    public void deleteMlePcr(String mleUuid, String pcrIndex) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", pcrIndex); 
        Response obj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
