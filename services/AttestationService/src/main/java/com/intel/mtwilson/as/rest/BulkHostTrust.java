package com.intel.mtwilson.as.rest;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.business.trust.BulkHostTrustBO;
import com.intel.mtwilson.datatypes.BulkHostTrustResponse;
import com.intel.mtwilson.security.annotations.RolesAllowed;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 * 
 * Example query:
 * http://localhost:8080/AttestationService/resources/hosts
 * /trust?hosts=10.1.71.104&force_verify=false
 * 
 * @author dmagadix
 */
@Stateless
@Path("/hosts/bulk")
public class BulkHostTrust {

        /**
	 * REST Web Service Example: GET
	 * /hosts/trust?hosts=host_name_1
	 * ,host_name_2,host_name_3&force_verify=true
	 * 
	 * @param hosts
	 * @param forceVerify
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_XML })
        @Path("/trust/saml")
        @RolesAllowed({"Attestation", "Report"})
	public String getTrustSaml(
			@QueryParam("hosts") String hosts,
			@QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify,
                        @QueryParam("threads") @DefaultValue("5") Integer threads,
                        @QueryParam("timeout") @DefaultValue("600") Integer timeout) {

		if (hosts == null || hosts.length() == 0) {
			throw new ASException(com.intel.mtwilson.datatypes.ErrorCode.AS_MISSING_INPUT,
					"hosts"  );
		}
                
            Set<String> hostSet = new HashSet<String>();
            hostSet.addAll(Arrays.asList(hosts.split(",")));
                BulkHostTrustBO bulkHostTrustBO = new BulkHostTrustBO(threads, timeout);
		return bulkHostTrustBO.getBulkTrustSaml(hostSet,forceVerify);


	}

        
        	/**
	 * REST Web Service Example: GET
	 * /hosts/trust?hosts=host_name_1
	 * ,host_name_2,host_name_3&force_verify=true
	 * 
	 * @param hosts
	 * @param forceVerify
	 * @return
	 */
        @Path("/trust")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
        @RolesAllowed({"Attestation", "Report"})
	public BulkHostTrustResponse getTrustJson(
			@QueryParam("hosts") String hosts,
			@QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify,
                        @QueryParam("threads") @DefaultValue("5") Integer threads,
                        @QueryParam("timeout") @DefaultValue("600") Integer timeout) {

		if (hosts == null || hosts.length() == 0) {
			throw new ASException(com.intel.mtwilson.datatypes.ErrorCode.AS_MISSING_INPUT,
					"hosts"  );
		}
                
            Set<String> hostSet = new HashSet<String>();
            hostSet.addAll(Arrays.asList(hosts.split(",")));
                 BulkHostTrustBO bulkHostTrustBO = new BulkHostTrustBO(threads, timeout);
		return bulkHostTrustBO.getBulkTrustJson(hostSet,forceVerify);


	}

}
