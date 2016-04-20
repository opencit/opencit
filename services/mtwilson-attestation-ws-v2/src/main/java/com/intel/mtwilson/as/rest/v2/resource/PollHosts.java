/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.launcher.ws.ext.V2;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author ssbangal
 */
@V2
@Path("/integrations/openstack/PollHosts")
public class PollHosts  {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PollHosts.class);
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OpenStackHostTrustLevelReport getOpenStackHostTrustReport(OpenStackHostTrustLevelQuery request) {
        log.debug("Got request to retrieve the attestation report for {} number of hosts.", request.hosts.length);
        ValidationUtil.validate(request);
        OpenStackHostTrustLevelReport pollHosts = new HostTrustBO().getPollHosts(request);
        log.debug("Successfully processed the attestation report for {} number of hosts.", pollHosts.pollHosts.size());        
        return pollHosts;
    }
            
}
