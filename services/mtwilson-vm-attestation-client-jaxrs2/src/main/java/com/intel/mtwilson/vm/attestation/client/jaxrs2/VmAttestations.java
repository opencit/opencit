/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.vm.attestation.client.jaxrs2;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationFilterCriteria;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class VmAttestations extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());
    Properties properties = null;

    public VmAttestations(URL url) throws Exception{
        super(url);
    }

    public VmAttestations(Properties properties) throws Exception {
        super(properties);
        this.properties = properties;
    }
       
    /**
     * Forces a complete attestation cycle for the specified VM running on the specified host and returns back the detailed attestation report.
     * Optionally the user can also request for the host attestation report also to be included in the final report.
     * The accept content type header should be set to "Accept: application/json" or "Accept: application/xml".<br>
     * @param obj VMAttestation object with the UUID of the host for which the attestation has to be done. 
     * @return VMAttestation object with the detailed trust report. 
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions vm_attestations:create
     * @mtwContentTypeReturned JSON or XML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8181/mtwilson/v2/host-attestations
     * 
     * Input: {"host_name":"194.168.1.2","vm_instance_id":"61489ac0-51a9-4a33-a1ec-e6f67fd49bd4","include_host_report":false} 
     * 
     * Output: 
     * {"id":"d6555b08-78ce-4216-8f88-078755aea31b","host_name":"mh-kvm-71","vm_instance_id":"8a227d37-420e-48f7-ac5a-ce30400aa1d5","trust_status":true,
     * "vm_saml":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Assertion xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" ID=\"VMTrustAssertion\" IssueInstant=\"2016-02-15T08:52:04.315Z\" Version=\"2.0\"><saml2:Issuer>https://10.1.71.88:8443</saml2:Issuer><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"#VMTrustAssertion\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>K2VZJghqM1Fe49Hp8i6z4JgboRY=</DigestValue></Reference></SignedInfo><SignatureValue>Y6BWOT+c6Bxj8fGfWMTulPcjYeWoHE0OkOTdf8f4K1j711ammOQobxtZhKU+ZYPV0Tp3SArIOxEr\nPEjplBhdPMf4PX6wXMacIDM0maxIGZSeRgKZquJs0U4DY7A9OffkW6beJkwEFZTcQIEeZEcre398\nfjCxsCXa8kURBRiSk12kg/fCTBxtWPXwLV7+YTrkniDp/23sQ9K6hVFeiOfyJug1gysXLF+ztmyx\n9Qr8P25k+QwISaHK/yKaBDACH9puW87tkGm1UCUYDr2uw6AZNLPiLd+3epa63F33C8MPm8C5kEkQ\njAiSP1TEbP57FEfnCIjoqKv5Z+eR5GbQvLIhHg==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry\ncGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg\n59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9\nVW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R\nZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud\nDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b\nfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf\nbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7\n1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB\n5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk\niRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:NameID><saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:sender-vouches\"><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">Cloud Integrity Technology</saml2:NameID><saml2:SubjectConfirmationData Address=\"10.1.71.88\" NotBefore=\"2016-02-15T08:52:04.315Z\" NotOnOrAfter=\"2016-02-15T09:52:04.315Z\"/></saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name=\"Host_Name\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">mh-kvm-71</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"AIK_Certificate\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">-----BEGIN CERTIFICATE-----&#13;\nMIICvTCCAaWgAwIBAgIGAVLV46YjMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u&#13;\nLXBjYS1haWswHhcNMTYwMjEyMTQzMDEyWhcNMjYwMjExMTQzMDEyWjAAMIIBIjANBgkqhkiG9w0B&#13;\nAQEFAAOCAQ8AMIIBCgKCAQEAwa43TlH7cBdJ6m5TBU7MDsLiFh3D6ncmOckqgU7wwO3nGOR8OIcz&#13;\nFATHNPRudr3rRKk2pDNMw3TyFEhMLn9QSgEyUIW07F8uBvpxjBgR2FqNLI+qwqxZUe3OLYnEuiwR&#13;\nW1ItCid6Z+CC0bnUIMSPu0ovYL/fcD92xAHYtD/B4zR2hUcXkNfaH89Az3as5YgQYn9GL8w/AiuU&#13;\njHqwAeavyF0+qfpaRY2UvJakVv6Gl/QjeN/mPL/DvutnrN3il6qNJikJYnxF8BhTCKPLJh7UsrCK&#13;\nyT8Rllm1Map/WCQvSWeDBX18aHRL5opbGRGQTfF2QbaNWVKtYvsqOe7B8HrqRQIDAQABoyIwIDAe&#13;\nBgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQAf9wEKsfnL&#13;\nP8hhFhmj/EWSWmeyoPcXPh6tD/XcYhVucNMf+ki3ZlUPJyaieOcSvfv0NWpYc7GngZLtmCyggfjU&#13;\nwvpKBqieqzocM/o4LpTk84h8KAY2V5qnlipsHheBQ3mwOMSfDXAjUaEUwesuwLhjHeno/rELxLMx&#13;\nVugf/hbVLJYZ9zfrnBGJvtuLAGfGpz8zPUiHhCVB2MR81WzT5N3ULvbWf7f1iANXG02bS7fRTHze&#13;\nrVsYDC+36wDP4wN8brLR/o/9UkfUPrw8vhGQu8dyShyK5j4zllflCOTva2OX1dJThLn4MuFFPBaf&#13;\nLB8TLOM0HD5iMO96Iojjqjx5P89Z&#13;\n-----END CERTIFICATE-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"AIK_SHA1\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">76a2851ce74ec07926aa70f6b7ed98ef6a59a168</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Instance_Id\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Trust_Status\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Trust_Policy\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">MeasureAndEnforce</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>","vm_trust_report":{"vm_report":{"vm_trust_policy":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:TrustPolicy xmlns:ns2=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns3=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><ns2:Director><ns2:CustomerId>testId</ns2:CustomerId></ns2:Director><ns2:Image><ns2:ImageId>9a156c2c-be6d-4f0c-b2d0-e848b5f6a3a7</ns2:ImageId><ns2:ImageHash>4bd5297bdc972066b154dd6cf497bf5427b3866eda00f67e0bce3fb9ba705602</ns2:ImageHash></ns2:Image><ns2:LaunchControlPolicy>MeasureAndEnforce</ns2:LaunchControlPolicy><ns2:Encryption><ns2:Key URL=\"uri\">http://10.1.71.73/v1/keys/f15c2ad1-00ef-4e8d-ae70-552be2f38782/transfer</ns2:Key><ns2:Checksum DigestAlg=\"md5\">fd84897d80c81f4699e8fb00231694dc</ns2:Checksum></ns2:Encryption><ns2:Whitelist DigestAlg=\"sha256\"><ns2:File Path=\"/bin/chown\">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</ns2:File></ns2:Whitelist><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>3TnKv6iVrVNzk6JVyvLw/UN7Zs4=</DigestValue></Reference></SignedInfo><SignatureValue>ZNYW08tkZJylGkjN+hR6N1LgWREFKIIWcjvYuA3UI/Mj1f3JTBGBYUWfUTsVNialrwiw9m8trG2B\n1LS6JOZ4o3O+/VoTFMIf5sBy0/lJZCpUFgC7PT9lbgycheOh+5t6S/l+yMEt7ue+3ov3GoXi62n0\nCSznkJi0pSSpv6YyWXt+AHiSKJkUlzuSETkJihiL8sfViqPNeX9CZAtVkGAviJrWEfkeZFJ07wSv\nPBNxTQnaIJD4U7yyurNV3doS3PVjAmEbGzquTG/F6Hyu4szHzEgurVM237DmM5s16F6XPPf1sQey\n2FQTVjw3y+zycqE6N0trcwcOom4RW+3W/33piA==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry\ncGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg\n59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9\nVW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R\nZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud\nDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b\nfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf\nbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7\n1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB\n5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk\niRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature></ns2:TrustPolicy>",
     * "vm_measurements":"<?xml version=\"1.0\"?>\n<Measurements xmlns=\"mtwilson:trustdirector:measurements:1.1\" DigestAlg=\"sha256\">\n<File Path=\"/bin/chown\">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</File>\n</Measurements>"},
     * "results":[{"rule":{"rule_name":"com.intel.mtwilson.policy.rule.VmMeasurementLogEquals","markers":["VM"]},"rule_name":"com.intel.mtwilson.policy.rule.VmMeasurementLogEquals","trusted":true}],"trusted":true}}
     * </xmp></pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation attestation = new VMAttestation();
     *   attestation.setHostName("194.168.1.2");
     *   attestation.setVmInstanceId("14e03157-0935-442f-b4d6-1622154468e4");
     *   attestation.setIncludeHostReport(true);
     *   VMAttestation createVMAttestation = client.createVMAttestation(attestation);
     * </xmp></pre></div>
     */    
    public VMAttestation createVMAttestation(VMAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        VMAttestation result = getTarget().path("vm-attestations").request(MediaType.APPLICATION_JSON).post(Entity.json(obj), VMAttestation.class);
        return result;
    }

    /**
     * Forces a complete attestation cycle for the specified VM running on the specified host and returns back the SAML assertion.
     * 
     * The accept content type header should be set to "Accept: application/samlassertion+xml".
     * @param obj HostAttestation object with the UUID of the host for which the attestation has to be done. 
     * @return String having the SAML assertion that was just created. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions vm_attestations:create
     * @mtwContentTypeReturned SAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8443/mtwilson/v2/vm-attestations
     * 
     * Input: {"host_name":"194.168.1.2","vm_instance_id":"8a227d37-420e-48f7-ac5a-ce30400aa1d5"} 
     * 
     * Output: 
     * <?xml version="1.0" encoding="UTF-8"?><saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="VMTrustAssertion" IssueInstant="2016-02-15T08:47:14.259Z" Version="2.0"><saml2:Issuer>https://server.com:8443</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#VMTrustAssertion"><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>5vj22/v5XB8nb0or/KHMvO7Dj9c=</DigestValue></Reference></SignedInfo><SignatureValue>b1SKbLowrzJZIQXpMAkkpP2fk8kyY5iIC2QaJz/cOdnCcVsZTX6WRA4hwlkzs1tHV/YSp0L2QnVL86tviRQ5O9hkG4p3XgeuKyN1k6CY918IY5PSEHHKykipVMTQ4XD4lQFDKfhTXApvS8yRyAwy+meSSH/43FcKs+PjNyuDmnb6l3VCsG/CNLMBpNYVwozcdxVIfjRogPll5MysGXIR/piq734WTN6h7Zj05FG55T52g82I9O+QOrHX0Y3EGEf0/ZlDxS3n0jPqm+3mLvm5/rm/y7ko7+aY/AqlFh3bdaxubRVznn3HV5etek6KW9sQPQ0i/Aejkvl5zsV73RWIVA==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRrycGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9VW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0RZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1UdDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6bfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBfbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr71DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZkiRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches"><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">Cloud Integrity Technology</saml2:NameID><saml2:SubjectConfirmationData Address="server.com" NotBefore="2016-02-15T08:47:14.259Z" NotOnOrAfter="2016-02-15T09:47:14.259Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name="Host_Name"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">mh-kvm-71</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_Certificate"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">-----BEGIN CERTIFICATE-----&#13;MIICvTCCAaWgAwIBAgIGAVLV46YjMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u&#13;LXBjYS1haWswHhcNMTYwMjEyMTQzMDEyWhcNMjYwMjExMTQzMDEyWjAAMIIBIjANBgkqhkiG9w0B&#13;AQEFAAOCAQ8AMIIBCgKCAQEAwa43TlH7cBdJ6m5TBU7MDsLiFh3D6ncmOckqgU7wwO3nGOR8OIcz&#13;FATHNPRudr3rRKk2pDNMw3TyFEhMLn9QSgEyUIW07F8uBvpxjBgR2FqNLI+qwqxZUe3OLYnEuiwR&#13;W1ItCid6Z+CC0bnUIMSPu0ovYL/fcD92xAHYtD/B4zR2hUcXkNfaH89Az3as5YgQYn9GL8w/AiuU&#13;jHqwAeavyF0+qfpaRY2UvJakVv6Gl/QjeN/mPL/DvutnrN3il6qNJikJYnxF8BhTCKPLJh7UsrCK&#13;yT8Rllm1Map/WCQvSWeDBX18aHRL5opbGRGQTfF2QbaNWVKtYvsqOe7B8HrqRQIDAQABoyIwIDAe&#13;BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQAf9wEKsfnL&#13;P8hhFhmj/EWSWmeyoPcXPh6tD/XcYhVucNMf+ki3ZlUPJyaieOcSvfv0NWpYc7GngZLtmCyggfjU&#13;wvpKBqieqzocM/o4LpTk84h8KAY2V5qnlipsHheBQ3mwOMSfDXAjUaEUwesuwLhjHeno/rELxLMx&#13;Vugf/hbVLJYZ9zfrnBGJvtuLAGfGpz8zPUiHhCVB2MR81WzT5N3ULvbWf7f1iANXG02bS7fRTHze&#13;rVsYDC+36wDP4wN8brLR/o/9UkfUPrw8vhGQu8dyShyK5j4zllflCOTva2OX1dJThLn4MuFFPBaf&#13;LB8TLOM0HD5iMO96Iojjqjx5P89Z&#13;-----END CERTIFICATE-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_SHA1"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">76a2851ce74ec07926aa70f6b7ed98ef6a59a168</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VM_Instance_Id"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VM_Trust_Status"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VM_Trust_Policy"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">MeasureAndEnforce</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
     * </xmp></pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation attestation = new VMAttestation();
     *   attestation.setHostName("194.168.1.2");
     *   attestation.setVmInstanceId("14e03157-0935-442f-b4d6-1622154468e4");
     *   String hostSaml = client.createVMAttestationSaml(attestation);
     * </xmp></pre></div>
     */    
    public String createVMAttestationSaml(VMAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        String samlAssertion = getTarget().path("vm-attestations").request(CryptoMediaType.APPLICATION_SAML).post(Entity.json(obj), String.class);
        return samlAssertion;
    }
    
    /**
     * Deletes the VM attestation report with the specifiied ID cached in the system. 
     * @param uuid - UUID of the cached VM attestation to be deleted from the system. 
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions vm_attestations:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8443/mtwilson/v2/vm-attestations/32923691-9847-4493-86ee-3036a4f24940
     * </xmp></pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   client.deleteVMAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </xmp></pre></div>
     */
    public void deleteVMAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("vm-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

    /**
     * Deletes the attestation results for the Virtual Machine (VM) matching the specified criteria. 
     * @param criteria VMAttestationFilterCriteria object that specifies the search criteria.
     * The possible search options include one of the following
     * - VM attestation ID - A specific VM attestation report would be retrieved 
     * - VM instance ID - UUID of the VM instance - All reports for the specified VM instance would be retrieved.
     * - Host Name or IP address - Retrieves reports for all the VMs that were/are running on the specified host.
     * 
     * For both VM instance and Host name search criteria, user can additionally specify the below criteria.
     * - numberOfDays - Specifies the number of days back from the current date for which the attestations are needed. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * - fromDate & toDate - Specifies the date range for which the attestations are needed. Currently the following ISO 8601 date formats are supported
     *     -- date. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&fromDate=2015-05-01&toDate=2015-06-01
     *     -- date+time. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T00:00Z&toDate=2015-06-05T00:00Z
     *     -- date+time+zone. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T12:30-02:00&toDate=2015-06-05T12:30-02:00
     * 
     * Note that when the fromDate and toDate options are specified, the output includes the attestations from the fromDate upto the toDate but not including the
     * attestations from the toDate.
     * 
     * By default the last 10 attestation results would be returned back. The user can change this by additionally specifying the limit criteria (limit=5).
     * 
     * @since CIT 3.0
     * @mtwRequiresPermissions vm_attestations:search,retrieve,delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&limit=2
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * https://server.com:8443/mtwilson/v2/vm-attestations?hostNameEqualTo=192.168.0.2&fromDate=2015-05-01&toDate=2015-06-01
     * </xmp></pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestationFilterCriteria criteria = new VMAttestationFilterCriteria();
     *   criteria.vmInstanceIdEqualTo = "14e03157-0935-442f-b4d6-1622154468e4";
     *   criteria.numberOfDays = 2;
     *   client.deleteVMAttestation(criteria);
     * </xmp></pre></div>
     */    
    public void deleteVMAttestation(VMAttestationFilterCriteria criteria) {        
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("vm-attestations", criteria).request(MediaType.APPLICATION_JSON).delete();        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete VM attestation failed");
        }        
    }
    
    /**
     * This functionality is not supported.
     */
    public HostAttestation editHostAttestation(HostAttestation obj) {
        throw new UnsupportedOperationException("Not supported yet.");    
    }

    /**
     * Retrieves the VM attestation report with the specifiied ID (UUID). Note that this is the UUID of the attestation that was created for the
     * VM and cached in the system. 
     * @param uuid - UUID of the cached attestation. 
     * @return VMAttestation object with the attestation report.
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions vm_attestations:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8181/mtwilson/v2/vm-attestations/51fd9631-d8f0-4927-8b2a-f68023b38ce5
     * 
     * Output: 
     * {"id":"51fd9631-d8f0-4927-8b2a-f68023b38ce5","host_name":"mh-kvm-71","vm_instance_id":"8a227d37-420e-48f7-ac5a-ce30400aa1d5","trust_status":true,
     * "vm_saml":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Assertion xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" ID=\"VMTrustAssertion\" IssueInstant=\"2016-02-12T15:58:13.889Z\" Version=\"2.0\"><saml2:Issuer>https://server.com:8443</saml2:Issuer><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"#VMTrustAssertion\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>gVsEmbT8vXu9xXBoZ97qRWcm/Lg=</DigestValue></Reference></SignedInfo><SignatureValue>Cv2rEcGiZjYJPwoiiwlrydTbAbo1ZqAPGSdHVt9Iu8I5QsYF9W1bky5bvJtOMT+Ose4LHkWlURvu\nzsm+qgFYuE5ENcgJt3fqH7ZB7pb400g0ncAr5bRY8x23+TF3Ep5XKT0OnEOtKxauiV5uXTxYfZ+Y\nxzodoMbD+JlhVwOjelvarJ9EplKXf/zgV1tALIBo+msq/QFpOPHcdyK2U1lOZt52H1nw7dCITtZd\n5Z9O1fvP8mkwZOYBClO7QbM3lI+CBuNth8FmrbDm/mQVaBiDR6QGAUqvoOijYjUrqxkNtOkYv+NI\np9A1pVcI7zBgVg7Fq7sMhcwJRcUhV/t03RAd2g==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry\ncGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg\n59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9\nVW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R\nZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud\nDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b\nfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf\nbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7\n1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB\n5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk\niRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:NameID><saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:sender-vouches\"><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">Cloud Integrity Technology</saml2:NameID><saml2:SubjectConfirmationData Address=\"server.com\" NotBefore=\"2016-02-12T15:58:13.889Z\" NotOnOrAfter=\"2016-02-12T16:58:13.889Z\"/></saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name=\"Host_Name\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">mh-kvm-71</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"AIK_Certificate\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">-----BEGIN CERTIFICATE-----&#13;\nMIICvTCCAaWgAwIBAgIGAVLV46YjMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u&#13;\nLXBjYS1haWswHhcNMTYwMjEyMTQzMDEyWhcNMjYwMjExMTQzMDEyWjAAMIIBIjANBgkqhkiG9w0B&#13;\nAQEFAAOCAQ8AMIIBCgKCAQEAwa43TlH7cBdJ6m5TBU7MDsLiFh3D6ncmOckqgU7wwO3nGOR8OIcz&#13;\nFATHNPRudr3rRKk2pDNMw3TyFEhMLn9QSgEyUIW07F8uBvpxjBgR2FqNLI+qwqxZUe3OLYnEuiwR&#13;\nW1ItCid6Z+CC0bnUIMSPu0ovYL/fcD92xAHYtD/B4zR2hUcXkNfaH89Az3as5YgQYn9GL8w/AiuU&#13;\njHqwAeavyF0+qfpaRY2UvJakVv6Gl/QjeN/mPL/DvutnrN3il6qNJikJYnxF8BhTCKPLJh7UsrCK&#13;\nyT8Rllm1Map/WCQvSWeDBX18aHRL5opbGRGQTfF2QbaNWVKtYvsqOe7B8HrqRQIDAQABoyIwIDAe&#13;\nBgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQAf9wEKsfnL&#13;\nP8hhFhmj/EWSWmeyoPcXPh6tD/XcYhVucNMf+ki3ZlUPJyaieOcSvfv0NWpYc7GngZLtmCyggfjU&#13;\nwvpKBqieqzocM/o4LpTk84h8KAY2V5qnlipsHheBQ3mwOMSfDXAjUaEUwesuwLhjHeno/rELxLMx&#13;\nVugf/hbVLJYZ9zfrnBGJvtuLAGfGpz8zPUiHhCVB2MR81WzT5N3ULvbWf7f1iANXG02bS7fRTHze&#13;\nrVsYDC+36wDP4wN8brLR/o/9UkfUPrw8vhGQu8dyShyK5j4zllflCOTva2OX1dJThLn4MuFFPBaf&#13;\nLB8TLOM0HD5iMO96Iojjqjx5P89Z&#13;\n-----END CERTIFICATE-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"AIK_SHA1\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">76a2851ce74ec07926aa70f6b7ed98ef6a59a168</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Instance_Id\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Trust_Status\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Trust_Policy\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">MeasureAndEnforce</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>",
     * "vm_trust_report":{"vm_report":{"vm_trust_policy":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:TrustPolicy xmlns:ns2=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns3=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><ns2:Director><ns2:CustomerId>testId</ns2:CustomerId></ns2:Director><ns2:Image><ns2:ImageId>9a156c2c-be6d-4f0c-b2d0-e848b5f6a3a7</ns2:ImageId><ns2:ImageHash>4bd5297bdc972066b154dd6cf497bf5427b3866eda00f67e0bce3fb9ba705602</ns2:ImageHash></ns2:Image><ns2:LaunchControlPolicy>MeasureAndEnforce</ns2:LaunchControlPolicy><ns2:Encryption><ns2:Key URL=\"uri\">http://10.1.71.73/v1/keys/f15c2ad1-00ef-4e8d-ae70-552be2f38782/transfer</ns2:Key><ns2:Checksum DigestAlg=\"md5\">fd84897d80c81f4699e8fb00231694dc</ns2:Checksum></ns2:Encryption><ns2:Whitelist DigestAlg=\"sha256\"><ns2:File Path=\"/bin/chown\">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</ns2:File></ns2:Whitelist><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>3TnKv6iVrVNzk6JVyvLw/UN7Zs4=</DigestValue></Reference></SignedInfo><SignatureValue>ZNYW08tkZJylGkjN+hR6N1LgWREFKIIWcjvYuA3UI/Mj1f3JTBGBYUWfUTsVNialrwiw9m8trG2B\n1LS6JOZ4o3O+/VoTFMIf5sBy0/lJZCpUFgC7PT9lbgycheOh+5t6S/l+yMEt7ue+3ov3GoXi62n0\nCSznkJi0pSSpv6YyWXt+AHiSKJkUlzuSETkJihiL8sfViqPNeX9CZAtVkGAviJrWEfkeZFJ07wSv\nPBNxTQnaIJD4U7yyurNV3doS3PVjAmEbGzquTG/F6Hyu4szHzEgurVM237DmM5s16F6XPPf1sQey\n2FQTVjw3y+zycqE6N0trcwcOom4RW+3W/33piA==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry\ncGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg\n59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9\nVW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R\nZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud\nDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b\nfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf\nbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7\n1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB\n5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk\niRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature></ns2:TrustPolicy>",
     * "vm_measurements":"<?xml version=\"1.0\"?>\n<Measurements xmlns=\"mtwilson:trustdirector:measurements:1.1\" DigestAlg=\"sha256\">\n<File Path=\"/bin/chown\">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</File>\n</Measurements>"},
     * "results":[{"rule":{"rule_name":"com.intel.mtwilson.policy.rule.VmMeasurementLogEquals","markers":["VM"]},"rule_name":"com.intel.mtwilson.policy.rule.VmMeasurementLogEquals","trusted":true}],"trusted":true}}
     * </xmp></pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation obj = client.retrieveVMAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </xmp></pre></div>
    */    
    public VMAttestation retrieveVMAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        VMAttestation obj = getTarget().path("vm-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(VMAttestation.class);
        return obj;
    }
    
    /**
     * Searches for the attestation results for the Virtual Machine (VM) with the specified criteria. Complete attestation report would be returned back to the caller for the
     * VMs matching the search criteria. If during the actual request, the host attestation report was also requested, then that would also be included.
     * @param criteria VMAttestationFilterCriteria object that specifies the search criteria.
     * The possible search options include one of the following
     * - VM attestation ID - A specific VM attestation report would be retrieved 
     * - VM instance ID - UUID of the VM instance - All reports for the specified VM instance would be retrieved.
     * - Host Name or IP address - Retrieves reports for all the VMs that were/are running on the specified host.
     * 
     * For both VM instance and Host name search criteria, user can additionally specify the below criteria.
     * - numberOfDays - Specifies the number of days back from the current date for which the attestations are needed. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * - fromDate & toDate - Specifies the date range for which the attestations are needed. Currently the following ISO 8601 date formats are supported
     *     -- date. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&fromDate=2015-05-01&toDate=2015-06-01
     *     -- date+time. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T00:00Z&toDate=2015-06-05T00:00Z
     *     -- date+time+zone. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T12:30-02:00&toDate=2015-06-05T12:30-02:00
     * 
     * Note that when the fromDate and toDate options are specified, the output includes the attestations from the fromDate upto the toDate but not including the
     * attestations from the toDate.
     * 
     * By default the last 10 attestation results would be returned back. The user can change this by additionally specifying the limit criteria (limit=5).
     * 
     * @return VMAttestationCollection object with a list of VM attestations matching the filter criteria. 
     * @since CIT 3.0
     * @mtwRequiresPermissions vm_attestations:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=61489ac0-51a9-4a33-a1ec-e6f67fd49bd4
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=61489ac0-51a9-4a33-a1ec-e6f67fd49bd4&limit=2
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=61489ac0-51a9-4a33-a1ec-e6f67fd49bd4&numberOfDays=5
     * https://server.com:8443/mtwilson/v2/vm-attestations?hostNameEqualTo=192.168.0.2&fromDate=2015-05-01&toDate=2015-06-01
     * 
     * Output for https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=8a227d37-420e-48f7-ac5a-ce30400aa1d5&fromDate=2016-02-12&toDate=2016-02-16 :
     * 
     * {"vmattestations":[{"id":"51fd9631-d8f0-4927-8b2a-f68023b38ce5","host_name":"mh-kvm-71","vm_instance_id":"8a227d37-420e-48f7-ac5a-ce30400aa1d5","trust_status":true,
     * "vm_saml":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><saml2:Assertion xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\" ID=\"VMTrustAssertion\" IssueInstant=\"2016-02-12T15:58:13.889Z\" Version=\"2.0\"><saml2:Issuer>https://server.com:8443</saml2:Issuer><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"#VMTrustAssertion\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>gVsEmbT8vXu9xXBoZ97qRWcm/Lg=</DigestValue></Reference></SignedInfo><SignatureValue>Cv2rEcGiZjYJPwoiiwlrydTbAbo1ZqAPGSdHVt9Iu8I5QsYF9W1bky5bvJtOMT+Ose4LHkWlURvu\nzsm+qgFYuE5ENcgJt3fqH7ZB7pb400g0ncAr5bRY8x23+TF3Ep5XKT0OnEOtKxauiV5uXTxYfZ+Y\nxzodoMbD+JlhVwOjelvarJ9EplKXf/zgV1tALIBo+msq/QFpOPHcdyK2U1lOZt52H1nw7dCITtZd\n5Z9O1fvP8mkwZOYBClO7QbM3lI+CBuNth8FmrbDm/mQVaBiDR6QGAUqvoOijYjUrqxkNtOkYv+NI\np9A1pVcI7zBgVg7Fq7sMhcwJRcUhV/t03RAd2g==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry\ncGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg\n59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9\nVW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R\nZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud\nDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b\nfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf\nbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7\n1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB\n5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk\niRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:NameID><saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:sender-vouches\"><saml2:NameID Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\">Cloud Integrity Technology</saml2:NameID><saml2:SubjectConfirmationData Address=\"server.com\" NotBefore=\"2016-02-12T15:58:13.889Z\" NotOnOrAfter=\"2016-02-12T16:58:13.889Z\"/></saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name=\"Host_Name\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">mh-kvm-71</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"AIK_Certificate\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">-----BEGIN CERTIFICATE-----&#13;\nMIICvTCCAaWgAwIBAgIGAVLV46YjMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u&#13;\nLXBjYS1haWswHhcNMTYwMjEyMTQzMDEyWhcNMjYwMjExMTQzMDEyWjAAMIIBIjANBgkqhkiG9w0B&#13;\nAQEFAAOCAQ8AMIIBCgKCAQEAwa43TlH7cBdJ6m5TBU7MDsLiFh3D6ncmOckqgU7wwO3nGOR8OIcz&#13;\nFATHNPRudr3rRKk2pDNMw3TyFEhMLn9QSgEyUIW07F8uBvpxjBgR2FqNLI+qwqxZUe3OLYnEuiwR&#13;\nW1ItCid6Z+CC0bnUIMSPu0ovYL/fcD92xAHYtD/B4zR2hUcXkNfaH89Az3as5YgQYn9GL8w/AiuU&#13;\njHqwAeavyF0+qfpaRY2UvJakVv6Gl/QjeN/mPL/DvutnrN3il6qNJikJYnxF8BhTCKPLJh7UsrCK&#13;\nyT8Rllm1Map/WCQvSWeDBX18aHRL5opbGRGQTfF2QbaNWVKtYvsqOe7B8HrqRQIDAQABoyIwIDAe&#13;\nBgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQAf9wEKsfnL&#13;\nP8hhFhmj/EWSWmeyoPcXPh6tD/XcYhVucNMf+ki3ZlUPJyaieOcSvfv0NWpYc7GngZLtmCyggfjU&#13;\nwvpKBqieqzocM/o4LpTk84h8KAY2V5qnlipsHheBQ3mwOMSfDXAjUaEUwesuwLhjHeno/rELxLMx&#13;\nVugf/hbVLJYZ9zfrnBGJvtuLAGfGpz8zPUiHhCVB2MR81WzT5N3ULvbWf7f1iANXG02bS7fRTHze&#13;\nrVsYDC+36wDP4wN8brLR/o/9UkfUPrw8vhGQu8dyShyK5j4zllflCOTva2OX1dJThLn4MuFFPBaf&#13;\nLB8TLOM0HD5iMO96Iojjqjx5P89Z&#13;\n-----END CERTIFICATE-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"AIK_SHA1\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">76a2851ce74ec07926aa70f6b7ed98ef6a59a168</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Instance_Id\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Trust_Status\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name=\"VM_Trust_Policy\"><saml2:AttributeValue xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">MeasureAndEnforce</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>",
     * "vm_trust_report":{"vm_report":{"vm_trust_policy":"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:TrustPolicy xmlns:ns2=\"mtwilson:trustdirector:policy:1.1\" xmlns:ns3=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><ns2:Director><ns2:CustomerId>testId</ns2:CustomerId></ns2:Director><ns2:Image><ns2:ImageId>9a156c2c-be6d-4f0c-b2d0-e848b5f6a3a7</ns2:ImageId><ns2:ImageHash>4bd5297bdc972066b154dd6cf497bf5427b3866eda00f67e0bce3fb9ba705602</ns2:ImageHash></ns2:Image><ns2:LaunchControlPolicy>MeasureAndEnforce</ns2:LaunchControlPolicy><ns2:Encryption><ns2:Key URL=\"uri\">http://10.1.71.73/v1/keys/f15c2ad1-00ef-4e8d-ae70-552be2f38782/transfer</ns2:Key><ns2:Checksum DigestAlg=\"md5\">fd84897d80c81f4699e8fb00231694dc</ns2:Checksum></ns2:Encryption><ns2:Whitelist DigestAlg=\"sha256\"><ns2:File Path=\"/bin/chown\">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</ns2:File></ns2:Whitelist><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>3TnKv6iVrVNzk6JVyvLw/UN7Zs4=</DigestValue></Reference></SignedInfo><SignatureValue>ZNYW08tkZJylGkjN+hR6N1LgWREFKIIWcjvYuA3UI/Mj1f3JTBGBYUWfUTsVNialrwiw9m8trG2B\n1LS6JOZ4o3O+/VoTFMIf5sBy0/lJZCpUFgC7PT9lbgycheOh+5t6S/l+yMEt7ue+3ov3GoXi62n0\nCSznkJi0pSSpv6YyWXt+AHiSKJkUlzuSETkJihiL8sfViqPNeX9CZAtVkGAviJrWEfkeZFJ07wSv\nPBNxTQnaIJD4U7yyurNV3doS3PVjAmEbGzquTG/F6Hyu4szHzEgurVM237DmM5s16F6XPPf1sQey\n2FQTVjw3y+zycqE6N0trcwcOom4RW+3W/33piA==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE\nCBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv\nbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw\nCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx\nEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA\nA4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry\ncGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg\n59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9\nVW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R\nZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud\nDgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b\nfzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf\nbHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7\n1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB\n5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk\niRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature></ns2:TrustPolicy>",
     * "vm_measurements":"<?xml version=\"1.0\"?>\n<Measurements xmlns=\"mtwilson:trustdirector:measurements:1.1\" DigestAlg=\"sha256\">\n<File Path=\"/bin/chown\">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</File>\n</Measurements>"},
     * "results":[{"rule":{"rule_name":"com.intel.mtwilson.policy.rule.VmMeasurementLogEquals","markers":["VM"]},"rule_name":"com.intel.mtwilson.policy.rule.VmMeasurementLogEquals","trusted":true}],"trusted":true}}]}
     * </xmp></pre></div>
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestationFilterCriteria criteria = new VMAttestationFilterCriteria();
     *   criteria.vmInstanceIdEqualTo = "14e03157-0935-442f-b4d6-1622154468e4";
     *   criteria.numberOfDays = 2;
     *   VMAttestationCollection objCollection = client.searchVmAttestations(criteria);
     * </xmp></pre></div>
     */    
    public VMAttestationCollection searchVMAttestations(VMAttestationFilterCriteria criteria) {        
        log.debug("target: {}", getTarget().getUri().toString());
        VMAttestationCollection objCollection = getTargetPathWithQueryParams("vm-attestations", criteria).request(MediaType.APPLICATION_JSON).get(VMAttestationCollection.class);
        return objCollection;
    }

    /**
     * Verifies the signature of the retrieved SAML assertion using the SAML certificate stored in the user keystore created during user registration.
     * This functionality is available for the Api library users only.
     * @param saml SAML assertion.
     * @return TrustAssertion object having the status of verification.
     * @since Mt.Wilson 3.0
     * @mtwSampleApiCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestationFilterCriteria criteria = new VMAttestationFilterCriteria();
     *   criteria.vmInstanceIdEqualTo = "14e03157-0935-442f-b4d6-1622154468e4";
     *   criteria.numberOfDays = 2;
     *   VMAttestationCollection objCollection = client.searchVmAttestations(criteria);
     *   for (VMAttestation obj : objCollection.getVMAttestations()) {
     *       TrustAssertion verifyTrustAssertion = client.verifyTrustAssertion(obj.getVmSaml());
     *   }
     * </xmp></pre></div>
     */        
    public TrustAssertion verifyTrustAssertion(String saml) throws KeyManagementException, ApiException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException {
        String mtwilsonApiKeystore = properties.getProperty("mtwilson.api.keystore");
        String mtwilsonApiKeystorePassword = properties.getProperty("mtwilson.api.keystore.password");
        
        if (properties == null || mtwilsonApiKeystore == null || mtwilsonApiKeystore.isEmpty()
                || mtwilsonApiKeystorePassword == null || mtwilsonApiKeystorePassword.isEmpty()) {
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
