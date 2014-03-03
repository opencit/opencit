/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
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
public class HostAttestations extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostAttestations(URL url) {
        //super(url);
    }

    public HostAttestations(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostAttestationCollection searchHostAttestations(HostAttestationFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostAttestationCollection objCollection = getTargetPathWithQueryParams("host-attestations", criteria).request(MediaType.APPLICATION_JSON).get(HostAttestationCollection.class);
        return objCollection;
    }
    
    public HostAttestation retrieveHostAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        HostAttestation obj = getTarget().path("host-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAttestation.class);
        return obj;
    }

    public HostAttestation createHostAttestation(HostAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostAttestation newObj = getTarget().path("host-attestations").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), HostAttestation.class);
        return newObj;
    }

    public HostAttestation editHostAttestation(HostAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        HostAttestation newObj = getTarget().path("host-attestations/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), HostAttestation.class);
        return newObj;
    }

    public void deleteHostAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("host-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
