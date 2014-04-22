/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code> Mle </code> is the class used to create, update, delete, search and retreive MLE's .
 * @author ssbangal
 */
public class Mles extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Mles(URL url) throws Exception{
        super(url);
    }

     /**
     * Constructor to create the <code> Mles </code> object.
     * @param properties <code> Properties </code> object to initialize the <code>Mles</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     */
    public Mles(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Search for MLESources that match a specified Filter criteria.
     * @param criteria <code> MleFilterCriteria </code> used to specify the parameters of search. 
     *          Search Criteria can be nameEqualTo, nameContains, osUuid and oemUuid
     * @return <code> MleCollection</code>, list of the Mle's that match the specified collection.
     *  The search always returns  a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * : https://10.1.71.234:8443/mtwilson/v2/mles?nameContains=Intel
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   mles:[2]
     *   0:{
     *   id: "8ca9f4f0-86a5-4b87-8abf-94f83488b161"
     *   name: "Intel_Corporation"
     *   version: "01.00.0063"
     *   attestation_type: "PCR"
     *   mle_type: "BIOS"
     *   oem_uuid: "f310b4e3-1f9c-4687-be60-90260262afd9"
     *   source: "10.1.71.175"
     *   }
     *   1:{
     *   id: "40a6e111-ad60-49bb-b047-01dc8c410aaf"
     *   name: "Intel_Thurley_VMware_ESXi"
     *   version: "5.1.0-1065491"
     *   attestation_type: "MODULE"
     *   mle_type: "VMM"
     *   os_uuid: "58bd1dac-1d89-4b48-8486-1d44fb9f5609"
     *   source: "10.1.71.175"
     *   }
     * }  
     */
    public MleCollection searchMles(MleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        MleCollection objCollection = getTargetPathWithQueryParams("mles", criteria).request(MediaType.APPLICATION_JSON).get(MleCollection.class);
        return objCollection;
    }
    
    /**
     * Retrieves the MLE with the specified uuid
     * @param uuid - UUID of the MLE to be retrieved from the backend
     * @return <code> Mle </code> that is retrieved from the backend
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/mles/8ca9f4f0-86a5-4b87-8abf-94f83488b161/
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "8ca9f4f0-86a5-4b87-8abf-94f83488b161"
     *  name: "Intel_Corporation"
     *  version: "01.00.0063"
     *  attestation_type: "PCR"
     *  mle_type: "BIOS"
     *  oem_uuid: "f310b4e3-1f9c-4687-be60-90260262afd9"
     *  source: "10.1.71.175"
     *  }
     */
    public Mle retrieveMle(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Mle obj = getTarget().path("mles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Mle.class);
        return obj;
    }

     /**
     * Creates the MLE in the database. 
     * @param mle  - MLE that needs to be created.
     * @return <code> Mle </code> post creation with the specified properties.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/mles/
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"vmmmle","version":"1.2.3","description":"Test","attestation_type":"MODULE","mle_type":"VMM",
     *     "os_uuid":"8f1ca1aa-9461-11e3-8204-005056b5286f","oem_uuid":"",
     *      "mle_manifests":[{"name": "18", "value": "BDC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "8ca9f4f0-86a5-4b87-8abf-94f83488b161"
     *  name: "vmmmle"
     *  version: "1.2.3"
     *  attestation_type: "MODULE"
     *  description: "Test"
     *  mle_type: "VMM"
     *  os_uuid: "8f1ca1aa-9461-11e3-8204-005056b5286f"
     *  -mle_manifests: [1]
     *   -0: {
     *      name: "18"
     *      value: "BDC83B19E793491B1C6EA0FD8B46CD9F32E592FC"
     *    }
     *  }
     *      
     */
    public Mle createMle(Mle obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Mle newObj = getTarget().path("mles").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Mle.class);
        return newObj;
    }

    /**
     * Edits/Updates the MLE in the database. 
     * @param mle  - MLE that needs to be updated.
     * @return <code> Mle </code> post updation with the specified properties.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/mles/8ca9f4f0-86a5-4b87-8abf-94f83488b161/
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"Test123","version":"1.2.3","description":"Test"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  version: "1.2.3"
     *  attestation_type: "PCR"
     *  description: "Test"
     *  mle_type: "BIOS"
     *  oem_uuid: "f310b4e3-1f9c-4687-be60-90260262afd9"
     *  source: "10.1.71.175"
     *  }
     *      
     */
    public Mle editMle(Mle obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        Mle newObj = getTarget().path("mles/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Mle.class);
        return newObj;
    }

     /**
     * Deletes the MLE with the specified UUID from the database
     * @param uuid - UUID of the MLE that has to be deleted.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/mles/8ca9f4f0-86a5-4b87-8abf-94f83488b161/
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     * 
     */
    public void deleteMle(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("mles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
