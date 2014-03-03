/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.HostAik;
import com.intel.mtwilson.as.rest.v2.model.HostAikCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikFilterCriteria;
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
public class HostAiks extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostAiks(URL url) {
        //super(url);
    }

    public HostAiks(Properties properties) throws Exception {
        super(properties);
    }
    
    public HostAikCollection searchHostAiks(HostAikFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", criteria.hostUuid.toString());
        HostAikCollection objCollection = getTargetPathWithQueryParams("hosts/{host_id}/aiks", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAikCollection.class);
        return objCollection;
    }
    
    public HostAik retrieveHostAik(String hostUuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("host_id", hostUuid);
        // We are passing the host UUID to "id" also even though it will not be used (without this framework treats this call as a 
        // search call instead of a retrieve call. Since there will be only one aik for a host, we can retrieve
        // the aik for the host uniquely with the host uuid itself.
        map.put("id", hostUuid); 
        HostAik obj = getTarget().path("hosts/{host_id}/aiks/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAik.class);
        return obj;
    }

}
