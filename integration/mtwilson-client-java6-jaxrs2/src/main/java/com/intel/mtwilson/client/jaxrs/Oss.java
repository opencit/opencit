/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Oss</code> is the class that allows creation , deletion  and updates to OS objects.
 * @author ssbangal
 */
public class Oss extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Oss(URL url) throws Exception{
        super(url);
    }

    /**     
     * Initializes the <class>OSS</class> with the Mt.Wilson configuration properties.      * 
     * @param properties Properties associated with the Mt.Wilson configuration. 
     * Use the getClientProperties() of <class>MyConfiguration</class> for initializing.
     * @throws Exception 
     
     * @param properties
     * @throws Exception 
     * 
     */
    public Oss(Properties properties) throws Exception {
        super(properties);
    }
    
     
     /**
     * Searches for the OS's with the specified set of criteria
     * @param criteria - <code> OsFilterCriteria </code> expressing the filter criteria
     *      The possible search options include id, nameEqualTo and nameContains.
     * @return <code> OsCollection </code> that meets the filter criteria
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/oss?nameContains=VMWare
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   oss: [2]
     *       0:  {
     *           id: "d5b084f3-c0c0-11e3-bfb2-005056b5643f"
     *           name: "VMWare"
     *           version: "5.0"
     *       }
     *       1:  {
     *           id: "b7acfc54-f37c-4630-aec7-0e9f22fee6ec"
     *           name: "VMware_ESXi"
     *           version: "5.5.0"
     *       }
     *   }
     * 
     */
    public OsCollection searchOss(OsFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        OsCollection objCollection = getTargetPathWithQueryParams("oss", criteria).request(MediaType.APPLICATION_JSON).get(OsCollection.class);
        return objCollection;
    }
    
     /**
     * Retrieves the OS with the specified uuid
     * @param uuid - UUID of the US to be retrieved from the backend
     * @return <code> OS </code> that is retrieved from the backend
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/oss/b7acfc54-f37c-4630-aec7-0e9f22fee6ec
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "b7acfc54-f37c-4630-aec7-0e9f22fee6ec"
     *  name: "VMware_ESXi"
     *  version: "5.5.0"
     * }
     * 
     */
    public Os retrieveOs(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Os obj = getTarget().path("oss/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Os.class);
        return obj;
    }

     /**
     * Creates an OS object with the specified parameters.
     * 
     * @param OS - OS object that needs to be created. It should be a non-null object with name and description.
     * @return <code> OS </code> that is created in the database
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oss
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"Test123","description":"Intel OEM","version":"v1.2.3"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "e946ccec-4a55-4913-bdb6-5878c88a9e81"
     *  name: "Test123"
     *  version: "v1.2.3"
     *  description: "Intel OEM"
     * }
     *      
    */
    public Os createOs(Os os) {
        log.debug("target: {}", getTarget().getUri().toString());
        Os newObj = getTarget().path("oss").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(os), Os.class);
        return newObj;
    }

      /**
     * Edits/Updates the OS in the database. 
     * @param OS - OS that needs to be updated.
     * @return <code> OS </code> post updation with the specified properties.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oss/e946ccec-4a55-4913-bdb6-5878c88a9e81
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"Test1234","description":"Intel OEM updated"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     * id: "e946ccec-4a55-4913-bdb6-5878c88a9e81"
     * name: "Test1234"
     * description: "Intel OEM updated"
     * }
     *      
     */
    public Os editOs(Os obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        Os newObj = getTarget().path("oss/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Os.class);
        return newObj;
    }

    /**
     * Deletes the OS with the specified UUID from the database
     * @param uuid - UUID of the OS that has to be deleted.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/oss/e946ccec-4a55-4913-bdb6-5878c88a9e81
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     * 
     */
    public void deleteOs(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("oss/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
