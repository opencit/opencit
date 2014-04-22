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
 * <code>Oems</code> is the class that allows creation , deletion  and updates to OEM objects.
 * 
 * @author ssbangal
 */
public class Oems extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Oems(URL url) throws Exception{
        super(url);
    }

    /**
     * Initializes the <class>Oems</class> with the Mt.Wilson configuration properties.      * 
     * @param properties Properties associated with the Mt.Wilson configuration. 
     * Use the getClientProperties() of <class>MyConfiguration</class> for initializing.
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     * }
     */
    public Oems(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Searches for the OEM's with the specified set of criteria
     * @param criteria - <code> OemFilterCriteria </code> expressing the filter criteria
     *      The possible search options include nameEqualTo and nameContains.
     * @return <code> OemCollection </code> that meets the filter criteria
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oems?nameContains=Intel
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"oems":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * 
     *  <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  OemFilterCriteria criteria = new OemFilterCriteria();
     *  //criteria.id = new UUID();
     *  criteria.nameContains = "ibm";
     *  //criteria.nameEqualTo = "nameequalto";
     *  OemCollection oems = client.searchOems(criteria);
     * } 
     * 
     */
    public OemCollection searchOems(OemFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        //OemCollection oems = getTarget().path("oems").queryParam("nameContains", name).request(MediaType.APPLICATION_JSON).get(OemCollection.class);
        OemCollection oems = getTargetPathWithQueryParams("oems", criteria).request(MediaType.APPLICATION_JSON).get(OemCollection.class);
        return oems;
    }
    
     /**
     * Retrieves the OEM with the specified uuid
     * @param uuid - UUID of the OEM to be retrieved from the backend
     * @return <code> Oem </code> that is retrieved from the backend
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oems/31741556-f5c7-4eb6-a713-338a23e43b93
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     * id: "31741556-f5c7-4eb6-a713-338a23e43b93"
     * name: "Test123"
     * description: "Intel OEM"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  Oem retrieveOem = client.retrieveOem("27ae76f0-e678-4224-92fc-a91ebbf761b8");
     * }
     */
    public Oem retrieveOem(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Oem oem = getTarget().path("oems/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Oem.class);
        return oem;
    }

     /**
     * Creates an OEM object with the specified parameters.
     * 
     * @param oem - OEM object that needs to be created. It should be a non-null object with name and description.
     * @return <code> Oem </code> that is created in the database
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oems
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"Test123","description":"Intel OEM"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     * id: "31741556-f5c7-4eb6-a713-338a23e43b93"
     * name: "Test123"
     * description: "Intel OEM"
     * }
     *       
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  Oem oem = new Oem();
     *  oem.setName("APIOEM");
     *  oem.setDescription("API Created OEM");
     *  Oem createOem = client.createOem(oem);
     * }
     */
    public Oem createOem(Oem oem) {
        log.debug("target: {}", getTarget().getUri().toString());
        Oem newOem = getTarget().path("oems").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(oem), Oem.class);
        return newOem;
    }

    /**
     * Edits/Updates the OEM in the database. 
     * @param oem - OEM that needs to be updated.
     * @return <code> Oem </code> post updation with the specified properties.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oems/31741556-f5c7-4eb6-a713-338a23e43b93
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"Test123","description":"Intel OEM updated"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     * id: "31741556-f5c7-4eb6-a713-338a23e43b93"
     * name: "Test123"
     * description: "Intel OEM updated"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);  
     *  Oem oem = new Oem();
     *  oem.setId(UUID.valueOf("27ae76f0-e678-4224-92fc-a91ebbf761b8"));
     *  oem.setDescription("Updated description");
     *  oem = client.editOem(oem);
     * }
     */
    public Oem editOem(Oem oem) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", oem.getId().toString());
        Oem newOem = getTarget().path("oems/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(oem), Oem.class);
        return newOem;
    }

    /**
     * Deletes the OEM with the specified UUID from the database
     * @param uuid - UUID of the OEM that has to be deleted.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8443/mtwilson/v2/oems/31741556-f5c7-4eb6-a713-338a23e43b93
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     *   
     * <i><u>Sample Java API call :</u></i><br>
     *{@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  client.deleteOem("27ae76f0-e678-4224-92fc-a91ebbf761b8");
     * }
     */
    public void deleteOem(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response oem = getTarget().path("oems/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(oem.toString());
    }
    
}
