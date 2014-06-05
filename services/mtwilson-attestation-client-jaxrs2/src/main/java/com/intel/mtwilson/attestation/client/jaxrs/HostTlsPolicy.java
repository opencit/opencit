/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>HostTlsPolicy</code> is the class that allows searching, accessing and updating TLS Policies of hosts
 * @author ssbangal 
 */
public class HostTlsPolicy extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTlsPolicy(URL url) throws Exception{
        super(url);
    }

    public HostTlsPolicy(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Updates the host's TLS policy for the specified host in the system. 
     * @param HostTlsPolicy that needs to be updated.
     * @return Updated HostTlsPolicy object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/de07c08a-7fc6-4c07-be08-0ecb2f803681/tls-policy/de07c08a-7fc6-4c07-be08-0ecb2f803681
     * Input: {"name":"INSECURE"}
     * Output: {"host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681","name":"INSECURE"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * HostTlsPolicy client = new HostTlsPolicy(My.configuration().getClientProperties());
     * com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy obj = new com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy();
     * obj.setHostUuid("de07c08a-7fc6-4c07-be08-0ecb2f803681");
     * obj.setName("INSECURE");
     * com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy editHostTlsPolicy = client.editHostTlsPolicy(obj);
     * </pre>     
     */
    public com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy editHostTlsPolicy(com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", obj.getHostUuid());
        map.put("id", obj.getHostUuid()); // See above as to why we are using the value of hostuuid again.
        com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy newObj = getTarget().path("hosts/{host_id}/tls-policy/{id}")
                .resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON)
                .put(Entity.json(obj), com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy.class);
        return newObj;
    }
        
     /**
     * Retrieves the TLS policy associated with the specified host.
     * @param hostUuid - UUID of the host.
     * @return <code>HostTlsPolicy</code> that is retrieved from the system.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts//de07c08a-7fc6-4c07-be08-0ecb2f803681/tls-policy/de07c08a-7fc6-4c07-be08-0ecb2f803681
     * Output: {"tls_policies":[{"host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681","name":"TRUST_FIRST_CERTIFICATE"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy retrieveHostTlsPolicy = client.retrieveHostTlsPolicy("de07c08a-7fc6-4c07-be08-0ecb2f803681");
     * </pre>
     */
    public com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy retrieveHostTlsPolicy(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", hostUuid);
        // We are passing the host UUID to "id" also even though it will not be used (without this framework treats this call as a 
        // search call instead of a retrieve call. Since there will be only one tlspolicy for a host, we can retrieve
        // the tlspolicy for the host uniquely with the host uuid itself.
        map.put("id", hostUuid);
        com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy obj = getTarget().path("hosts/{host_id}/tls-policy/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy.class);
        return obj;
    }
    
     /**
     * Searches for the TLS policy associated with the specified host.
     * @param HostTlsPolicyFilterCriteria object specifying the filter criteria. The possible search options include host's uuid.
     * @return <code> HostTlsPolicyCollection </code> that meets the filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_tls_policies:search
     * @mtwContentTypeReturned JSON/XML/YAML/SAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/de07c08a-7fc6-4c07-be08-0ecb2f803681/tls-policy
     * Output: {"tls_policies":[{"host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681","name":"TRUST_FIRST_CERTIFICATE"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     * HostTlsPolicyFilterCriteria criteria = new HostTlsPolicyFilterCriteria();
     * criteria.hostUuid = UUID.valueOf("de07c08a-7fc6-4c07-be08-0ecb2f803681");
     * HostTlsPolicyCollection searchHostTlsPolicy = client.searchHostTlsPolicy(criteria);
     * </pre>
     */
    public HostTlsPolicyCollection searchHostTlsPolicy(HostTlsPolicyFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_id", criteria.hostUuid);
        HostTlsPolicyCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/tls-policy", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsPolicyCollection.class);
        return objCollection;
    }
    
}
