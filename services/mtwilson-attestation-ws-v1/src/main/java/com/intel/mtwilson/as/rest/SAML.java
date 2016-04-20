package com.intel.mtwilson.as.rest;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.security.annotations.*;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import com.intel.mountwilson.as.common.ValidationException;
import com.intel.mtwilson.launcher.ws.ext.V1;
import java.io.IOException;
import java.util.Arrays;
//import javax.ejb.Stateless;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;

/**
 * REST Web Service *
 */
@V1
//@Stateless
@Path("/AttestationService/resources/saml")
public class SAML {

    private HostTrustBO hostTrustBO = ASComponentFactory.getHostTrustBO();
//    private final Marshaller hostManifestReportXML;
//    private SamlGenerator saml;
//    
//    public SAML() throws UnknownHostException, ConfigurationException {
//        InetAddress localhost = InetAddress.getLocalHost();
//        saml = new SamlGenerator(ASConfig.getConfiguration());
//        saml.setIssuer("https://"+localhost.getHostAddress()+":8181/AttestationService");
//    }

    /**
     * Returns the a SAML assertion about the host
     *
     * Sample request: GET
     * http://localhost:8080/AttestationService/resources/hosts/assertions?ID=Some+TXT+Host
     *
     * Sample output:
     * <?xml version="1.0" encoding="UTF-8"?><saml2:Assertion
     * xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
     * IssueInstant="2012-04-12T20:56:32.843Z"
     * Version="2.0"><saml2:Issuer>http://127.0.0.1</saml2:Issuer><saml2:Subject><saml2:NameID
     * Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">test host
     * 1</saml2:NameID><saml2:SubjectConfirmation
     * Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches"><saml2:SubjectConfirmationData
     * Address="10.19.160.229"
     * NotBefore="2012-04-12T20:56:32.914Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute
     * Name="BIOS_Name"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">BIOS
     * ABC</saml2:AttributeValue></saml2:Attribute><saml2:Attribute
     * Name="BIOS_Version"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">1.2.3</saml2:AttributeValue></saml2:Attribute><saml2:Attribute
     * Name="BIOS_OEM"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">BIOS OEM
     * DEF</saml2:AttributeValue></saml2:Attribute><saml2:Attribute
     * Name="VMM_Name"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">VMM
     * XYZ</saml2:AttributeValue></saml2:Attribute><saml2:Attribute
     * Name="VMM_Version"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">7.8.9</saml2:AttributeValue></saml2:Attribute><saml2:Attribute
     * Name="VMM_OSName"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">VMM OS
     * GHI</saml2:AttributeValue></saml2:Attribute><saml2:Attribute
     * Name="VMM_OSVersion"><saml2:AttributeValue
     * xmlns:xs="http://www.w3.org/2001/XMLSchema"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:type="xs:string">4.5.6</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
     *
     * @param ID unique name of the host to query
     * @return a SAML assertion document for the host
     */
    //@RolesAllowed({"Attestation", "Report"})
    //@RequiresRoles({"Attestation","Report"})
    @RequiresPermissions("host_attestations:create,retrieve")            
    @GET
    @Produces({"application/samlassertion+xml"})
    @Path("/assertions/host")
    public String getHostAssertions(
            @QueryParam("hostName") String hostName,
            @QueryParam("force_verify") @DefaultValue("false") Boolean forceVerify) throws IOException {
        if (hostName == null || hostName.isEmpty()) {
            throw new ValidationException("Hostname or IP address is missing");
        }
        String[] hostArray = hostName.split(",");
        if (hostArray.length == 1) {
            // original single-host SAML response
            if (!ValidationUtil.isValidWithRegex(hostName, RegexPatterns.IPADDR_FQDN)) {
                throw new ValidationException("Hostname or IP address is missing or invalid");
            }
            return hostTrustBO.getTrustWithSaml(hostName, forceVerify);
        } else {
            for (String host : hostArray) {
                if (!ValidationUtil.isValidWithRegex(host, RegexPatterns.IPADDR_FQDN)) {
                    throw new ValidationException("Hostname or IP address is missing or invalid");
                }
            }
            return hostTrustBO.getTrustWithSamlForHostnames(Arrays.asList(hostArray)/* , forceVerify*/); 
        }
    }
}
