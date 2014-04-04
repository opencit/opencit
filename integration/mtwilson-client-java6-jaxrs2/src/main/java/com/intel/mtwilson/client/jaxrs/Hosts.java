/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
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
public class Hosts extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Hosts(URL url) throws Exception{
        super(url);
    }

    public Hosts(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostCollection searchHosts(HostFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostCollection objCollection = getTargetPathWithQueryParams("hosts", criteria).request(MediaType.APPLICATION_JSON).get(HostCollection.class);
        return objCollection;
    }
    
    public Host retrieveHost(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Host obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Host.class);
        return obj;
    }

    public Host createHost(Host obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Host newObj = getTarget().path("hosts").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Host.class);
        return newObj;
    }

    public Host editHost(Host obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        Host newObj = getTarget().path("hosts/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Host.class);
        return newObj;
    }

    public void deleteHost(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
