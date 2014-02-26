/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.model.File;
import com.intel.mtwilson.as.rest.v2.model.FileCollection;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class Oems extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    /* commenting out because the corresponding MtWilsonClient constructor is already fixed in dev-2.0-sprint-1-atag-merge  but not yet merged into dev-2.0-sprint-1 , so TODO re-enable this after everything is merged back
    public Oems(URL url) {
        super(url);
    }
    */

    public Oems(Properties properties) throws Exception {
        super(properties);
    }
    
    public OemCollection searchOems(String name) {
        log.debug("target: {}", getTarget().getUri().toString());
        OemCollection oems = getTarget().path("oems").queryParam("nameContains", name).request(MediaType.APPLICATION_JSON).get(OemCollection.class);
        return oems;
    }
    
    public Oem retrieveOem(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        Oem oem = getTarget().path("oems/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Oem.class);
        return oem;
    }

    public Oem createOem(Oem oem) {
        log.debug("target: {}", getTarget().getUri().toString());
        Oem newOem = getTarget().path("oems").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(oem), Oem.class);
        return newOem;
    }

    public Oem editOem(Oem oem) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", oem.getId().toString());
        Oem newOem = getTarget().path("oems/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(oem), Oem.class);
        return newOem;
    }

    public void deleteOem(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        Response oem = getTarget().path("oems/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(oem.toString());
    }
    
}
