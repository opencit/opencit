/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.saml.TrustAssertion;
import java.io.File;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostAttestations extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());
    Properties properties = null;

    public HostAttestations(URL url) throws Exception{
        super(url);
    }

    public HostAttestations(Properties properties) throws Exception {
        super(properties);
        this.properties = properties;
    }
       
    /**
     * Forces a complete attestation cycle for the host whose UUID is specified. This does not return back the cached attestation result, instead
     * it creates a new one, caches it in the system and returns back the same to the caller.  
     * The accept content type header should be set to "Accept: application/json".<br>
     * @param obj HostAttestation object with the UUID of the host for which the attestation has to be done. 
     * @return HostAttestation object with the details trust report. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:create
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations
     * Input: {"host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681"} 
     * Output: 
     * {"id":"77b20374-4ae7-4bd5-a1bd-1870d3950ec7","host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681","host_name":"192.168.0.2",
     * "trust_report":{"host_report":
     *  {"variables":{},"pcr_manifest":"0: 5e724d834fec48c62d523d95d08884dcac7f4f98\n1: 3a3f780f11a4b49969fcaa80cd6e3957c33b2275\n
     * 2: cae02cde18cfd267a5b3dbeee532a459c2cc3fa4\n3: 3a3f780f11a4b49969fcaa80cd6e3957c33b2275\n4: 1508b83d473be8eca3385230a3e763ccd42985d5\n
     * 5: 82e9ebba44862bba9cc3dd3bfd0b3be5aa102cbd\n6: 3a3f780f11a4b49969fcaa80cd6e3957c33b2275\n7: 3a3f780f11a4b49969fcaa80cd6e3957c33b2275\n
     * 8: 0000000000000000000000000000000000000000\n9: 0000000000000000000000000000000000000000\n10: 0000000000000000000000000000000000000000\n
     * 11: 0000000000000000000000000000000000000000\n12: 0000000000000000000000000000000000000000\n13: 0000000000000000000000000000000000000000\n
     * 14: 0000000000000000000000000000000000000000\n15: 0000000000000000000000000000000000000000\n16: 0000000000000000000000000000000000000000\n
     * 17: 496c8530d2b4ba6a6f3901455c8c240bbb482d85\n18: f6fd306d2fa33e21c69ca598330b64df1ed0d002\n19: eeddcdd0a2a54ba3791f534884b9536fada66fd1\n
     * 20: 7f824ea48e5d50a4b236152223206b00620bc74b\n21: 0000000000000000000000000000000000000000\n22: 654c7c9cf8fa01d03e61d2c649b16417e9965b71\n
     * 23: 0000000000000000000000000000000000000000\n",
     * "tpm_quote":null,"aik":null,"tag_certificate":null},
     * "policy_name":"Host trust policy for host with AIK 192.168.0.2",
     * "results":[{"rule":{"markers":["VMM"],"expected_pcr":{"value":"7f824ea48e5d50a4b236152223206b00620bc74b","index":"20"}},
     *          "faults":[],"rule_name":"com.intel.mtwilson.policy.rule.PcrMatchesConstant","trusted":true},
     *      {"rule":{"markers":["VMM"],"pcr_module_manifest":{"pcr_index":"19",
     *          "event_log":[{"label":"componentName.ata_pata-ata-pata-pdc2027x-1.0-3vmw.510.0.0.799733","info":
     *          {"EventName":"Vim25Api.HostTpmSoftwareComponentEventDetails","PackageName":"ata-pata-pdc2027x",
     *          "PackageVendor":"VMware","PackageVersion":"1.0-3vmw.510.0.0.799733","HostSpecificModule":"false",
     *          "ComponentName":"componentName.ata_pata-ata-pata-pdc2027x-1.0-3vmw.510.0.0.799733"},......
     "          "value":"67e5d8494a9582bff55bcd91ba3e8ed244fd9755"}]}},
     *          "faults":[],"rule_name":"com.intel.mtwilson.policy.rule.PcrEventLogEqualsExcluding","trusted":true},
     *      {"rule":{"markers":["VMM"],"pcr_index":"19"},"faults":[],"rule_name":"com.intel.mtwilson.policy.rule.PcrEventLogIntegrity","trusted":true},
     *      {"rule":{"markers":["VMM"],"expected_pcr":{"value":"f6fd306d2fa33e21c69ca598330b64df1ed0d002","index":"18"}},
     *          "faults":[],"rule_name":"com.intel.mtwilson.policy.rule.PcrMatchesConstant","trusted":true},
     *      {"rule":{"markers":["BIOS"],"expected_pcr":{"value":"5e724d834fec48c62d523d95d08884dcac7f4f98","index":"0"}},
     *          "faults":[],"rule_name":"com.intel.mtwilson.policy.rule.PcrMatchesConstant","trusted":true},
     *      {"rule":{"markers":["BIOS"],"expected_pcr":{"value":"496c8530d2b4ba6a6f3901455c8c240bbb482d85","index":"17"}},
     *          "faults":[],"rule_name":"com.intel.mtwilson.policy.rule.PcrMatchesConstant","trusted":true}],"trusted":true}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   HostAttestation hostAttestation = new HostAttestation();
     *   hostAttestation.setHostUuid("de07c08a-7fc6-4c07-be08-0ecb2f803681");
     *   HostAttestation createHostAttestation = client.createHostAttestation(hostAttestation);
     * </pre>
     */    
    public HostAttestation createHostAttestation(HostAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
//        Object o = getTarget().path("host-attestations").request(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
//        ObjectMapper mapper = new ObjectMapper();
//        try { log.debug("serializing host-attestations return object: {}", mapper.writeValueAsString(o)); } catch (Exception ex) {log.debug("test?");}
        
        HostAttestation result = getTarget().path("host-attestations").request(MediaType.APPLICATION_JSON).post(Entity.json(obj), HostAttestation.class);
        return result;
    }

    /**
     * Forces a complete attestation cycle for the specified host. 
     * The host can be specified by UUID, AIK Certificate SHA-1, 
     * AIK Public Key SHA-1, or HostName.
     * The accept content type header should be set to "Accept: application/samlassertion+xml".
     * @param obj HostAttestation object with the UUID of the host for which the attestation has to be done. 
     * @return String having the SAML assertion that was just created. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:create
     * @mtwContentTypeReturned SAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations
     * Input: {"host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681"} 
     * Output (SAML): <?xml version="1.0" encoding="UTF-8" ?>
     * <saml2:Assertion ID="HostTrustAssertion" IssueInstant="2014-05-03T01:51:40.924Z" Version="2.0">
     * <saml2:Issuer>https://192.168.0.234:8181</saml2:Issuer>
     *  <Signature>
     *  <SignedInfo>
     *  <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" />
     *  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1" />
     *  <Reference URI="#HostTrustAssertion">
     *  <Transforms>
     *  <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
     *  </Transforms>
     *  <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" />
     *  <DigestValue>44CUa4dBt1cDqoPMX64FmbFh3Yo=</DigestValue>
     *  </Reference>
     *  </SignedInfo>
     *  <SignatureValue>DP94MAPedP+cxyGZKswavB6wkchr7B/jRI4SPO8PfjAQnK6OC/E4Y8PErwNdQxgdbac+kWCNsx+p zynOXajohUfSUoNrP4RZptl1zyTzNX3xbZ6Nm2gMT8sAP4YVHYj3KFHdzr8PNKLG0AmHA97vPNcc FMkZz+SYHNH0t8p4GxeZNyHaBgvccb9h3ciEYBrpR/Lk4jOOmlnZy24nWvXQ5vkGkGLEeP1SeQC+ 6OgtF4Dd3KVrcWxIDHnwiBRJQzKz8FUujElGzLx2WuXrTehz/652XcrR2BZWBydChl0lLOmuzlcL s5AcsmUoS9BTfGOFvc9bwt3uUq6YRgicI73qhA==</SignatureValue>
     *  <KeyInfo>
     *  <X509Data>
     *  <X509Certificate>MIIDYzCCAkugAwIBAgIENgiqGDANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE CBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv bjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTQwNDIzMDQyNDQ0WhcNMjQwNDIwMDQyNDQ0WjBiMQsw CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx EjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA A4IBDwAwggEKAoIBAQCCFAVZDs9WAe2RgF/zY1AwtLLKkI8JyzdRnfTR7FB+QA4SlF8rqg/413Hz ScxBPzNNfOXylyfkHpRrzqHuw2cYiFCE4yE1FpE4bfij7wF40MnyBYiJceHMH1lQpYgoX6Ll+oAE TVWespiOcYia5puTJ2ricOAZxdlklEgu9AVamjJwT//JjfMxhhKfIFZYMTuYnvdibGBvUY9CIDx5 RZWBlix6cMWMCLsuYsXlou5A8P1SEUv7dv75dDqE41dpmiduN0maX61OggO7WACeLbd95bQC+Hxr 5Wc4dsx8z9r9IuZ6+KlpqJ9Zxk12pPPt/s5XzVIS56wT4ooi/VczCeohAgMBAAGjITAfMB0GA1Ud DgQWBBR7omQkuYF8S4MJ+zk97+3QNv7/1zANBgkqhkiG9w0BAQsFAAOCAQEAd1vP55PscV21PmT6 TlNgicokTfJSGk3nNVQFmh8MDN2rnB0JDfhs1iPnaGUGgRNbK6/0EFKEvdX8gswqhehnOi3icJcI O+1c3vNqol5+UfJ0y9Fh9o5tZRzBe1qzxcMoq4/BozKNGUhHpaSzYJpCzKgyupHuOKqOki9xh6yb dpUqD153+Ze2FSIM1uR+URlL2zNHKozEBPOqLVooAEZLE1+RDxqmK1o/e3xEILj5L3eNGRoF1dlM PljwL6BXausdL6ZzRQHHTT5eC06ReQAfeBCHQJVPDsn3e+p8R35oyHLoglH7fFY7gHwasjZh44/O TD5a0rl/2SuHapQ6LsICYw==</X509Certificate>
     *  </X509Data>
     *  </KeyInfo>
     *  </Signature>
     *  <saml2:Subject>
     *  <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">192.168.0.2</saml2:NameID>
     *  <saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     *  <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">AttestationService-0.5.4</saml2:NameID>
     *  <saml2:SubjectConfirmationData Address="127.0.0.1" NotBefore="2014-05-03T01:51:40.924Z" NotOnOrAfter="2014-05-03T02:51:40.924Z" />
     *  </saml2:SubjectConfirmation>
     *  </saml2:Subject>
     * <saml2:AttributeStatement>
     * <saml2:Attribute Name="Host_Name"><saml2:AttributeValue xsi:type="xs:string">192.168.0.2</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Host_Address"><saml2:AttributeValue xsi:type="xs:string">192.168.0.2</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Trusted"><saml2:AttributeValue xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Trusted_BIOS"><saml2:AttributeValue xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="BIOS_Name"><saml2:AttributeValue xsi:type="xs:string">Intel_Corporation_001</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="BIOS_Version"><saml2:AttributeValue xsi:type="xs:string">01.00.0060</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="BIOS_OEM"><saml2:AttributeValue xsi:type="xs:string">Intel Corporation</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Trusted_VMM"><saml2:AttributeValue xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_Name"><saml2:AttributeValue xsi:type="xs:string">Intel_Thurley_VMware_ESXi</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_Version"><saml2:AttributeValue xsi:type="xs:string">5.1.0-799733</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_OSName"><saml2:AttributeValue xsi:type="xs:string">VMware_ESXi</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_OSVersion"><saml2:AttributeValue xsi:type="xs:string">5.1.0</saml2:AttributeValue></saml2:Attribute>
     * </saml2:AttributeStatement>
     * </saml2:Assertion>
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   HostAttestation hostAttestation = new HostAttestation();
     *   hostAttestation.setHostUuid("de07c08a-7fc6-4c07-be08-0ecb2f803681");
     *   String hostSaml = client.createHostAttestationSaml(hostAttestation);
     * </pre>
     */    
    public String createHostAttestationSaml(HostAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        String samlAssertion = getTarget().path("host-attestations").request(CryptoMediaType.APPLICATION_SAML).post(Entity.json(obj), String.class);
        return samlAssertion;
    }
    
    /**
     * Deletes the cached host attestation entry from the system with the specified UUID.
     * @param uuid - UUID of the cached host attestation to be deleted from the system. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations/32923691-9847-4493-86ee-3036a4f24940
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   client.deleteHostAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </pre>
     */
    public void deleteHostAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("host-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

    /**
     * This functionality is not supported.
     */
    public HostAttestation editHostAttestation(HostAttestation obj) {
        throw new UnsupportedOperationException("Not supported yet.");    
    }

    /**
     * Retrieves the basic trust attestation report with the specifiied UUID. Note that this is not the UUID of the host
     * for which the attestation report needs to be retrieved. It is the UUID of the attestation that was created for the
     * host and cached in the system. This always retrieves the latest cached attestation result from the system.
     * @param uuid - UUID of the cached attestation. 
     * @return HostAttestation object with the basic attestation report.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations/32923691-9847-4493-86ee-3036a4f24940
     * Output: {"id":"32923691-9847-4493-86ee-3036a4f24940",
     * "host_uuid":"de07c08a-7fc6-4c07-be08-0ecb2f803681",
     * "host_name":"de07c08a-7fc6-4c07-be08-0ecb2f803681",
     * "host_trust_response":{"hostname":"de07c08a-7fc6-4c07-be08-0ecb2f803681","trust":{"bios":true,"vmm":true,"location":false,"asset_tag":false}}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   client.retrieveHostAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </pre>
    */    
    public HostAttestation retrieveHostAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        HostAttestation obj = getTarget().path("host-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(HostAttestation.class);
        return obj;
    }
    
    /**
     * Searches for the attestation results for the host with the specified criteria, and returns the attestation in the format specified by the accept header.
     * Basic attestation report would be returned back to the caller for the hosts matching the search criteria. If the user specifies the accept content type 
     * as "application/samlassertion+xml", then only the latest valid cached SAML assertion matching the search criteria would be returned back to the caller
     * (a single attestation).<br>
     * <br>
     * Other valid accept headers include:<br>
     * "application/xml"<br>
     * "application/json"<br>
     * <br>
     * If no accept header is specified, the returned data will default to the JSON type. Bot the XML and JSON types will return all attestations that meet the
     * specified criteria.<br>
     * <br>
     * Currently the following ISO 8601 date formats are supported for date parameters:<br>
     *     -- date. Ex: nameEqualTo=192.168.0.2&fromDate=2015-05-01&toDate=2015-06-01<br>
     *     -- date+time. Ex: nameEqualTo=192.168.0.2&fromDate=2015-04-05T00:00Z&toDate=2015-06-05T00:00Z<br>
     *     -- date+time+zone. Ex: nameEqualTo=192.168.0.2&fromDate=2015-04-05T12:30-02:00&toDate=2015-06-05T12:30-02:00<br>
     * <br>
     * Note that when the fromDate and toDate options are specified, the output includes the attestations from the fromDate upto the toDate but not including the
     * attestations from the toDate.
     * 
     * @param id Host attestation ID
     * @param host_id Host UUID
     * @param nameEqualTo Host Name
     * @param aik Host AIK SHA1
     * @param aik_public_key_sha1 Host AIK Public Key SHA1
     * @param numberOfDays Number of days for which we want the attestations from current date
     * @param fromDate Date from which we want the attestations
     * @param toDate Date till which we want the attestations
     * @param limit number of records we want to return back (default is set to 10; set this higher when retrieving all attestations over a certain date range)
     * @param filter setting this parameter to "false" returns all attestations with no criteria applied, use with caution (default is set to "true"; returns no records; parameters are required)
     * 
     * @return HostAttestationCollection object with a list of attestations for the hosts that match the filter criteria. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:search
     * @mtwContentTypeReturned JSON/XML/YAML/SAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations?nameEqualTo=192.168.0.2&limit=2
     * Output: {"host_attestations":[{"id":"39cd1143-4f74-4767-8d82-9cb93d202115","host_uuid":"7ad3f23a-4a60-4562-9d0a-777dd2cd788e",
     * "host_name":"192.168.0.2","host_trust_response":{"hostname":"192.168.0.2","trust":{"bios":true,"vmm":true,"location":false,"asset_tag":false}}},
     * {"id":"351408fd-53d4-4b65-8488-59e9867d091f","host_uuid":"7ad3f23a-4a60-4562-9d0a-777dd2cd788e","host_name":"192.168.0.2",
     * "host_trust_response":{"hostname":"192.168.0.2","trust":{"bios":true,"vmm":true,"location":false,"asset_tag":false}}}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   HostAttestationFilterCriteria criteria = new HostAttestationFilterCriteria();
     *   criteria.nameEqualTo = "192.168.0.2";
     *   criteria.limit = 2;
     *   HostAttestationCollection objCollection = client.searchHostAttestations(criteria);
     * </pre>
     */    
    public HostAttestationCollection searchHostAttestations(HostAttestationFilterCriteria criteria) {        
        log.debug("target: {}", getTarget().getUri().toString());
        HostAttestationCollection objCollection = getTargetPathWithQueryParams("host-attestations", criteria).request(MediaType.APPLICATION_JSON).get(HostAttestationCollection.class);
        return objCollection;
    }

    /**
     * Searches for the cached SAML attestation results for the host with the specified criteria. The latest cached SAML assertion would be returned
     * back if the cached value is still valid. The accept content type header should be set to "Accept: application/samlassertion+xml".
     * @param criteria HostAttestationFilterCriteria object that specifies the search criteria.
     * The possible search options include host attestation id, host UUID, host AIK and host name. 
     * @return String object having the SAML assertion contents.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions host_attestations:search
     * @mtwContentTypeReturned SAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations?nameEqualTo=192.168.0.2
     * Output (SAML): <?xml version="1.0" encoding="UTF-8" ?>
     * <saml2:Assertion ID="HostTrustAssertion" IssueInstant="2014-05-03T01:51:40.924Z" Version="2.0">
     * <saml2:Issuer>https://192.168.0.234:8181</saml2:Issuer>
     *  <Signature>
     *  <SignedInfo>
     *  <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments" />
     *  <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1" />
     *  <Reference URI="#HostTrustAssertion">
     *  <Transforms>
     *  <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
     *  </Transforms>
     *  <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" />
     *  <DigestValue>44CUa4dBt1cDqoPMX64FmbFh3Yo=</DigestValue>
     *  </Reference>
     *  </SignedInfo>
     *  <SignatureValue>DP94MAPedP+cxyGZKswavB6wkchr7B/jRI4SPO8PfjAQnK6OC/E4Y8PErwNdQxgdbac+kWCNsx+p zynOXajohUfSUoNrP4RZptl1zyTzNX3xbZ6Nm2gMT8sAP4YVHYj3KFHdzr8PNKLG0AmHA97vPNcc FMkZz+SYHNH0t8p4GxeZNyHaBgvccb9h3ciEYBrpR/Lk4jOOmlnZy24nWvXQ5vkGkGLEeP1SeQC+ 6OgtF4Dd3KVrcWxIDHnwiBRJQzKz8FUujElGzLx2WuXrTehz/652XcrR2BZWBydChl0lLOmuzlcL s5AcsmUoS9BTfGOFvc9bwt3uUq6YRgicI73qhA==</SignatureValue>
     *  <KeyInfo>
     *  <X509Data>
     *  <X509Certificate>MIIDYzCCAkugAwIBAgIENgiqGDANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE CBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv bjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTQwNDIzMDQyNDQ0WhcNMjQwNDIwMDQyNDQ0WjBiMQsw CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx EjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA A4IBDwAwggEKAoIBAQCCFAVZDs9WAe2RgF/zY1AwtLLKkI8JyzdRnfTR7FB+QA4SlF8rqg/413Hz ScxBPzNNfOXylyfkHpRrzqHuw2cYiFCE4yE1FpE4bfij7wF40MnyBYiJceHMH1lQpYgoX6Ll+oAE TVWespiOcYia5puTJ2ricOAZxdlklEgu9AVamjJwT//JjfMxhhKfIFZYMTuYnvdibGBvUY9CIDx5 RZWBlix6cMWMCLsuYsXlou5A8P1SEUv7dv75dDqE41dpmiduN0maX61OggO7WACeLbd95bQC+Hxr 5Wc4dsx8z9r9IuZ6+KlpqJ9Zxk12pPPt/s5XzVIS56wT4ooi/VczCeohAgMBAAGjITAfMB0GA1Ud DgQWBBR7omQkuYF8S4MJ+zk97+3QNv7/1zANBgkqhkiG9w0BAQsFAAOCAQEAd1vP55PscV21PmT6 TlNgicokTfJSGk3nNVQFmh8MDN2rnB0JDfhs1iPnaGUGgRNbK6/0EFKEvdX8gswqhehnOi3icJcI O+1c3vNqol5+UfJ0y9Fh9o5tZRzBe1qzxcMoq4/BozKNGUhHpaSzYJpCzKgyupHuOKqOki9xh6yb dpUqD153+Ze2FSIM1uR+URlL2zNHKozEBPOqLVooAEZLE1+RDxqmK1o/e3xEILj5L3eNGRoF1dlM PljwL6BXausdL6ZzRQHHTT5eC06ReQAfeBCHQJVPDsn3e+p8R35oyHLoglH7fFY7gHwasjZh44/O TD5a0rl/2SuHapQ6LsICYw==</X509Certificate>
     *  </X509Data>
     *  </KeyInfo>
     *  </Signature>
     *  <saml2:Subject>
     *  <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">192.168.0.2</saml2:NameID>
     *  <saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     *  <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">AttestationService-0.5.4</saml2:NameID>
     *  <saml2:SubjectConfirmationData Address="127.0.0.1" NotBefore="2014-05-03T01:51:40.924Z" NotOnOrAfter="2014-05-03T02:51:40.924Z" />
     *  </saml2:SubjectConfirmation>
     *  </saml2:Subject>
     * <saml2:AttributeStatement>
     * <saml2:Attribute Name="Host_Name"><saml2:AttributeValue xsi:type="xs:string">192.168.0.2</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Host_Address"><saml2:AttributeValue xsi:type="xs:string">192.168.0.2</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Trusted"><saml2:AttributeValue xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Trusted_BIOS"><saml2:AttributeValue xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="BIOS_Name"><saml2:AttributeValue xsi:type="xs:string">Intel_Corporation_001</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="BIOS_Version"><saml2:AttributeValue xsi:type="xs:string">01.00.0060</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="BIOS_OEM"><saml2:AttributeValue xsi:type="xs:string">Intel Corporation</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="Trusted_VMM"><saml2:AttributeValue xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_Name"><saml2:AttributeValue xsi:type="xs:string">Intel_Thurley_VMware_ESXi</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_Version"><saml2:AttributeValue xsi:type="xs:string">5.1.0-799733</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_OSName"><saml2:AttributeValue xsi:type="xs:string">VMware_ESXi</saml2:AttributeValue></saml2:Attribute>
     * <saml2:Attribute Name="VMM_OSVersion"><saml2:AttributeValue xsi:type="xs:string">5.1.0</saml2:AttributeValue></saml2:Attribute>
     * </saml2:AttributeStatement>
     * </saml2:Assertion>
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   HostAttestationFilterCriteria criteria = new HostAttestationFilterCriteria();
     *   criteria.nameEqualTo = "192.168.0.2";
     *   String hostSaml = client.searchHostAttestationsSaml(criteria);
     * </pre>
     */    
    public String searchHostAttestationsSaml(HostAttestationFilterCriteria criteria) {        
        log.debug("target: {}", getTarget().getUri().toString());
        String hostSaml = getTargetPathWithQueryParams("host-attestations", criteria).request(CryptoMediaType.APPLICATION_SAML).get(String.class);
        return hostSaml;
    }
    
    /**
     * Verifies the signature of the retrieved SAML assertion using the SAML certificate stored in the user keystore created during user registration.
     * This functionality is available for the Api library users only.
     * @param saml SAML assertion.
     * @return TrustAssertion object having the status of verification.
     * @since Mt.Wilson 2.0
     * @mtwSampleApiCall
     * <pre>
     *   HostAttestations client = new HostAttestations(My.configuration().getClientProperties());
     *   HostAttestation hostAttestation = new HostAttestation();
     *   hostAttestation.setHostUuid("de07c08a-7fc6-4c07-be08-0ecb2f803681");
     *   String hostSaml = client.createHostAttestationSaml(hostAttestation);
     *   TrustAssertion verifyTrustAssertion = attestationClient.verifyTrustAssertion(createHostAttestationSaml);
     * </pre>
     */        
    public TrustAssertion verifyTrustAssertion(String saml) throws KeyManagementException, ApiException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException {
        if (properties == null || properties.getProperty("mtwilson.api.keystore") == null || properties.getProperty("mtwilson.api.keystore.password") == null) {
            return null;
        }
        String mtwilsonApiKeystore = properties.getProperty("mtwilson.api.keystore");
        String mtwilsonApiKeystorePassword = properties.getProperty("mtwilson.api.keystore.password");
        
        if (mtwilsonApiKeystore == null || mtwilsonApiKeystore.isEmpty() || mtwilsonApiKeystorePassword == null || mtwilsonApiKeystorePassword.isEmpty()) {
            return null;
        }
        
        SimpleKeystore keystore = new SimpleKeystore(new File(mtwilsonApiKeystore), mtwilsonApiKeystorePassword);
        X509Certificate[] trustedSamlCertificates;
        try {
            trustedSamlCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        TrustAssertion trustAssertion = new TrustAssertion(trustedSamlCertificates, saml);
        return trustAssertion;
    }
}
