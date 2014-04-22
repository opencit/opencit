/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

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
     * Searches for the TLS Policies of host with the specified set of criteria
     * @param criteria - <code> HostTlsPolicyFilterCriteria </code> expressing the filter criteria
     *      The possible search options include hostUuid specification
     * @return <code> HostTlsPolicyCollection </code> that meets the filter criteria
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e/tls-policy
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"oems":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * 
     */
    public HostTlsPolicyCollection searchHostTlsPolicy(HostTlsPolicyFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", criteria.hostUuid);
        HostTlsPolicyCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/tls-policy", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsPolicyCollection.class);
        return objCollection;
    }
    
     /**
     * Retrieves the TLS Policy with the specified host uuid
     * @param hostUuid - UUID of the Host to be retrieved from the backend
     * @return <code>HostTlsPolicy</code> that is retrieved from the backend
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e/tls-policy
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   tls_policies: [1]
     *   0:{
     *   host_uuid: "2d026d64-ec08-4406-8a2d-3f90f2addd5e"
     *   name: "TRUST_FIRST_CERTIFICATE"
     *   }
     *  }
     */
    public com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy retrieveHostTlsPolicy(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
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
     * Edits/Updates the Host Tls Policy in the database. 
     * @param HostTlsPolicy - Host Tls Policy that needs to be updated.
     * @return <code> HostTlsPolicy </code> post updation with the specified properties.
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e/tls-policy/2d026d64-ec08-4406-8a2d-3f90f2addd5e
     * <p>
     * <i>Sample Input</i><br>
     *	{"name":"TRUST_FIRST_CERTIFICATE_CHGED"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     * host_uuid: "2d026d64-ec08-4406-8a2d-3f90f2addd5e"
     * name: "TRUST_FIRST_CERTIFICATE_CHGED"
     * }
     *      
     */
    public com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy editHostTlsPolicy(com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", obj.getHostUuid());
        map.put("id", obj.getHostUuid().toString()); // See above as to why we are using the value of hostuuid again.
        com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy newObj = getTarget().path("hosts/{host_id}/tls-policy/{id}")
                .resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON)
                .put(Entity.json(obj), com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy.class);
        return newObj;
    }
    
}
