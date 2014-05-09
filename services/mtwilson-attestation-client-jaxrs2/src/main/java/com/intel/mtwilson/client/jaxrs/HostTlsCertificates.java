/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsCertificateFilterCriteria;
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
public class HostTlsCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTlsCertificates(URL url) throws Exception{
        super(url);
    }

    public HostTlsCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostTlsCertificateCollection searchHostTlsCertificate(HostTlsCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", criteria.hostUuid);
        HostTlsCertificateCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/tls-policy/certificates", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsCertificateCollection.class);
        return objCollection;
    }
    
    public HostTlsCertificate retrieveHostTlsCertificate(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", hostUuid);
        // We are passing the host UUID to "id" also even though it will not be used (without this framework treats this call as a 
        // search call instead of a retrieve call. Since there will be only one tlspolicy cert for a host, we can retrieve
        // the tlspolicy cert for the host uniquely with the host uuid itself.
        map.put("id", hostUuid); 
        HostTlsCertificate obj = getTarget().path("hosts/{host_id}/tls-policy/certificates/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostTlsCertificate.class);
        return obj;
    }

    public HostTlsCertificate editHostTlsCertificate(HostTlsCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", obj.getHostUuid().toString());
        map.put("id", obj.getHostUuid().toString()); // See above as to why we are using the value of hostuuid again.
        HostTlsCertificate newObj = getTarget().path("hosts/{host_id}/tls-policy/certificates/{id}")
                .resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON)
                .put(Entity.json(obj), HostTlsCertificate.class);
        return newObj;
    }
    
}
