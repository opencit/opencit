package com.intel.mtwilson.as.rest;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.*;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import java.security.cert.X509Certificate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.common.ValidationException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.security.annotations.*;
import java.io.IOException;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mtwilson.launcher.ws.ext.V1;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service *
 * 
 * BACKWARD COMPATIBILITY:
 * Some POST and PUT methods accept TxtHost2 or TxtHostRecord2 with extended fields.
 * However, this class MUST NOT respond with those to the client because it would
 * break v1 clients. The v1 contract does not allow extra fields.
 * Therefore this class responds with TxtHost and TxtHostRecord.
 * 
 */
@V1
//@Stateless
@Path("/AttestationService/resources/hosts")
public class Host {

        private HostBO hostBO = ASComponentFactory.getHostBO();
        private Logger log = LoggerFactory.getLogger(getClass());

        /**
         * Returns the location of a host.
         *
         * Sample request: GET
         * http://localhost:8080/AttestationService/resources/hosts/location?hostName=Some+TXT+Host
         *
         * Sample output: San Jose
         *
         * @param inputHostname unique name of the host to query
         * @return the host location
         */
        //@RolesAllowed({"Attestation", "Report"})
        @RequiresPermissions("hosts:retrieve")        
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/location")
        public HostLocation getLocation(@QueryParam("hostName") String hostName) {
            if( !ValidationUtil.isValidWithRegex(hostName, RegexPatterns.IPADDR_FQDN) ) {throw new ValidationException("Invalid hostName parameter"); }
            else return ASComponentFactory.getHostTrustBO().getHostLocation(new Hostname(hostName)); // datatype.Hostname            
        }

        //@RolesAllowed({"Attestation", "Security"})
        @RequiresPermissions("hosts:store")        
        @POST
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/location")
        public String addLocation(HostLocation hlObj) {
                ValidationUtil.validate(hlObj);
                Boolean result = ASComponentFactory.getHostTrustBO().addHostLocation(hlObj);
                return Boolean.toString(result);
        }

    //@RolesAllowed({"Attestation","Report"})
    @RequiresPermissions("host_attestations:create,retrieve")            
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/aik-{aik}/trust.json")
    public HostTrustResponse getTrustByAik(@PathParam("aik")String aikFingerprint) {
        ValidationUtil.validate(aikFingerprint);
        if( aikFingerprint == null || aikFingerprint.isEmpty() ) { throw new ValidationException("Missing hostNames parameter"); }
                else {
        try {
            // 0.5.1 returned MediaType.TEXT_PLAIN string like "BIOS:0,VMM:0" :  return new HostTrustBO().getTrustStatusString(new Hostname(hostName)); // datatype.Hostname            
            Sha1Digest aikId = new Sha1Digest(aikFingerprint);
            if( aikId.isValid() ) {
                HostTrustStatus trust = ASComponentFactory.getHostTrustBO().getTrustStatusByAik(aikId);
                return new HostTrustResponse(new Hostname(aikId.toString()), trust);                
            }
            throw new ASException(ErrorCode.HTTP_INVALID_REQUEST, "Invalid AIK fingerprint: must be SHA1 digest");
        }
        catch(ASException e) {
            throw e;
        }catch(Exception ex) {
            // throw new ASException(e);
            log.error("Error during retrieval of host trust status.", ex);
            throw new ASException(ErrorCode.AS_HOST_TRUST_ERROR, ex.getClass().getSimpleName());
        }
        }
    }
    
