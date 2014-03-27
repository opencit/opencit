/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
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
public class TagCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public TagCertificates(URL url) {
        //super(url);
    }

    public TagCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public TagCertificateCollection searchTagCertificates(TagCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        TagCertificateCollection objCollection = getTargetPathWithQueryParams("host-tag-certificates", criteria).request(MediaType.APPLICATION_JSON).get(TagCertificateCollection.class);
        return objCollection;
    }
    
    public TagCertificate retrieveTagCertificate(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        TagCertificate obj = getTarget().path("host-tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TagCertificate.class);
        return obj;
    }

    public TagCertificate createTagCertificate(TagCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        TagCertificate newObj = getTarget().path("host-tag-certificates").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), TagCertificate.class);
        return newObj;
    }

    public TagCertificate editTagCertificate(TagCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        TagCertificate newObj = getTarget().path("host-tag-certificates/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), TagCertificate.class);
        return newObj;
    }

    public void deleteTagCertificate(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("host-tag-certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
