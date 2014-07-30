/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class PollHosts extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public PollHosts(URL url) throws Exception{
        super(url);
    }

    public PollHosts(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Retrieves the trust status of the list of hosts specified. This API is added for the OpenStack integration.
     * @param obj OpenStackHostTrustLevelQuery object with the UUID of the host for which the attestation has to be done. 
     * @return HostAttestation object with the details trust report. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:search,retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/integrations/openstack/PollHosts
     * Input: {"hosts":["192.168.0.2"]} 
     * Output: {"hosts":[{"host_name":"192.168.0.2","trust_lvl":"trusted","vtime":"Sat May 3 13:05:38 2014"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   PollHosts client = new PollHosts(My.configuration().getClientProperties());
     *   OpenStackHostTrustLevelQuery input = new OpenStackHostTrustLevelQuery();
     *   input.setHosts(new String[] {"192.168.0.2"});
     *   OpenStackHostTrustLevelReport openStackHostTrustReport = client.getOpenStackHostTrustReport(input);
     * </pre>
     */    
    public OpenStackHostTrustLevelReport getOpenStackHostTrustReport(OpenStackHostTrustLevelQuery obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        OpenStackHostTrustLevelReport result = getTarget().path("integrations/openstack/PollHosts").request(MediaType.APPLICATION_JSON).
                post(Entity.json(obj), OpenStackHostTrustLevelReport.class);
        return result;
    }
    
}
