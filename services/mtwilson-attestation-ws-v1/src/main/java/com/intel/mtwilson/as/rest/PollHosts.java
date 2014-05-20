
package com.intel.mtwilson.as.rest;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.security.annotations.*;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.launcher.ws.ext.V1;
//import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * REST Web Service
 * 
 * Currently the "count" and "pcrMask" fields of HostTrustInput are ignored by PollHosts and HostTrustBO,
 * so removed it from the pollHosts method documentation.
 * 
 */
@V1
//@Stateless
@Path("/AttestationService/resources/PollHosts")
public class PollHosts {
    private static final Logger log = LoggerFactory.getLogger(PollHosts.class);
    
    /**
     * Returns information about the trust status of the specified hosts.
     * 
     * Sample request:
     * Content-Type: application/json
     * POST http://localhost:8080/AttestationService/resources/PollHosts
     * {
     *   "hosts":["host name 1", "host name 2", "host name 3"]
     * }
     * 
     * Sample response (JSON format):
     * {
     *   "count":3,
     *   "hosts":{
     *     "host name 1": {
     *       "trust_lvl": "unknown",
     *       "timestamp": "Tue Feb 14 09:02:48 2012"
     *     },
     *     "host name 2": {
     *       "trust_lvl": "untrusted",
     *       "timestamp": "Tue Feb 14 09:02:48 2012"
     *     },
     *     "host name 3":{
     *       "trust_lvl": "trusted",
     *       "timestamp": "Tue Feb 14 09:02:48 2012"
     *     }
     *   }
     * }
     * 
     * @param input
     * @return the trust status of the specified hosts
     */
    //@RolesAllowed({"Attestation","Report"})
//    @RequiresPermissions("poll_hosts:create,retrieve") 
    @RequiresPermissions("host_attestations:search,retrieve")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public OpenStackHostTrustLevelReport pollMultipleHosts(OpenStackHostTrustLevelQuery input) {
        try {
            ValidationUtil.validate(input);
            log.debug("PCR Mask {}", input.pcrMask);
            return ASComponentFactory.getHostTrustBO().getPollHosts(input);
        }
        catch(ASException e) {
            throw e;
        }
        catch(Exception ex) {
            // throw new ASException(e);
            log.error("Error during retrieval of host trust status.", ex);
            throw new ASException(ErrorCode.AS_HOST_TRUST_ERROR, ex.getClass().getSimpleName());
        }
    }

    
}
