/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.File;
import com.intel.mtwilson.as.rest.v2.model.FileCollection;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.WebTarget;
import com.intel.mtwilson.as.rest.v2.model.FileFilterCriteria;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author jbuhacoff
 */
public class Files extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
    public Files() {
        super();
    }
    public Files(Properties properties) throws Exception {
        super(properties);
    }
    
    public FileCollection searchFiles(FileFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FileCollection files = getTargetPathWithQueryParams("files", criteria).request(MediaType.APPLICATION_JSON).get(FileCollection.class);
        return files;
    }
    public File retrieveFile(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        File file = getTarget().path("files/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(File.class);
        return file;
    }
    
}