    //@RolesAllowed({"Attestation","Report"})
    @RequiresPermissions("host_attestations:create,retrieve")            
    @GET
    @Produces({"application/samlassertion+xml"})
    @Path("/aik-{aik}/trust.saml")
    public String getSamlByAik(
            @PathParam("aik")String aikFingerprint,
            @QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify
            ) throws IOException {
        
        try {
            // 0.5.1 returned MediaType.TEXT_PLAIN string like "BIOS:0,VMM:0" :  return new HostTrustBO().getTrustStatusString(new Hostname(hostName)); // datatype.Hostname            
            Sha1Digest aikId = new Sha1Digest(aikFingerprint);
            if( aikId.isValid() ) {
                log.debug("getSamlByAik calling getTrustWithSamlByAik for aik {} force_verify {}", aikFingerprint, forceVerify);
                return ASComponentFactory.getHostTrustBO().getTrustWithSamlByAik(aikId, forceVerify);
            }
            throw new ASException(ErrorCode.HTTP_INVALID_REQUEST, "Invalid AIK fingerprint: must be SHA1 digest");
        }
        catch(ASException e) {
            throw e;
        }catch(Exception e) {
            throw new ASException(e);
        }
    }
    
    
    /**
     * Implements SAFE AR "closing the loop" aka "tls-enforced attestation" and "host bind-data public key".
     * Given SHA1(AIK), returns DER-ENCODED X509 CERTIFICATE corresponding to the host in its current trust
     * policy -- even if the host is not currently trusted, the returned certificate corresponds to the current
     * trust policy for the host so that when the host becomes trusted the certificate will be valid.
     * If the whitelist for the host changes you will have to obtain a new certificate.
     * @author jbuhacoff
     * @since 1.2
     * @param aikFingerprint
     * @return 
     */
    //@RolesAllowed({"Attestation","Report"})
    @RequiresPermissions("host_aik_certificates:retrieve")            
    @GET
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @Path("/aik-{aik}/trustcert.x509")
    public byte[] getCurrentTrustCertificateByAik(@PathParam("aik")String aikFingerprint) {
        ValidationUtil.validate(aikFingerprint);
        if( aikFingerprint == null || aikFingerprint.isEmpty() ) { throw new ValidationException("Missing hostNames parameter"); }
        else{
        try {
            // 0.5.1 returned MediaType.TEXT_PLAIN string like "BIOS:0,VMM:0" :  return new HostTrustBO().getTrustStatusString(new Hostname(hostName)); // datatype.Hostname            
            Sha1Digest aikId = new Sha1Digest(aikFingerprint);
            if( aikId.isValid() ) {
                TblHosts host = ASComponentFactory.getHostBO().getHostByAik(aikId);
                KeystoreCertificateRepository repository = new KeystoreCertificateRepository(host.getTlsKeystoreResource(),My.configuration().getTlsKeystorePassword()); 
                List<X509Certificate> certificates = repository.getCertificates(); // guaranteed not to be null, but may be empty
                for(X509Certificate certificate : certificates) {
                    // we are looking for a certificate that is marked with the AIK;   the other one is the trust agent's or vcenter's ssl;    for now we are putting the AIK fingerprint in the subject CN
                    if( certificate.getSubjectX500Principal().getName().contains("CN="+aikId.toString()) ) {
                        return certificate.getEncoded();
                    }
                }
                throw new ASException(ErrorCode.HTTP_NOT_FOUND, "Host does not have a certificate under that AIK");
            }
            throw new ASException(ErrorCode.HTTP_INVALID_REQUEST, "Invalid AIK fingerprint: must be SHA1 digest");
        }
        catch(ASException e) {
            throw e;
        }catch(Exception ex) {
            // throw new ASException(e);
            log.error("Error during retrieval of host trust certificate.", ex);
            throw new ASException(ErrorCode.AS_HOST_TRUST_CERT_ERROR, ex.getClass().getSimpleName());
        }
        }
    }
    
