/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificate;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikCertificateFilterCriteria;
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
public class HostAikCertificates extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostAikCertificates(URL url) throws Exception{
        super(url);
    }

    public HostAikCertificates(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostAikCertificateCollection searchHostAikCertificates(HostAikCertificateFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", criteria.hostUuid);
        HostAikCertificateCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/aik-certificates", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAikCertificateCollection.class);
        return objCollection;
    }
    
    public HostAikCertificate retrieveHostAikCertificate(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", hostUuid);
        // We are passing the host UUID to "id" also even though it will not be used (without this framework treats this call as a 
        // search call instead of a retrieve call. Currently we support only one aik certificate for a host, we can retrieve
        // the aik certificate for the host uniquely with the host uuid itself.
        map.put("id", hostUuid); 
        HostAikCertificate obj = getTarget().path("hosts/{host_id}/aik-certificates/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAikCertificate.class);
        return obj;
    }

    public HostAikCertificate createHostAikCertificate(HostAikCertificate obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", obj.getHostUuid().toString());
        HostAikCertificate newObj = getTarget().path("hosts/{host_id}/aik-certificates").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), HostAikCertificate.class);
        return newObj;
    }
    
}
