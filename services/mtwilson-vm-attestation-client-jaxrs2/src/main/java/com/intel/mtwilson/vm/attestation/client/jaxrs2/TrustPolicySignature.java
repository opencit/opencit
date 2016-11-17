/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.vm.attestation.client.jaxrs2;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.trustpolicy1.xml.TrustPolicy;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author boskisha
 */
public class TrustPolicySignature extends MtWilsonClient {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustPolicySignature.class);

    public TrustPolicySignature(Properties properties) throws Exception {
        super(properties);
    }
    public TrustPolicySignature(URL url) throws Exception {
        super(url);
    }
    
//    public TrustPolicy signTrustPolicy(TrustPolicy trustPolicy) throws IOException, JAXBException, XMLStreamException {
//        log.debug("target: {}", getTarget().getUri().toString());
//        JAXB jaxb = new JAXB();
//        String signedPolicy = getTarget().path("trustpolicy-signature").request().accept(MediaType.APPLICATION_XML).post(Entity.xml(jaxb.write(trustPolicy)), String.class);
//        return jaxb.read(signedPolicy, TrustPolicy.class);        
//    }
    
    /**
     * Creates attestation server digital signature over input trust policy. Input trust policy must follow trustpolicy schema defined by trust director. 
     * @param uuid - UUID of the cached attestation. 
     * @return VMAttestation object with the attestation report.
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions host_attestations:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/vm-attestations/51fd9631-d8f0-4927-8b2a-f68023b38ce5
     * Output: {"id": "51fd9631-d8f0-4927-8b2a-f68023b38ce5"
     * "host_name": "192.168.1.2"
     * "vm_instance_id": "8a227d37-420e-48f7-ac5a-ce30400aa1d5"
     * "trust_status": true
     * "vm_saml": "<?xml version="1.0" encoding="UTF-8"?><saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="VMTrustAssertion" 
     * IssueInstant="2016-02-12T15:58:13.889Z" Version="2.0"><saml2:Issuer>https://server.com:8443</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
     * <SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
     * <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#VMTrustAssertion"><Transforms>
     * <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
     * <DigestValue>gVsEmbT8vXu9xXBoZ97qRWcm/Lg=</DigestValue></Reference></SignedInfo><SignatureValue>Cv2rEcGiZjYJPwoiiwlrydTbAbo1ZqAPGSdHVt9Iu8I5QsYF9W1bky5bvJtOMT+Ose4LHkWlURvu 
     * zsm+qgFYuE5ENcgJt3fqH7ZB7pb400g0ncAr5bRY8x23+TF3Ep5XKT0OnEOtKxauiV5uXTxYfZ+Y xzodoMbD+JlhVwOjelvarJ9EplKXf/zgV1tALIBo+msq/QFpOPHcdyK2U1lOZt52H1nw7dCITtZd 
     * 5Z9O1fvP8mkwZOYBClO7QbM3lI+CBuNth8FmrbDm/mQVaBiDR6QGAUqvoOijYjUrqxkNtOkYv+NI p9A1pVcI7zBgVg7Fq7sMhcwJRcUhV/t03RAd2g==</SignatureValue><KeyInfo><X509Data>
     * <X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE CBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv 
     * bjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx 
     * EjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA A4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry 
     * cGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg 59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9 
     * VW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R ZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud 
     * DgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b fzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf 
     * bHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7 1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB 
     * 5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk iRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature>
     * <saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:NameID>
     * <saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     * <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">Cloud Integrity Technology</saml2:NameID>
     * <saml2:SubjectConfirmationData Address="server.com" NotBefore="2016-02-12T15:58:13.889Z" NotOnOrAfter="2016-02-12T16:58:13.889Z"/></saml2:SubjectConfirmation>
     * </saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name="Host_Name">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">192.168.1.2</saml2:AttributeValue>
     * </saml2:Attribute><saml2:Attribute Name="AIK_Certificate"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" 
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">-----BEGIN CERTIFICATE-----&#13; 
     * MIICvTCCAaWgAwIBAgIGAVLV46YjMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u&#13; LXBjYS1haWswHhcNMTYwMjEyMTQzMDEyWhcNMjYwMjExMTQzMDEyWjAAMIIBIjANBgkqhkiG9w0B&#13; 
     * AQEFAAOCAQ8AMIIBCgKCAQEAwa43TlH7cBdJ6m5TBU7MDsLiFh3D6ncmOckqgU7wwO3nGOR8OIcz&#13; FATHNPRudr3rRKk2pDNMw3TyFEhMLn9QSgEyUIW07F8uBvpxjBgR2FqNLI+qwqxZUe3OLYnEuiwR&#13; 
     * W1ItCid6Z+CC0bnUIMSPu0ovYL/fcD92xAHYtD/B4zR2hUcXkNfaH89Az3as5YgQYn9GL8w/AiuU&#13; jHqwAeavyF0+qfpaRY2UvJakVv6Gl/QjeN/mPL/DvutnrN3il6qNJikJYnxF8BhTCKPLJh7UsrCK&#13; 
     * yT8Rllm1Map/WCQvSWeDBX18aHRL5opbGRGQTfF2QbaNWVKtYvsqOe7B8HrqRQIDAQABoyIwIDAe&#13; BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQAf9wEKsfnL&#13; 
     * P8hhFhmj/EWSWmeyoPcXPh6tD/XcYhVucNMf+ki3ZlUPJyaieOcSvfv0NWpYc7GngZLtmCyggfjU&#13; wvpKBqieqzocM/o4LpTk84h8KAY2V5qnlipsHheBQ3mwOMSfDXAjUaEUwesuwLhjHeno/rELxLMx&#13; 
     * Vugf/hbVLJYZ9zfrnBGJvtuLAGfGpz8zPUiHhCVB2MR81WzT5N3ULvbWf7f1iANXG02bS7fRTHze&#13; rVsYDC+36wDP4wN8brLR/o/9UkfUPrw8vhGQu8dyShyK5j4zllflCOTva2OX1dJThLn4MuFFPBaf&#13; 
     * LB8TLOM0HD5iMO96Iojjqjx5P89Z&#13; -----END CERTIFICATE-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_SHA1">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">76a2851ce74ec07926aa70f6b7ed98ef6a59a168</saml2:AttributeValue>
     * </saml2:Attribute><saml2:Attribute Name="VM_Instance_Id"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">8a227d37-420e-48f7-ac5a-ce30400aa1d5</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VM_Trust_Status">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">true</saml2:AttributeValue>
     * </saml2:Attribute><saml2:Attribute Name="VM_Trust_Policy"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" 
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">MeasureAndEnforce</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>"
     * "vm_trust_report": {
     * "vm_report": {
     * "vm_trust_policy": "<?xml version="1.0" encoding="UTF-8"?><ns2:TrustPolicy xmlns:ns2="mtwilson:trustdirector:policy:1.1" xmlns:ns3="http://www.w3.org/2000/09/xmldsig#" xmlns:xs="http://www.w3.org/2001/XMLSchema"><ns2:Director><ns2:CustomerId>testId</ns2:CustomerId></ns2:Director><ns2:Image><ns2:ImageId>9a156c2c-be6d-4f0c-b2d0-e848b5f6a3a7</ns2:ImageId><ns2:ImageHash>4bd5297bdc972066b154dd6cf497bf5427b3866eda00f67e0bce3fb9ba705602</ns2:ImageHash></ns2:Image><ns2:LaunchControlPolicy>MeasureAndEnforce</ns2:LaunchControlPolicy><ns2:Encryption><ns2:Key URL="uri">http://10.1.71.73/v1/keys/f15c2ad1-00ef-4e8d-ae70-552be2f38782/transfer</ns2:Key><ns2:Checksum DigestAlg="md5">fd84897d80c81f4699e8fb00231694dc</ns2:Checksum></ns2:Encryption><ns2:Whitelist DigestAlg="sha256"><ns2:File Path="/bin/chown">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</ns2:File></ns2:Whitelist><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>3TnKv6iVrVNzk6JVyvLw/UN7Zs4=</DigestValue></Reference></SignedInfo><SignatureValue>ZNYW08tkZJylGkjN+hR6N1LgWREFKIIWcjvYuA3UI/Mj1f3JTBGBYUWfUTsVNialrwiw9m8trG2B 1LS6JOZ4o3O+/VoTFMIf5sBy0/lJZCpUFgC7PT9lbgycheOh+5t6S/l+yMEt7ue+3ov3GoXi62n0 CSznkJi0pSSpv6YyWXt+AHiSKJkUlzuSETkJihiL8sfViqPNeX9CZAtVkGAviJrWEfkeZFJ07wSv PBNxTQnaIJD4U7yyurNV3doS3PVjAmEbGzquTG/F6Hyu4szHzEgurVM237DmM5s16F6XPPf1sQey 2FQTVjw3y+zycqE6N0trcwcOom4RW+3W/33piA==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEBMi5xzANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UE CBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNv bjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjExMjEyOTQwWhcNMjYwMjA4MjEyOTQwWjBiMQsw CQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwx EjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUA A4IBDwAwggEKAoIBAQCa8aDs68qIuhhDel1mOOQpClH+zAul6bef9rlrfh6a1xz7eW4qxxoLpRry cGpTtUsAJ+CEDl4LcX+VHC9WE6F8a6rPGj3VA17EiuceBMBrRnRTAV0/7Z2xnetyj7cFnTC+hppg 59yqtOXSXuqDsh+WaHx7toVWXGQe7S/mxTcE4MIZdOoLubFHim/cuc9ILltuXijZrllD9Pn9DVt9 VW8oRVIgRZCvqlXcrO3MtY/XFvfSZhCXTZ5I4sQut4btyBELa2yxPOeyGzVviYR3KGjl4x4Hxu0R ZDTyZBLNPYl0Qj9kpnULbGvZy2QNE8B7yMNWCaA0sdMFJ0vB6pKEyv5TAgMBAAGjITAfMB0GA1Ud DgQWBBRPtJHLhobP5TGEu0fsR7bQD9FJgTANBgkqhkiG9w0BAQsFAAOCAQEAdp9Mo25XyDUGkg6b fzXA0h87YEAbaaZGGQifYJN5nxxsjWTy8IHOkXSOBUJ6kAu7R+73GvTOAxS/d417eG/GAwjzJlBf bHp12P8Rx0fKtmsnqJCzI5Z+S4mKEdQq7wcc80H4LSgyV3Wvkj6rVEZdofC6KL4s5mq13tD43rr7 1DUvLSzG5sWM1yzBw7nHh9Ho1yXdRr8purv3xRf8MI0OcTZIQLb+N5G5kSbIZ5oLpGnhndXkHHSB 5eFhJIf9C/fdM9GFjMzXuPnSNKJGhnqMeVr/v8uTS0VLXhfR6hH2NlaJMnlP9RNfaZHHNLUuvZZk iRG5APE8sGy13q25XeltLw==</X509Certificate></X509Data></KeyInfo></Signature></ns2:TrustPolicy>"
     * "vm_measurements": "<?xml version="1.0"?> <Measurements xmlns="mtwilson:trustdirector:measurements:1.1" DigestAlg="sha256"> <File Path="/bin/chown">31f9e039d77aa9e220828d439c8254d51748bc77c3ee3721df8f4274d0638fb4</File> </Measurements>"
     * }-
     * "results": [1]
     * 0:  {
     * "rule": {
     * "rule_name": "com.intel.mtwilson.policy.rule.VmMeasurementLogEquals"
     * "markers": [1]
     * 0:  "VM"
     * -
     * }-
     * "rule_name": "com.intel.mtwilson.policy.rule.VmMeasurementLogEquals"
     * "trusted": true
     * }-
     * -
     * "trusted": true
     * }-
     * }-
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation obj = client.retrieveVMAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </pre>
    */    
    
    /**
     * Creates attestation server digital signature over input trust policy. Input trust policy must follow trustpolicy schema defined by trust director. Ensure that both the Accept
     * and Content-Type headers are set to "application/xml"
     * @param trustPolicy in XML format
     * @return signed trustPolicy in XML format.
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <div style="word-wrap: break-word; width: 800px"><pre><xmp>
     * https://server.com:8181/mtwilson/v2/trustpolicy-signature
     * 
     * Input: <?xml version="1.0" encoding="UTF-8" standalone="yes"?><TrustPolicy xmlns:ns2="http://www.w3.org/2000/09/xmldsig#" xmlns="mtwilson:trustdirector:policy:1.1" xmlns:xs="http://www.w3.org/2001/XMLSchema"><Director><CustomerId>testId</CustomerId></Director><Image><ImageId>5a04c975-cc2c-43ba-a80b-66e646669256</ImageId><ImageHash>43cd01e1b5a8358bd60e43a839eae7026887c47db829accc0d1feeb44ab2367d</ImageHash></Image><LaunchControlPolicy>MeasureOnly</LaunchControlPolicy><Whitelist DigestAlg="sha256"><Dir Include="*" Exclude="" Recursive="true" Path="/boot">0c8d4de5a9e5d494bf3284a883a4220c36892d537895973125b1ce0f423d9b55</Dir><File Path="/boot/grub/menu.lst">0a1cfafd98f3f87dc079d41b9fe1391dcac8a41badf2d845648f95fe0edcd6c4</File><File Path="/boot/vmlinuz-3.0.0-12-virtual">fd844dea53352d5165a056bbb0f1af5af195600545de601c824decd5a30d3c49</File><File Path="/boot/config-3.0.0-12-virtual">2be73211f10b30c5d2705058d4d4991d0108b3b787578145a7e8dfb740b7c232</File><File Path="/boot/grub/stage1">77c1024a494c2170d0236dabdb795131d8a0f1809792735b3dd7f563ef5d951e</File><File Path="/boot/grub/e2fs_stage1_5">1d317c1e94328cdbe00dc05d50b02f0cb9ec673159145b7f4448cec28a33dc14</File><File Path="/boot/initrd.img-3.0.0-12-virtual">683972bff3c4d3d69f25504a2ca0a046772e21ebba4c67e4b857f4061e3cb143</File><File Path="/boot/grub/stage2">5aa718ea1ecc59140eef959fc343f8810e485a44acc35805a0f6e9a7ffb10973</File></Whitelist></TrustPolicy>
     * 
     * Output: <?xml version="1.0" encoding="UTF-8"?><ns3:TrustPolicy xmlns:ns3="mtwilson:trustdirector:policy:1.1" xmlns:ns2="http://www.w3.org/2000/09/xmldsig#" xmlns:xs="http://www.w3.org/2001/XMLSchema"><ns3:Director><ns3:CustomerId>testId</ns3:CustomerId></ns3:Director><ns3:Image><ns3:ImageId>680039e7-d5d3-4fdc-9c16-2f840e980d6e</ns3:ImageId><ns3:ImageHash>43cd01e1b5a8358bd60e43a839eae7026887c47db829accc0d1feeb44ab2367d</ns3:ImageHash></ns3:Image><ns3:LaunchControlPolicy>MeasureOnly</ns3:LaunchControlPolicy><ns3:Whitelist DigestAlg="sha256"><ns3:Dir Exclude="" Include="*" Path="/boot" Recursive="true">0c8d4de5a9e5d494bf3284a883a4220c36892d537895973125b1ce0f423d9b55</ns3:Dir><ns3:File Path="/boot/grub/menu.lst">0a1cfafd98f3f87dc079d41b9fe1391dcac8a41badf2d845648f95fe0edcd6c4</ns3:File><ns3:File Path="/boot/vmlinuz-3.0.0-12-virtual">fd844dea53352d5165a056bbb0f1af5af195600545de601c824decd5a30d3c49</ns3:File><ns3:File Path="/boot/config-3.0.0-12-virtual">2be73211f10b30c5d2705058d4d4991d0108b3b787578145a7e8dfb740b7c232</ns3:File><ns3:File Path="/boot/grub/stage1">77c1024a494c2170d0236dabdb795131d8a0f1809792735b3dd7f563ef5d951e</ns3:File><ns3:File Path="/boot/grub/e2fs_stage1_5">1d317c1e94328cdbe00dc05d50b02f0cb9ec673159145b7f4448cec28a33dc14</ns3:File><ns3:File Path="/boot/initrd.img-3.0.0-12-virtual">683972bff3c4d3d69f25504a2ca0a046772e21ebba4c67e4b857f4061e3cb143</ns3:File><ns3:File Path="/boot/grub/stage2">5aa718ea1ecc59140eef959fc343f8810e485a44acc35805a0f6e9a7ffb10973</ns3:File></ns3:Whitelist><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>lA8S7VAMlaTYOhAE5lbkiBdMijo=</DigestValue></Reference></SignedInfo><SignatureValue>nbywbfSmaINOLRWFWZvhDNtvsDFCUs2KKW37S5q4x+Tdqg2G76mGIUKm0WOaHiiDBERPY9YaWgHKYG3CmLe6jmbDPtLU6nP+Yrne6S9vA8YjFelp7mYwzsvsModc6hwGfY2Er64nZHmg8tiyzqIi20sL3YRGBKR4xoeRuSScNgzw76NPRVpCnIjqhCSvi6/X7+42KstUuI8kGZ0n2Pf5KXr14/oDCVVS1nOlVosSRmn70DKRt/GK1J6RDFdG78Kz1q3EOmws8p6523uTHVorQ6aTC6mS1hDtvKqA46Ph5/iysRyo/1n6sMe2Cy3gJODjQx/Y3FFzipdGs32EuURreg==</SignatureValue><KeyInfo><X509Data><X509SubjectName>CN=mtwilson,OU=Mt Wilson,O=Intel,L=Folsom,ST=CA,C=US</X509SubjectName><X509Certificate>MIIDYzCCAkugAwIBAgIEURIDxDANBgkqhkiG9w0BAQsFADBiMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wHhcNMTYwMjEwMDMxNTI5WhcNMjYwMjA3MDMxNTI5WjBiMQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExDzANBgNVBAcTBkZvbHNvbTEOMAwGA1UEChMFSW50ZWwxEjAQBgNVBAsTCU10IFdpbHNvbjERMA8GA1UEAxMIbXR3aWxzb24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCl8aIBlcQL77iIr7BEr0JmGAjummFU9BF9iblkrfbx7uYUc+zoe9oWxHzl465P7tmDz3R8USaldmWSlk1+S2UNZfpRpHl+ksr78RX3FxDjlu4CGu/6uz25ht27JakA6s6DVoTOfoF24bnwVTJ0xfBnRmByoSdoqtO1mzz7/I6+W3716excbGlbh3yp9X2w5qnZSKHkfNDGKKO7irnokde6//LY98YmyK5oy6BkIK2lrEYn3fSahYY92higWiEvgsAk5iU/wRtI8ynM0Kaapoox8uhrceF80Adewh7dfxDt9qS6rWpwlELhrfI6acWx9vP3eX/ECoSPLwHmlwGXEzkvAgMBAAGjITAfMB0GA1UdDgQWBBRBTHDGgh1qn7C+J4uDzeD9PtxyMDANBgkqhkiG9w0BAQsFAAOCAQEARkK5d8s2mkW72st9HCSmpEYA8zi8nCG1fJd9vLLoB1SeRacW7fvSX5NDX+T0JRtxvwGhpmks7OiUlwV/jXLNnLO7a1b8JLvCicv+GsNwGHIsq86nkbUeK7K5hkBZjpsUv95yHUqB+zYJDlBnbCBYVkoG1heDL7kaGjsVyfJBfq1LQ6Tmrr6CDGgsSN8zKUBj9InJYFH/aa1o4gLpxak6LhI8H/1h3IFhJ+BFi7oJwfNOLVnG1G1/khuLGoFBvxxSpx1odtdhT8XJqaRtzry4CzRpRokaW4NX/3Q8XvuVnfHOQYG6Irsr4ZCq1e+075Jgqcype43RMgcmJC8BqAmImQ==</X509Certificate></X509Data></KeyInfo></Signature></ns3:TrustPolicy>
     * </xmp></pre></div>
     * @throws IOException
     * @throws JAXBException
     * @throws XMLStreamException 
     */
    public String signTrustPolicy(String trustPolicy) throws IOException, JAXBException, XMLStreamException {
        log.debug("target: {}", getTarget().getUri().toString());
        String signedPolicy = getTarget().path("trustpolicy-signature").request().accept(MediaType.APPLICATION_XML).post(Entity.xml(trustPolicy), String.class);
        return signedPolicy;        
    }
}
