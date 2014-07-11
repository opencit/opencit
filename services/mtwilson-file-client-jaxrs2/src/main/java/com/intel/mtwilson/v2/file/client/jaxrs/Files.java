/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.file.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.v2.file.model.File;
import com.intel.mtwilson.v2.file.model.FileCollection;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.WebTarget;
import com.intel.mtwilson.v2.file.model.FileFilterCriteria;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import javax.ws.rs.core.Response;

/**
 *
 * @author jbuhacoff
 */
public class Files extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Files.class);
    
     /**
     * Constructor to create the <code> Files </code> object.
     * @param properties <code> Properties </code> object to initialize the <code>Files</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Files files = new Files(prop);
     * }
     */
    public Files(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Search for Files that match a specified Filter criteria.
     * @param criteria <code> FileFilterCriteria </code> used to specify the parameters of search. 
     *  criteria can be one of nameEqualTo, nameContains and contentTypeEquals
     * @return <code> FileCollection</code>, list of the Files that match the specified collection.
     * 
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/host-files?nameEquals=testfile
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   files: [1]
     *   0:  {
     *   name: "testfile"
     *   content_type: "text/plain"
     *  }
     * }
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      Files files = new Files(My.configuration().getClientProperties())
     *      FileFilterCriteria criteria = new FileFilterCriteria();
     *      criteria.nameEquals = "testfile";
     *      FileCollection resultCollection = files.searchFiles(criteria);
     * }
     */
    public FileCollection searchFiles(FileFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        FileCollection files = getTargetPathWithQueryParams("host-files", criteria).request().accept(MediaType.APPLICATION_JSON).get(FileCollection.class);
        return files;
    }
    
    
     /**
     * Retrieve for Files with the specified id.
     * @param id - ID of the File to be retrieved
     * @return <code> File </code>, File with the specified Id value.
     * 
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     *   https://10.1.71.234:8181/mtwilson/v2/host-files/88bff1c8-0766-465f-a25f-78c6a37c0321
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "88bff1c8-0766-465f-a25f-78c6a37c0321"
     *  meta: {
     *      content_href: "/files/88bff1c8-0766-465f-a25f-78c6a37c0321/content"
     *  }
     *  name: "testfile"
     *  content_type: "text/plain"
     *  }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Files files = new Files(My.configuration().getClientProperties())
     *  File file = files.retrieveFile((new UUID()).toString());
     * }
     */
    public File retrieveFile(String id) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", id);
        File file = getTarget().path("host-files/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).get(File.class);
        return file;
    }
    
    
    /**
     * Creates the File in the database.
     * @param file - File to be created
     * @return - File post creation
     *  
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/host-files
     * <p>
     * <i>Sample Input</i><br>
     *  {"name":"123_TEST","content_type":"text/plain"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "3b5563d2-c5e2-4a4d-9ce7-acf6f9ec3d02"
     *   name: "TEST123"
     *   content_type: "text/plain"
     * }
     *  
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Files files = new Files(My.configuration().getClientProperties())
     *  File file = new File();
     *  file.setId(new UUID());
     *  file.setName("TEST123");
     *  file.setContent("hello world");
     *  file.setContentType("textplain");
     *  File responseFile = files.createFile(file);     * 
     * }
     */
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
