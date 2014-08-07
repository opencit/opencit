/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
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

    public MlePcrs(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new whitelist value in the system and associates with the Mle specified. Except for PCR 19 all whitelists
     * for other PCRs should be configured using this method. For Citrix XenServer hosts even PCR 19 should be configured
     * using this method. PCR based whitelists are compared directly to the PCR values retrieved from the host as against the
     * Module based whitelists where individual modules are used for calculation of PCR 19. Currently only PCR 19 supports 
     * Module based attestation (exception being Citrix XenServer).
     * @param obj - MlePcr to be created
     * @return - MlePcr Created 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_pcrs:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs
     * Input: {"pcr_index":"20","pcr_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC"}
     * Output: {"id":"2100fc61-921f-405a-91af-b01dbeaf5c69","mle_uuid":"31021a8a-de64-4c5f-b314-8d3f077a55e5",
     *   "pcr_index":"20","pcr_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcr obj = new MlePcr();
     *   obj.setMleUuid("31021a8a-de64-4c5f-b314-8d3f077a55e5");
     *   obj.setPcrIndex("20");
     *   obj.setPcrValue("CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC");
     *   client.createMlePcr(obj);     * 
     * </pre>
     */
    public MlePcr createMlePcr(MlePcr obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", obj.getMleUuid());
        MlePcr newObj = getTarget().path("mles/{mle_id}/pcrs").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MlePcr.class);
        return newObj;
    }
    
     /**
     * Deletes the specified MlePcr whitelist for the specified Mle object
     * @param mleUuid - UUID of the Mle with which the whitelist has been associated.
     * @param pcrIndex - Index of the MLE PCR that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_pcrs:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs/18
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   client.deleteMlePcr("31021a8a-de64-4c5f-b314-8d3f077a55e5", "18");        
     * </pre>
     */
    public void deleteMlePcr(String mleUuid, String pcrIndex) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", mleUuid);
        map.put("id", pcrIndex); 
        Response obj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
    /**
     * Deletes the PCR white lists of the specified MLE using the filter criteria.
     * @param criteria MlePcrFilterCriteria object specifying the search criteria. Search options supported
     * include id, indexEqualTo and valueEqualTo.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_pcrs:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs?indexEqualTo=18
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  MlePcr client = new MlePcr(My.configuration().getClientProperties());
     *  MlePcrFilterCriteria criteria = new MlePcrFilterCriteria();
     *  criteria.mleUuid = UUID.valueOf("31021a8a-de64-4c5f-b314-8d3f077a55e5");
     *  criteria.indexEqualTo = "18";
     *  client.deleteMlePcr(criteria);
     * </pre>
     */
    public void deleteMlePcr(MlePcrFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", criteria.mleUuid);
        Response obj = getTargetPathWithQueryParams("mles/{mle_id}/pcrs", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete Mle PCR failed");
        }
    }
    
    /**
     * Updates the value of the specified whitelist.
     * @param obj - MlePcr to be updated
     * @return  Updated <code> MlePCR </code>.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_pcrs:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs/18
     * Input: {"pcr_value":"AAAB6F19330613513101F04B88BCB7B79A8F250E"}
     * Output: {"id":"38a793f8-ca70-4c9e-91cc-0474585c286d","mle_uuid":"31021a8a-de64-4c5f-b314-8d3f077a55e5",
     *   "pcr_index":"18","pcr_value":"AAAB6F19330613513101F04B88BCB7B79A8F250E"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcr obj = new MlePcr();
     *   obj.setMleUuid("31021a8a-de64-4c5f-b314-8d3f077a55e5");
     *   obj.setPcrIndex("18");
     *   obj.setPcrValue("AAAB6F19330613513101F04B88BCB7B79A8F250E");
     *   MlePcr newObj = client.editMlePcr(obj);     * 
     * }
     */
    public MlePcr editMlePcr(MlePcr obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", obj.getMleUuid());
        map.put("id", obj.getPcrIndex()); 
        MlePcr newObj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MlePcr.class);
        return newObj;
    }
    
    /**
     * Retrieves the specified whitelist associated with the Mle.
     * @param mleUuid - UUID of the Mle associated with the whitelist.
     * @param pcrIndex - Index of the PCR whitelist to be retrieved. 
     * @return PCR whitelist matching the specified criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_pcrs:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs/18
     * Output: {"id":"38a793f8-ca70-4c9e-91cc-0474585c286d","mle_uuid":"31021a8a-de64-4c5f-b314-8d3f077a55e5",
     *   "pcr_index":"18","pcr_value":"AAAB6F19330613513101F04B88BCB7B79A8F250E"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcr obj = client.retrieveMlePcr("31021a8a-de64-4c5f-b314-8d3f077a55e5", "18");
     * </pre>
     */
    public MlePcr retrieveMlePcr(String mleUuid, String pcrIndex) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", mleUuid);
        map.put("id", pcrIndex);
        MlePcr obj = getTarget().path("mles/{mle_id}/pcrs/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MlePcr.class);
        return obj;
    }
    
     /**
     * Searches for PCR whitelists matching the specified criteria.
     * @param criteria MlePcrFilterCriteria object specifying the filter criteria. Currently supported
     * search options include id, indexEqualTo and valueEqualTo.
     * If in case the caller needs the list of all records, filter option can to be set to false. [Ex: /pcrs?filter=false]
     * @return MlePcrCollection having the list of the PCR whitelists that match the specified criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_pcrs:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/pcrs?indexEqualTo=18
     * Output: {"mle_pcrs":[{"id":"38a793f8-ca70-4c9e-91cc-0474585c286d","mle_uuid":"31021a8a-de64-4c5f-b314-8d3f077a55e5",
     *   "pcr_index":"18","pcr_value":"AAAB6F19330613513101F04B88BCB7B79A8F250E"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MlePcrs client = new MlePcrs(My.configuration().getClientProperties());
     *   MlePcrFilterCriteria criteria = new MlePcrFilterCriteria();
     *   criteria.mleUuid = UUID.valueOf("31021a8a-de64-4c5f-b314-8d3f077a55e5");
     *   criteria.indexEqualTo = "18";
     *   MlePcrCollection searchMlePcrs = client.searchMlePcrs(criteria);
     * </pre>
     */
    
    public MlePcrCollection searchMlePcrs(MlePcrFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", criteria.mleUuid);
        MlePcrCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/pcrs", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MlePcrCollection.class);
        return objCollection;
    }    
}
