/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.User;
import com.intel.mtwilson.as.rest.v2.model.UserCollection;
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
public class PollHosts extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public PollHosts(URL url) throws Exception{
        super(url);
    }

    public PollHosts(Properties properties) throws Exception {
        super(properties);
    }
    
    public UserCollection searchUsers(String name) {
        log.debug("target: {}", getTarget().getUri().toString());
        UserCollection objCollection = getTarget().path("users").queryParam("nameContains", name).request(MediaType.APPLICATION_JSON).get(UserCollection.class);
        return objCollection;
    }
    
    public User retrieveUser(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        User obj = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(User.class);
        return obj;
    }

    public User createUser(User obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        User newObj = getTarget().path("users").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), User.class);
        return newObj;
    }

    public User editUser(User obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        User newObj = getTarget().path("users/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), User.class);
        return newObj;
    }

    public void deleteUser(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        Response obj = getTarget().path("users/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
