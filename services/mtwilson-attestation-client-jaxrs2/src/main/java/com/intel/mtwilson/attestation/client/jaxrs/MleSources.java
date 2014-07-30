/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
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
 * <code> MleSources </code> is the class used to create, update, delete, search and retreive MLE sources.
 * @author ssbangal
 */
public class MleSources extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MleSources(URL url) throws Exception{
        super(url);
    }

    public MleSources(Properties properties) throws Exception {
        super(properties);
    }
    
    
     /**
     * Configures the source/name of the host from which the whitelists for the specified Mle were configured.
     * This information is mainly for auditing and tracking purpose.
     * @param obj - <code> MleSource </code> object specifying the name of the host and the details of the MLE object for
     * which the mapping should be added.
     * @return <code>MleSource</code> that is created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_sources:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1/source
     * Input: {"name":"Server 01"}
     * Output: {"mle_sources":[{"id":"89a46064-c831-4c9b-acd7-1f0c8d1984de","name":"Server 01","mle_uuid":"9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSource obj = new MleSource();
     *      obj.setMleUuid("9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1");
     *      obj.setName("Server 01");
     *      MleSource newObj = client.createMleSource(obj);
     * </pre>
     */
    public MleSource createMleSource(MleSource obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", obj.getMleUuid());
        MleSource newObj = getTarget().path("mles/{mle_id}/source").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MleSource.class);
        return newObj;
    }

     /**
     * Deletes the existing mapping between the Mle and the host from which the white lists for the Mle were configured.
     * @param mleUuid - UUID Of the Mle for which the mapping should be deleted.
     * @param uuid - UUID of the MleSource associated with the Mle.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_sources:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1/source/89a46064-c831-4c9b-acd7-1f0c8d1984de
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      client.deleteMleSource("9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1", "89a46064-c831-4c9b-acd7-1f0c8d1984de");        
     * </pre>
     */
    public void deleteMleSource(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid); // Even though this id is not needed, the framework expects it to be there.
        Response obj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
    /**
     * Deletes the existing mapping between the Mle and the host from which the white lists for the Mle were configured
     * using the specified filter criteria.
     * @param criteria MleFilterCriteria object specifying the search criteria. Search options supported
     * include UUID of Mle.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_sources:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1/source/
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  MleSource client = new MleSource(My.configuration().getClientProperties());
     *  MleSourceFilterCriteria criteria = new MleSourceFilterCriteria();
     *  criteria.mleUuid = UUID.valueOf("9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1");
     *  client.deleteMle(criteria);
     * </pre>
     */
    public void deleteMleSource(MleSourceFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", criteria.mleUuid);
        Response obj = getTargetPathWithQueryParams("mles/{mle_id}/source", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete Mle Source failed");
        }
    }
    
    /**
     * Updates the host name from which the white lists were retrieved for the specified Mle. 
     * @param obj - <code> MleSource </code> to be updated. 
     * @return <code>MleSource </code> that is updated.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_sources:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1/source/89a46064-c831-4c9b-acd7-1f0c8d1984de
     * Input: {"name":"Server 02"}
     * Output: {"id":"89a46064-c831-4c9b-acd7-1f0c8d1984de","name":"Server 02","mle_uuid":"9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSource obj = new MleSource();
     *      obj.setId(UUID.valueOf("89a46064-c831-4c9b-acd7-1f0c8d1984de"));
     *      obj.setMleUuid("9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1");
     *      obj.setName("Server 02");
     *      MleSource newObj = client.editMleSource(obj);
     * </pre>
     */
    public MleSource editMleSource(MleSource obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", obj.getMleUuid());
        map.put("id", obj.getId().toString()); // Even though this id is not needed, the framework expects it to be there.
        MleSource newObj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MleSource.class);
        return newObj;
    }

     /**
     * Retrieve the details of the source from which the whitelists were configured for the specified Mle
     * @param mleUuid - UUId of the Mle for which the whitelist source needs to be retrieved.
     * @param uuid - UUID of the MleSource.
     * @return <code>MleSource</code> matching the specified criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_sources:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1/source/89a46064-c831-4c9b-acd7-1f0c8d1984de
     * Output: {"id":"89a46064-c831-4c9b-acd7-1f0c8d1984de","name":"192.168.0.2","mle_uuid":"9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSource obj = client.retrieveMleSource("9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1", "89a46064-c831-4c9b-acd7-1f0c8d1984de");
     * </pre>
     */
    public MleSource retrieveMleSource(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid);
        MleSource obj = getTarget().path("mles/{mle_id}/source/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleSource.class);
        return obj;
    }

    /**
     * Searches for host that was used to configure the Mle with the whitelists.
     * @param criteria MleSourceFilterCriteria object specifying the search criteria. Search options supported
     * include UUID of Mle.
     * @return MleSourceCollection having the source of the Mle whitelist specified.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_sources:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1/source
     * Output:{"mle_sources":[{"id":"89a46064-c831-4c9b-acd7-1f0c8d1984de","name":"Server 02","mle_uuid":"9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleSources client = new MleSources(My.configuration().getClientProperties());
     *      MleSourceFilterCriteria criteria = new MleSourceFilterCriteria();
     *      criteria.mleUuid = UUID.valueOf("9831bbe9-e993-4f41-a8d0-3f8b11a9f6f1");
     *      MleSourceCollection searchMleSources = client.searchMleSources(criteria);
     * </pre>
     */
    public MleSourceCollection searchMleSources(MleSourceFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", criteria.mleUuid);
        MleSourceCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/source", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleSourceCollection.class);
        return objCollection;
    }
    
}
