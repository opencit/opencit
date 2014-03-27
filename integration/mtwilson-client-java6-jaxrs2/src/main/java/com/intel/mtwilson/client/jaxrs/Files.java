/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.File;
import com.intel.mtwilson.as.rest.v2.model.FileCollection;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.WebTarget;
import com.intel.mtwilson.as.rest.v2.model.FileFilterCriteria;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
public class Files extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
    public Files(Properties properties) throws Exception {
        super(properties);
    }
    
    public FileCollection searchFiles(FileFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FileCollection files = getTargetPathWithQueryParams("host-files", criteria).request().accept(MediaType.APPLICATION_JSON).get(FileCollection.class);
        return files;
    }
    public File retrieveFile(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        File file = getTarget().path("host-files/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).get(File.class);
        return file;
    }
    public File createFile(File file) {
        log.debug("target: {}", getTarget().getUri().toString());
        /*
        Response response = getTarget().path("host-files").request().post(Entity.entity(file, MediaType.APPLICATION_JSON));
        if( response.hasEntity()) {
            log.debug("response has an entity");
            String test = response.readEntity(String.class); log.debug("Response = {}", test);
        }
        return null;
        */
        File responseFile = getTarget().path("host-files").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(file)).readEntity(File.class);
        return responseFile;
    }
    
}
