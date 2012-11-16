package com.intel.mtwilson.as.rest;

import javax.ejb.Stateless;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.intel.mtwilson.as.business.trust.BulkHostsTrustBean;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.datatypes.BulkHostTrustResponse;
import javax.ejb.EJB;

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
@Path("/hosts/bulk/trust")
public class HostsTrust {
        @EJB(name="MultipleHostsTrustBean")
        BulkHostsTrustBean multipleHostsTrustBean;
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
        @Path("/saml")
	public String getTrustSaml(
			@QueryParam("hosts") String hosts,
			@QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify) {

		if (hosts == null || hosts.length() == 0) {
			throw new ASException(com.intel.mtwilson.datatypes.ErrorCode.AS_MISSING_INPUT,
					"hosts"  );
		}
		return multipleHostsTrustBean.getTrustSaml(hosts,forceVerify);


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
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public BulkHostTrustResponse getTrustJson(
			@QueryParam("hosts") String hosts,
			@QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify) {

		if (hosts == null || hosts.length() == 0) {
			throw new ASException(com.intel.mtwilson.datatypes.ErrorCode.AS_MISSING_INPUT,
					"hosts"  );
		}
		return multipleHostsTrustBean.getTrustJson(hosts,forceVerify);


	}

}
