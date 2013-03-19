package com.intel.mtwilson.as.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.intel.mtwilson.as.business.HostBO;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.helper.ASComponentFactory;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.security.annotations.*;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Web Service *
 */
@Stateless
@Path("/hosts")
public class Host {

        private HostBO hostBO = new ASComponentFactory().getHostBO();

        /**
         * Returns the location of a host.
         *
         * Sample request: GET
         * http://localhost:8080/AttestationService/resources/hosts/location?hostName=Some+TXT+Host
         *
         * Sample output: San Jose
         *
         * @param hostName unique name of the host to query
         * @return the host location
         */
        @RolesAllowed({"Attestation", "Report"})
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/location")
        public HostLocation getLocation(@QueryParam("hostName") String hostName) {
                return new ASComponentFactory().getHostTrustBO().getHostLocation(new Hostname(hostName)); // datatype.Hostname            
        }

        @RolesAllowed({"Attestation", "Security"})
        @POST
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/location")
        public String addLocation(HostLocation hlObj) {
                Boolean result = new ASComponentFactory().getHostTrustBO().addHostLocation(hlObj);
                return Boolean.toString(result);
        }

        /**
         * Returns the trust status of a host.
         *
         * Sample request: GET
         * http://localhost:8080/AttestationService/resources/hosts/trust?hostName=Some+TXT+Host
         *
         * Sample output for untrusted host: BIOS:0,VMM:0
         *
         * Sample output for trusted host: BIOS:1,VMM:1
         *
         * @param hostName unique name of the host to query
         * @return a string like BIOS:0,VMM:0 representing the trust status
         */
        @RolesAllowed({"Attestation", "Report"})
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/trust")
        public HostTrustResponse get(@QueryParam("hostName") String hostName) {
                try {
                        // 0.5.1 returned MediaType.TEXT_PLAIN string like "BIOS:0,VMM:0" :  return new HostTrustBO().getTrustStatusString(new Hostname(hostName)); // datatype.Hostname            
                        Hostname hostname = new Hostname(hostName);
                        HostTrustStatus trust = new ASComponentFactory().getHostTrustBO().getTrustStatus(hostname);
                        return new HostTrustResponse(hostname, trust);
                } catch (ASException e) {
                        throw e;
                } catch (Exception e) {
                        throw new ASException(e);
                }
        }

        /**
         * Adds a new host to the database. This action involves contacting the
         * host to obtain trust-related information. If the host is offline, the
         * request will fail.
         *
         * Required parameters for all hosts are host name, BIOS name and
         * version, and VMM name and version. If the host is an ESX host then
         * the vCenter connection URL is also required. Otherwise, the host IP
         * address and port are required. Host description and contact email are
         * optional.
         *
         * Parameter names: HostName IPAddress Port BIOS_Name BIOS_Version
         * BIOS_Oem VMM_Name VMM_Version VMM_OSName VMM_OSVersion
         * AddOn_Connection_String Description Email
         *
         * @param host a form containing the above parameters
         * @return error status
         *
         * Response: {"error_code":"",error_message:""}
         */
        @RolesAllowed({"Attestation"})
        @POST
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        public HostResponse post(TxtHostRecord hostRecord) {
                return hostBO.addHost(new TxtHost(hostRecord));
        }


        /**
         * Updates an existing host in the database. This action involves
         * contacting the host to obtain trust-related information. If the host
         * is offline, the request will fail.
         *
         * Required parameters for all hosts are host name, BIOS name and
         * version, and VMM name and version. If the host is an ESX host then
         * the vCenter connection URL is also required. Otherwise, the host IP
         * address and port are required. Host description and contact email are
         * optional.
         *
         * Parameter names: HostName IPAddress Port BIOS_Name BIOS_Version
         * VMM_Name VMM_Version AddOn_Connection_String Description Email
         *
         *
         * @param host a form containing the above parameters
         * @return error status
         */
        @RolesAllowed({"Attestation"})
        @PUT
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        public HostResponse put(TxtHostRecord hostRecord) {
                return hostBO.updateHost(new TxtHost(hostRecord));
        }

        /**
         * Deletes a host from the database.
         *
         * Example request: DELETE
         * http://localhost:8080/AttestationService/resources/hosts?hostName=Some+TXT+Host
         *
         * @param hostName the unique host name of the host to delete
         * @return error status
         */
        @RolesAllowed({"Attestation"})
        @DELETE
//    @Consumes({"text/html"})
        @Produces({MediaType.APPLICATION_JSON})
        public HostResponse delete(@QueryParam("hostName") String hostName) {
                return hostBO.deleteHost(new Hostname(hostName));
        }

        @RolesAllowed({"Attestation", "Report", "Security"})
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        public List<TxtHostRecord> queryForHosts(@QueryParam("searchCriteria") String searchCriteria) {
                return hostBO.queryForHosts(searchCriteria);
        }
}