    /**
     * Adds a new host to the database. This action involves contacting the host
     * to obtain trust-related information. If the host is offline, the request
     * will fail.
     * 
     * Required parameters for all hosts are host name, BIOS name and version, 
     * and VMM name and version. If the host is an ESX host then the vCenter
     * connection URL is also required. Otherwise, the host IP address and port
     * are required. Host description and contact email are optional.
     * 
     * Parameter names:
     *   HostName
     *   IPAddress
     *   Port
     *   BIOS_Name
     *   BIOS_Version
     *   BIOS_Oem
     *   VMM_Name
     *   VMM_Version
     *   VMM_OSName
     *   VMM_OSVersion
     *   AddOn_Connection_String
     *   Description
     *   Email
     * 
     * @param host a form containing the above parameters
     * @return error status
     * 
     * Response:
     * {"error_code":"",error_message:""}
     */
    //@RolesAllowed({"Attestation"})
    @RequiresPermissions("hosts:create") 
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public HostResponse post(TxtHostRecord hostRecord) { 
        ValidationUtil.validate(hostRecord);
        if( hostRecord == null || hostRecord.HostName == null || hostRecord.HostName.isEmpty() ) { throw new ValidationException("Missing hostNames parameter"); }
        else return hostBO.addHost(new TxtHost(hostRecord), null, null, null); 
    }
    
    
    /**
     * Updates an existing host in the database. This action involves contacting 
     * the host to obtain trust-related information. If the host is offline, the 
     * request will fail.
     * 
     * Required parameters for all hosts are host name, BIOS name and version, 
     * and VMM name and version. If the host is an ESX host then the vCenter
     * connection URL is also required. Otherwise, the host IP address and port
     * are required. Host description and contact email are optional.
     * 
     * Parameter names:
     *   HostName
     *   IPAddress
     *   Port
     *   BIOS_Name
     *   BIOS_Version
     *   VMM_Name
     *   VMM_Version
     *   AddOn_Connection_String
     *   Description
     *   Email
     * 
     * 
     * @param host a form containing the above parameters
     * @return error status
     */
    //@RolesAllowed({"Attestation"})
    @RequiresPermissions("hosts:store") 
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public HostResponse put(TxtHostRecord hostRecord) {
            ValidationUtil.validate(hostRecord);
            if( hostRecord == null || hostRecord.HostName == null || hostRecord.HostName.isEmpty() ) { throw new ValidationException("Missing hostNames parameter"); }
            else return hostBO.updateHost(new TxtHost(hostRecord), null, null, null);
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
         * @param forceVerify
         * @return a string like BIOS:0,VMM:0 representing the trust status
         */
        //@RolesAllowed({"Attestation", "Report"})
        @RequiresPermissions({"host_attestations:retrieve"})            
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        @Path("/trust")
        public HostTrustResponse get(
                @QueryParam("hostName") String hostName,
                @QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify) throws ASException {
                
            if( !ValidationUtil.isValidWithRegex(hostName, RegexPatterns.IPADDR_FQDN) ) {throw new ValidationException("Invalid hostName parameter"); }
                
                try {
                        // 0.5.1 returned MediaType.TEXT_PLAIN string like "BIOS:0,VMM:0" :  return new HostTrustBO().getTrustStatusString(new Hostname(hostName)); // datatype.Hostname            
                        Hostname hostname = new Hostname(hostName);
                        if(hostname.isValid()){
                            //HostTrustStatus trust = ASComponentFactory.getHostTrustBO().getTrustStatus(hostname);
                            HostTrustStatus trust = ASComponentFactory.getHostTrustBO().getTrustStatusWithCache(hostName, forceVerify);
                        return new HostTrustResponse(hostname, trust);
                        }
                        else throw new ASException(ErrorCode.AS_MISSING_INPUT, "hostName");
                } catch (ASException e) {
                        throw e;
                } catch (Exception ex) {
                        // throw new ASException(e);
                        log.error("Error during retrieval of host trust status.", ex);
                        throw new ASException(ErrorCode.AS_HOST_TRUST_ERROR, ex.getClass().getSimpleName());
                }
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
        //@RolesAllowed({"Attestation"})
        @RequiresPermissions("hosts:delete") 
        @DELETE
//    @Consumes({"text/html"})
        @Produces({MediaType.APPLICATION_JSON})
        public HostResponse delete(@QueryParam("hostName") String hostName) {
            if( !ValidationUtil.isValidWithRegex(hostName, RegexPatterns.IPADDR_FQDN) ) {throw new ValidationException("Invalid hostName parameter"); }
            else return hostBO.deleteHost(new Hostname(hostName), null);
        }

        /**
         * 
         * @param searchCriteria optional, a string that would be contained in the host name;  if not specified you will get a list of all the hosts
         * @return list of hosts whose hostname contains the value specified by searchCriteria;  in SQL terms, WHERE hostname LIKE '%searchCriteria%'
         */
        //@RolesAllowed({"Attestation", "Report", "Security"})
        @RequiresPermissions("hosts:search") 
        @GET
        @Produces({MediaType.APPLICATION_JSON})
        public List<TxtHostRecord> queryForHosts(
                    @QueryParam("searchCriteria") String searchCriteria,
                    @QueryParam("includeHardwareUuid")  @DefaultValue("false") boolean includeHardwareUuid,
                    @QueryParam("includeTlsPolicy")  @DefaultValue("false") boolean includeTlsPolicy) {
            log.debug("queryForHosts api searchCriteria["+searchCriteria+"] ");
            ValidationUtil.validate(searchCriteria);
                //if( searchCriteria == null || searchCriteria.isEmpty() ) { throw new ValidationException("Missing hostNames parameter"); }
                //else 
            List<TxtHostRecord> resultset;
            if(includeHardwareUuid) {
                resultset = hostBO.queryForHosts(searchCriteria,includeHardwareUuid);
            }else{
                resultset = hostBO.queryForHosts(searchCriteria);
            }
            
            if( !includeTlsPolicy ) {
                for(TxtHostRecord record : resultset) {
                    record.tlsPolicyChoice = null;
                }
            }
            return resultset;
        }

        // The following 2 APIs were being used internally. Since we are directly calling into the Attestation layer BO from the Management service
        // we do not need this API anymore.
        
        //@RolesAllowed({"Attestation"})
//        @RequiresPermissions({"hosts:create","mles:search"}) 
//        @POST
//        @Consumes({MediaType.APPLICATION_JSON})
//        @Produces({MediaType.APPLICATION_JSON})
//        @Path("/mle")
//        public HostResponse registerHostByFindingMLE(TxtHostRecord hostRecord) {
//                ValidationUtil.validate(hostRecord);
//                return ASComponentFactory.getHostTrustBO().getTrustStatusOfHostNotInDBAndRegister(hostRecord);
//        }
//
//        //@RolesAllowed({"Attestation"})
//        @RequiresPermissions({"hosts:create","mles:search"}) 
//        @POST
//        @Consumes({MediaType.APPLICATION_JSON})
//        @Produces({MediaType.APPLICATION_JSON})
//        @Path("/mle/verify")
//        public String checkMatchingMLEExists(TxtHostRecord hostRecord) {
//                ValidationUtil.validate(hostRecord);
//                String result = ASComponentFactory.getHostTrustBO().checkMatchingMLEExists(hostRecord, 
//                        hostRecord.Location.substring(0, hostRecord.Location.indexOf("|")), hostRecord.Location.substring(hostRecord.Location.indexOf("|")+1));
//                System.out.println("checkMatchingMLEExists RESULT:" + result);
//                return result;
//        }
        
}
