/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.UserCertificate;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.UserCertificateFilterCriteria;
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
public class UserCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public UserCertificates(URL url)throws Exception {
        super(url);
    }

    public UserCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public UserCertificateCollection searchUserCertificates(UserCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("user_id", criteria.userUuid);
        UserCertificateCollection objCollection = getTargetPathWithQueryParams("users/{user_id}/certificates", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(UserCertificateCollection.class);
        return objCollection;
    }
    
    public UserCertificate retrieveUserCertificate(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        UserCertificate obj = getTarget().path("users/{user_id}/certificates/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(UserCertificate.class);
        return obj;
    }

    public UserCertificate createUserCertificate(UserCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("user_id", obj.getUserUuid().toString());
        UserCertificate newObj = getTarget().path("users/{user_id}/certificates")
                .resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), UserCertificate.class);
        return newObj;
    }

    public UserCertificate editUserCertificate(UserCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("user_id", obj.getUserUuid().toString());
        map.put("id", obj.getId().toString());
        UserCertificate newObj = getTarget().path("users/{user_id}/certificates/{id}")
                .resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), UserCertificate.class);
        return newObj;
    }

    public void deleteUserCertificate(String userUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("user_id", userUuid);
        map.put("id", uuid);
        Response obj = getTarget().path("users/{user_id}/certificates/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
