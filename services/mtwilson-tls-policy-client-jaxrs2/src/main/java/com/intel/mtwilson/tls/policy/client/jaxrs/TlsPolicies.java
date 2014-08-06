/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tls.policy.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicy;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyCollection;
import com.intel.mtwilson.tls.policy.model.HostTlsPolicyFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlsPolicies extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public TlsPolicies(URL url) throws Exception{
        super(url);
    }

    public TlsPolicies(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an new TlsPolicy in the system that could be used during white listing or host registration for establishing trusted communication channel with the host.
     * @param hostTlsPolicy - HostTlsPolicy object that needs to be created. 
     * @return Created HostTlsPolicy object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tls-policies
     * Input: {"name":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest","data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     * "meta":{"digest_algorithm":"SHA-1"}},"private":false}
     * Output: {"id":"3e75091f-4657-496c-a721-8a77931ee9da","name":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest",
     * "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],"meta":{"digest_algorithm":"SHA-1"}},"private":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TlsPolicy being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
     *  TlsPolicies client = new TlsPolicies(My.configuration().getClientProperties());
     *  UUID id = new UUID();
     *  HostTlsPolicy tlsPolicy = new HostTlsPolicy();
     *  tlsPolicy.setId(id);
     *  tlsPolicy.setName("vcenter1_shared_policy");
     *  tlsPolicy.setPrivate(false);
     *  TlsPolicyDescriptor tlsPolicyDescriptor = new TlsPolicyDescriptor();
     *  tlsPolicyDescriptor.setPolicyType("certificate-digest");
     *  tlsPolicyDescriptor.setData(Arrays.asList("d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"));
     *  Map<String, String> metaData = new HashMap<>();
     *  metaData.put("digest_algorithm","SHA-1");
     *  tlsPolicyDescriptor.setMeta(metaData);
     *  tlsPolicy.setDescriptor(tlsPolicyDescriptor);
     *  HostTlsPolicy createTlsPolicy = client.createTlsPolicy(tlsPolicy);
     * </pre>
     */
    public HostTlsPolicy createTlsPolicy(HostTlsPolicy hostTlsPolicy) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostTlsPolicy newObj = getTarget().path("tls-policies").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(hostTlsPolicy), HostTlsPolicy.class);
        return newObj;
    }
    
    /**
     * Deletes the TlsPolicy with the specified UUID from the system. If the policy is associated with any of the registered hosts, then unless a new TLS policy
     * is associated with the host, no communication with happen with the host.
     * @param uuid - UUID of the TlsPolicy that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tls-policies/3e75091f-4657-496c-a721-8a77931ee9da
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TlsPolicy being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
     *  TlsPolicies client = new TlsPolicies(My.configuration().getClientProperties());
     *  client.deleteTlsPolicy("3e75091f-4657-496c-a721-8a77931ee9da");
     * </pre>
     */
    public void deleteTlsPolicy(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("tls-policies/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete TlsPolicy failed");
        }
    }

    /**
     * Deletes the TlsPolicy(s) matching the specified search criteria. 
     * @param criteria HostTlsPolicyFilterCriteria object specifying the search criteria. The search options include
     * id, hostId, nameEqualTo, nameContains, privateEqualTo, commentEqualTo and commentContains.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tls-policies?privateEqualTo=false
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TlsPolicy being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
     *  TlsPolicies client = new TlsPolicies(My.configuration().getClientProperties());
     *  HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
     *  criteria.privateEqualTo = false;
     *  client.deleteTlsPolicy(criteria);
     * </pre>
     */
    public void deleteTlsPolicy(HostTlsPolicyFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("tls-policies", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete TlsPolicy by search criteria failed");
        }
    }
    
    /**
     * Updates the details of the TlsPolicy in the system. All the details of the existing TlsPolciy can be updated.
     * @param tlsPolicy - HostTlsPolicy object details that needs to be updated.
     * @return Updated HostTlsPolicy object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre> 
     * https://server.com:8181/mtwilson/v2/tls-policies/3e75091f-4657-496c-a721-8a77931ee9da
     * Input: {"name":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest","data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     * "meta":{"digest_algorithm":"SHA-1"}},"comment":"Updated with comments","private":false}
     * Output: {"id":"3e75091f-4657-496c-a721-8a77931ee9da","name":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest",
     * "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],"meta":{"digest_algorithm":"SHA-1"}},"comment":"Updated with comments","private":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TlsPolicy being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
     *  TlsPolicies client = new TlsPolicies(My.configuration().getClientProperties());
     *  HostTlsPolicy currentTlsPolicy = client.retrieveTlsPolicy("3e75091f-4657-496c-a721-8a77931ee9da");
     *  currentTlsPolicy.setComment("Updated with comments");
     *  client.editTlsPolicy(currentTlsPolicy);
     * </pre>
     */
    public HostTlsPolicy editTlsPolicy(HostTlsPolicy tlsPolicy) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", tlsPolicy.getId().toString());
        HostTlsPolicy updatedObj = getTarget().path("tls-policies/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(tlsPolicy), HostTlsPolicy.class);
        return updatedObj;
    }
    
     /**
     * Retrieves the details of the existing HostTlsPolicy object with the specified UUID
     * @param uuid - UUID of the HostTlsPolicy to be retrieved
     * @return HostTlsPolicy object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tls-policies/3e75091f-4657-496c-a721-8a77931ee9da
     * Output: {"id":"3e75091f-4657-496c-a721-8a77931ee9da","name":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest",
     * "data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],"meta":{"digest_algorithm":"SHA-1"}},"comment":"Updated with comments","private":false}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TlsPolicy being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
     *  TlsPolicies client = new TlsPolicies(My.configuration().getClientProperties());
     *  HostTlsPolicy currentTlsPolicy = client.retrieveTlsPolicy("3e75091f-4657-496c-a721-8a77931ee9da");
     * </pre>
     */
    public HostTlsPolicy retrieveTlsPolicy(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        HostTlsPolicy obj = getTarget().path("tls-policies/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsPolicy.class);
        return obj;
    }
    
    /**
     * Searches for the TLS policies with the specified set of criteria. The meta data in the output indicates the allowed TLS policies that
     * can be configured. This can be updated in the mtwilson.properties file.
     * @param criteria HostTlsPolicyFilterCriteria object specifying the filter criteria. The search options include
     * id, hostId, nameEqualTo, nameContains, privateEqualTo, commentEqualTo and commentContains. 
     * Also, if the caller wants to retrieve the list of all the registered
     * roles, the filter option can be disabled by setting the filter criteria to false. By default
     * the filter criteria is true. [Ex: /v2/tls-policies?filter=false retrieves the list of all the TlsPolicies]
     * @return HostTlsPolicyCollection with the TlsPolicies that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tls-policies?filter=false
     * Output: {"meta":{"default":null,"allow":["certificate","certificate-digest"],"global":null},"tls_policies":[{"id":"3e75091f-4657-496c-a721-8a77931ee9da",
     * "name":"vcenter1_shared_policy","descriptor":{"policy_type":"certificate-digest","data":["d0 8f 07 b0 5c 6d 78 62 b9 27 48 ff 35 da 27 bf f2 03 b3 c1"],
     * "meta":{"digest_algorithm":"SHA-1"}},"private":false}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  // Need to register the extension of the TlsPolicy being used to authenticate to the MTW server. In the example we are using the Insecure policy.
     *  Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator.class);
     *  TlsPolicies client = new TlsPolicies(My.configuration().getClientProperties());
     *  HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
     *  criteria.privateEqualTo = false;
     *  HostTlsPolicyCollection searchTlsPolicies = client.searchTlsPolicies(criteria);
     * </pre>
     */
    public HostTlsPolicyCollection searchTlsPolicies(HostTlsPolicyFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostTlsPolicyCollection objList = getTargetPathWithQueryParams("tls-policies", criteria).request(MediaType.APPLICATION_JSON).get(HostTlsPolicyCollection.class);
        return objList;
    }
}
