/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code> MlePcrs </code> used to create, update, delete, search and retrieve MlePCrs from the system.
 * @author ssbangal
 */
public class MlePcrs extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MlePcrs(URL url) throws Exception{
        super(url);
    }

      /**
     * Constructor to create the <code> MlePcrs </code> object.
     * @param properties <code> Properties </code> object to initialize the <code>MlePcrs</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     * }
     */
    public MlePcrs(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Search for MLEPcrs that match a specified Filter criteria.
     * @param criteria <code> MlePcrFilterCriteria </code> used to specify the parameters of search. 
     *  criteria can be one of indexEqualTo, valueEqualTo and id
     * @return <code> MlePcrCollection</code>, list of the MlePcr's that match the specified collection.
     * 
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs?indexEqualTo=18
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *       mle_pcrs: [1]
     *       0:  {
     *       id: "38a793f8-ca70-4c9e-91cc-0474585c286d"
     *       mle_uuid: "38a793f8-ca70-4c9e-91cc-0474585c286d"
     *       pcr_index: "18"
     *       pcr_value: "2961d7d78606334bcf8ca891c0a60574a77f48b3"
     *       }
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcrFilterCriteria criteria = new MlePcrFilterCriteria();
     *   criteria.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *   criteria.indexEqualTo = "21";
     *   MlePcrCollection searchMlePcrs = client.searchMlePcrs(criteria);
     * }
     */
    
    public MlePcrCollection searchMlePcrs(MlePcrFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", criteria.mleUuid);
        MlePcrCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/pcrs", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MlePcrCollection.class);
        return objCollection;
    }
    
    /**
     * Retrieves the MLE with the specified uuid and PCR Index
     * @param mleUuid - UUID of the MLE To be retrieved
     * @param pcrIndex - INdex of the PCR to be retrieved.     * 
     * 
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs/18
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *  {
     *       id: "38a793f8-ca70-4c9e-91cc-0474585c286d"
     *       mle_uuid: "38a793f8-ca70-4c9e-91cc-0474585c286d"
     *       pcr_index: "18"
     *       pcr_value: "2961d7d78606334bcf8ca891c0a60574a77f48b3"
     *       }
     *  
     * @return <code> MlePcr </code> that is retrieved from the backend
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcr obj = client.retrieveMlePcr("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "21");
     * }
     */
    public MlePcr retrieveMlePcr(String mleUuid, String pcrIndex) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", pcrIndex);
        MlePcr obj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MlePcr.class);
        return obj;
    }

    /**
     * Creates the MLE PCR in the database.
     * @param obj - MLE PCR to be created
     * @return - MLE PCR Created 
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs
     * <p>
     * <i>Sample Input</i><br>
     *	{"pcr_index":"20","pcr_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "2100fc61-921f-405a-91af-b01dbeaf5c69"
     *   mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *   pcr_index: "20"
     *   pcr_value: "CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC"
     * }
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcr obj = new MlePcr();
     *   obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *   obj.setPcrIndex("21");
     *   obj.setPcrValue("6CAB6F19330613513101F04B88BCB7B79A8F250E");
     *   client.createMlePcr(obj);     * 
     * }
     */
    public MlePcr createMlePcr(MlePcr obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        MlePcr newObj = getTarget().path("mles/{mle_id}/pcrs").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MlePcr.class);
        return newObj;
    }

    /**
     * Updates the Specified MLE PCR.
     * @param obj - MLE PCR to be updated
     * @return  <code> MlePCR </code> post updation with the specified properties.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs
     * <p>
     * <i>Sample Input</i><br>
     *	{"pcr_index":"18","pcr_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FD"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "38a793f8-ca70-4c9e-91cc-0474585c286d"
     *   mle_uuid: "38a793f8-ca70-4c9e-91cc-0474585c286d"
     *   pcr_index: "18"
     *   pcr_value: "CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FD"
     * }
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcr obj = new MlePcr();
     *   obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *   obj.setPcrIndex("21");
     *   obj.setPcrValue("AAAB6F19330613513101F04B88BCB7B79A8F250E");
     *   MlePcr newObj = client.editMlePcr(obj);     * 
     * }
     */
    public MlePcr editMlePcr(MlePcr obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        map.put("id", obj.getPcrIndex()); 
        MlePcr newObj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MlePcr.class);
        return newObj;
    }

     /**
     * Deletes the MLE PCR with the specified UUID from the database
     * @param mleUuid - UUID of the MLE PCR  that has to be deleted.
     * @param pcrIndex - Index of the MLE PCR that has to be deleted.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs/18
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   client.deleteMlePcr("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "21");        
     * }
     */
    public void deleteMlePcr(String mleUuid, String pcrIndex) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", pcrIndex); 
        Response obj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
