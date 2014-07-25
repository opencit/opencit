/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tpm.endorsement.client.jaxrs;

import com.intel.mtwilson.tpm.endorsement.client.jaxrs.*;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementCollection;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TpmEndorsements extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public TpmEndorsements(URL url) throws Exception{
        super(url);
    }

    public TpmEndorsements(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an new TpmEndorsement in the system that could be during white listing or host registration for establishing trusted communication channel with the host.
     * @param hostTpmEndorsement - TpmEndorsement object that needs to be created. 
     * @return Created TpmEndorsement object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_endorsements:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tpm-endorsements
     * Input: {"issuer":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest","data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     * "meta":{"digest_algorithm":"SHA-1"}},"private":false}
     * Output: {"id":"3e75091f-4657-496c-a721-8a77931ee9da","issuer":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest",
     * "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],"meta":{"digest_algorithm":"SHA-1"}},"private":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TpmEndorsement being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TpmEndorsementCreator.class, com.intel.mtwilson.tpm.endorsement.creator.impl.InsecureTpmEndorsementCreator.class);
     *  TpmEndorsements client = new TpmEndorsements(My.configuration().getClientProperties());
     *  UUID id = new UUID();
     *  TpmEndorsement tlsPolicy = new TpmEndorsement();
     *  tlsPolicy.setId(id);
     *  tlsPolicy.setName("vcenter1_shared_policy");
     *  tlsPolicy.setPrivate(false);
     *  TpmEndorsementDescriptor tlsPolicyDescriptor = new TpmEndorsementDescriptor();
     *  tlsPolicyDescriptor.setPolicyType("certificate-digest");
     *  tlsPolicyDescriptor.setData(Arrays.asList("d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"));
     *  Map<String, String> metaData = new HashMap<>();
     *  metaData.put("digest_algorithm","SHA-1");
     *  tlsPolicyDescriptor.setMeta(metaData);
     *  tlsPolicy.setDescriptor(tlsPolicyDescriptor);
     *  TpmEndorsement createTpmEndorsement = client.createTpmEndorsement(tlsPolicy);
     * </pre>
     */
    public TpmEndorsement createTpmEndorsement(TpmEndorsement hostTpmEndorsement) {
        log.debug("target: {}", getTarget().getUri().toString());
        TpmEndorsement newObj = getTarget().path("tpm-endorsements").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(hostTpmEndorsement), TpmEndorsement.class);
        return newObj;
    }
    
    /**
     * Deletes the TpmEndorsement with the specified UUID from the system. If the policy is associated with any of the registered hosts, then unless a new TLS policy
     * is associated with the host, no communication with happen with the host.
     * @param uuid - UUID of the TpmEndorsement that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_endorsements:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tpm-endorsements/3e75091f-4657-496c-a721-8a77931ee9da
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TpmEndorsement being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TpmEndorsementCreator.class, com.intel.mtwilson.tpm.endorsement.creator.impl.InsecureTpmEndorsementCreator.class);
     *  TpmEndorsements client = new TpmEndorsements(My.configuration().getClientProperties());
     *  client.deleteTpmEndorsement("3e75091f-4657-496c-a721-8a77931ee9da");
     * </pre>
     */
    public void deleteTpmEndorsement(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("tpm-endorsements/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete TpmEndorsement failed");
        }
    }

    /**
     * Deletes the TpmEndorsement(s) matching the specified search criteria. 
     * @param criteria TpmEndorsementFilterCriteria object specifying the search criteria. The search options include
     * id, hardwareUuid, issuerEqualTo, issuerContains, revokedEqualTo, commentEqualTo and commentContains.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_endorsements:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tpm-endorsements?revokedEqualTo=false
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmEndorsements client = new TpmEndorsements(My.configuration().getClientProperties());
     *  TpmEndorsementFilterCriteria criteria = new TpmEndorsementFilterCriteria();
     *  criteria.revokedEqualTo = false;
     *  client.deleteTpmEndorsement(criteria);
     * </pre>
     */
    public void deleteTpmEndorsement(TpmEndorsementFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("tpm-endorsements", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete TpmEndorsement by search criteria failed");
        }
    }
    
    /**
     * Updates the details of the TpmEndorsement in the system. All the details of the existing TlsPolciy can be updated.
     * @param tlsPolicy - TpmEndorsement object details that needs to be updated.
     * @return Updated TpmEndorsement object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_endorsements:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre> 
     * https://server.com:8181/mtwilson/v2/tpm-endorsements/3e75091f-4657-496c-a721-8a77931ee9da
     * Input: 
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmEndorsements client = new TpmEndorsements(My.configuration().getClientProperties());
     *  TpmEndorsement currentTpmEndorsement = client.retrieveTpmEndorsement("3e75091f-4657-496c-a721-8a77931ee9da");
     *  currentTpmEndorsement.setComment("Updated with comments");
     *  client.editTpmEndorsement(currentTpmEndorsement);
     * </pre>
     */
    public TpmEndorsement editTpmEndorsement(TpmEndorsement tlsPolicy) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", tlsPolicy.getId().toString());
        TpmEndorsement updatedObj = getTarget().path("tpm-endorsements/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(tlsPolicy), TpmEndorsement.class);
        return updatedObj;
    }
    
     /**
     * Retrieves the details of the existing TpmEndorsement object with the specified UUID
     * @param uuid - UUID of the TpmEndorsement to be retrieved
     * @return TpmEndorsement object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_endorsements:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tpm-endorsements/3e75091f-4657-496c-a721-8a77931ee9da
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmEndorsements client = new TpmEndorsements(My.configuration().getClientProperties());
     *  TpmEndorsement currentTpmEndorsement = client.retrieveTpmEndorsement("3e75091f-4657-496c-a721-8a77931ee9da");
     * </pre>
     */
    public TpmEndorsement retrieveTpmEndorsement(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        TpmEndorsement obj = getTarget().path("tpm-endorsements/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TpmEndorsement.class);
        return obj;
    }
    
    /**
     * Searches for the TLS policies with the specified set of criteria.
     * @param criteria TpmEndorsementFilterCriteria object specifying the filter criteria. The search options include
     * id, hardwareUuid, issuerEqualTo, issuerContains, revokedEqualTo, commentEqualTo and commentContains. 
     * Also, if the caller wants to retrieve the list of all the registered
     * roles, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/tpm-endorsements?filter=false retrieves the list of all the TpmEndorsements]
     * @return TpmEndorsementCollection with the TpmEndorsements that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_endorsements:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tpm-endorsements?filter=false
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmEndorsementFilterCriteria criteria = new TpmEndorsementFilterCriteria();
     *  criteria.revokedEqualTo = false;
     *  TpmEndorsementCollection searchTpmEndorsements = client.searchTpmEndorsements(criteria);
     * </pre>
     */
    public TpmEndorsementCollection searchTpmEndorsements(TpmEndorsementFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        TpmEndorsementCollection objList = getTargetPathWithQueryParams("tpm-endorsements", criteria).request(MediaType.APPLICATION_JSON).get(TpmEndorsementCollection.class);
        return objList;
    }
}
