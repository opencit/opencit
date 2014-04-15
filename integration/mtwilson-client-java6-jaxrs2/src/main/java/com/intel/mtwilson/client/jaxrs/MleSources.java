/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code> MleSources </code> is the class used to create, update, delete, search and retreive MLE sources.
 * @author ssbangal
 */
public class MleSources extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MleSources(URL url) throws Exception{
        super(url);
    }

     /**
     * Constructor to create the <code> MleSources </code> object.
     * @param properties <code> Properties </code> object to initialize the <code>MleSources</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MleSources client = new MleSources(My.configuration().getClientProperties());
     * }
     */
    public MleSources(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Search for MLESources that match a specified Filter criteria.
     * @param criteria <code> MleSourceFilterCriteria </code> used to specify the parameters of search. Criteria should be non-null and the mleUUID should be set.
     * Permissible criteria is only id for MLESources.
     * @return <code> MleSourceCollection</code>, list of the MleSource's that match the specified collection.
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/source?id=31021a8a-de64-4c5f-b314-8d3f077a55e5
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   mle_sources: [1]
     *   0:  {
     *     id: "e20ba68e-4a37-4b79-9b85-cd94d33aa5de"
     *     name: "10.1.71.91"
     *     mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *   }
     *  }
     *  
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSourceFilterCriteria criteria = new MleSourceFilterCriteria();
     *      criteria.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *      MleSourceCollection searchMleSources = client.searchMleSources(criteria);
     * }
     */
    public MleSourceCollection searchMleSources(MleSourceFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", criteria.mleUuid);
        MleSourceCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/source", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleSourceCollection.class);
        return objCollection;
    }
    
     /**
     * Retrieve the MleSource corresponding to the specified mleUUID and UUID
     * @param mleUuid - UUId of the MLE and should be non-null.
     * @param uuid - UUID of the MLE source and should be non-null.
     * @return <code>MleSource</code> associated with the mentioned UUID's.
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>     * 
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/source/31021a8a-de64-4c5f-b314-8d3f077a55e5
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "e20ba68e-4a37-4b79-9b85-cd94d33aa5de"
     *   name: "10.1.71.91"
     *   mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     * }
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSource obj = client.retrieveMleSource("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "652e0b01-bcee-45cd-ae4d-ae561029dbd4");
     * }
     *
     */
    public MleSource retrieveMleSource(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid);
        MleSource obj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleSource.class);
        return obj;
    }

     /**
     * Creates the MLESource with the specified parameters.
     * @param obj - <code> MleSource </code> to be created. MleSource should be non-null and have the mle_id set to a non-null value.
     * @return <code>MleSource</code> that is created.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>     * 
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/source
     * <i><u>Sample Input:</u></i><br>
     *  {"name":"Server 01"}
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "27a1eb1b-ead4-473f-a245-1b4d89b7491"
     *   name: "Server 01"
     *   mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *  }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSource obj = new MleSource();
     *      obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *      obj.setName("100.1.1.1");
     *      MleSource newObj = client.createMleSource(obj);
     * }
     */
    public MleSource createMleSource(MleSource obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid());
        MleSource newObj = getTarget().path("mles/{mle_id}/source").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MleSource.class);
        return newObj;
    }

    
    /**
     * Edit/Update the MleSource object with the specified parameters.
     * @param obj - <code> MleSource </code> to be updated. It should be non-null value and have both mle_id and id set to non-null values.
     * @return <code>MleSource </code> that is updated.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>     * 
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/source/31021a8a-de64-4c5f-b314-8d3f077a55e5
     * <i><u>Sample Input:</u></i><br>
     *  {"name":"10.1.71.91"}
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *   name: "10.1.71.91"
     *   mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *  }
     *
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSource obj = new MleSource();
     *      obj.setId(UUID.valueOf("652e0b01-bcee-45cd-ae4d-ae561029dbd4"));
     *      obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *      obj.setName("10.1.71.100");
     *      MleSource newObj = client.editMleSource(obj);
     * }
     */
    public MleSource editMleSource(MleSource obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        map.put("id", obj.getId().toString()); // Even though this id is not needed, the framework expects it to be there.
        MleSource newObj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MleSource.class);
        return newObj;
    }

     /**
     * Deletes the MLE Source associated with the specified MLE UUID and UUID.
     * @param mleUuid - UUID Of the MLE associated with the MLE Source to be deleted and should be non-null.
     * @param uuid - UUID of the MLE Source associated and should be non-null.
     * 
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/modules/51bd39cc-24af-4780-91e5-d9bcfe13ec6f
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      client.deleteMleSource("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "8bc7bdfe-fe20-4385-9263-0e689e776f92");        
     * }
     */
    public void deleteMleSource(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid); // Even though this id is not needed, the framework expects it to be there.
        Response obj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
