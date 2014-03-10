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
 *
 * @author ssbangal
 */
public class HostTlsPolicy extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTlsPolicy(URL url) {
        super(url);
    }

    public HostTlsPolicy(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostTlsPolicyCollection searchHostTlsPolicy(HostTlsPolicyFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", criteria.hostUuid);
        HostTlsPolicyCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/tls-policy", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsPolicyCollection.class);
        return objCollection;
    }
    
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
