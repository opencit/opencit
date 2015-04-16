/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.saml;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.tag.common.X509AttrBuilder;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.My;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.ASComponentFactory;
import com.intel.mtwilson.as.business.trust.HostTrustBO;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import com.intel.mtwilson.tag.model.x509.UTF8NameValueSequence;
import com.intel.mtwilson.datatypes.HostTrustStatus;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.saml.SamlAssertion;
import com.intel.mtwilson.saml.SamlGenerator;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class SAMLTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SAMLTest.class);
    private String hostname = "10.1.71.175"; // TODO:  use mtwilson-env
//    private String hostname = "10.1.70.64"; // TODO:  use mtwilson-env
    private boolean forceVerify = true;
    
    /**
     * sample output using jdk7u13:
     * 
<?xml version="1.0" encoding="UTF-8"?><saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" ID="HostTrustAssertion" IssueInstant="2014-02-23T04:46:05.708Z" Version="2.0"><saml2:Issuer>https://127.0.0.1:8080</saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/><SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#HostTrustAssertion"><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>aqE/pYDJljeOMQ1Pj7FOtk8f6Kk=</DigestValue></Reference></SignedInfo><SignatureValue>G6XeqweiWyqnISNC/1G38H6jejdgbApJcPXVB55lN2NNKL8HStxoPehKwlZU1Sd5fNlFWr35mR2U
2RlWh082pTwktte4AgM0TkCR2BSpSqXEGhA4o5YVfkkXl2qAVZbUaK7pUop2IuOiOUZZP+1dfSWA
Vn7t39KrS2k4jPgGaU7zrgt7Tqc2CtYAC1d4F+6/dCKpd7g76YirUeS90MCenCkmc9dpViAJqsRm
hIOC8r6QVtrcehbmsg9pRjVvMWbaxyeP+r1XyMXDHFRpvQ96QB6cDCZYbRpR9zC4HKJIk9bnuORH
PNnY6cojC1PmHSOBCb91TSv7TUZRZ98EyJC1dw==</SignatureValue><KeyInfo><X509Data><X509Certificate>MIIC1DCCAbygAwIBAgIIOs/DIxyqZkgwDQYJKoZIhvcNAQELBQAwKTERMA8GA1UECxMIbXR3aWxz
b24xFDASBgNVBAMTC210d2lsc29uLWNhMB4XDTE0MDIyMTA3MDk0N1oXDTE1MDIyMTA3MDk0N1ow
KzERMA8GA1UECxMIbXR3aWxzb24xFjAUBgNVBAMTDW10d2lsc29uLXNhbWwwggEiMA0GCSqGSIb3
DQEBAQUAA4IBDwAwggEKAoIBAQCpOLLVQYZrMhUFPJeAaagorGgF6GSRkLWF981nFkxHdYVLXguz
lKuJRj7fnqVUPtXuRB5TvpED4EUdYrWBTAKajCuUFB+gQazEMFNjm4GolfDmh+RTHZKVY+kdNIIy
tOx+n9tGO+kGSBLjJeaPtlMb2TsDS7pWlrO3kRkkfjM5Wt8yCMWNCMikKz1PXA9ynpC1cMf/i3lN
XG8bOHY5URDXCXdl8IZ1OMUV2yIQ4I5PyFpw9kdiGlW2TvQCMJWyfg+/Sfl9M28Yg1m/nwGY6SdV
paFkxNKfBirfiHm+1pk6WSELEvSy+eWILk48C5P6vP9UVm4lzaM3zX6zWUkBnIE9AgMBAAEwDQYJ
KoZIhvcNAQELBQADggEBAJmm8okpAajnUoRGiMHPgjqVwD2kjH5+j0Hu9gSWPC4bOeHPleGPLcCa
60MBMWmc3RAH3UxUGlY5augZcXvEcftBUUDvgwQnkvHV2CDsrKN+3qDoVzsGs5NdNMrWO5Ho2VQf
5jJNqQjmU2+m3oCU4Aa9Mwoqhiio9XiS+yQ5L84PqBqAmJX4M548zc9yYqsTvAfFwNFBtlav6xS6
adQFeHGfM6SCxnn0LE/9Xa6wT+9pC29/mBtbdxRoHyntdwa6JoFxjni8dCsPP4Tr5NCXuoiTCAgP
55gw0BInWluHocdkrXzaDmrOYoe9N6nuGbSkJ/TQbW8nX6jo4k4BllugaRY=</X509Certificate></X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">10.1.71.175</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches"><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">AttestationService-0.5.4</saml2:NameID><saml2:SubjectConfirmationData Address="10.254.36.218" NotBefore="2014-02-23T04:46:05.755Z" NotOnOrAfter="2014-02-23T05:46:05.755Z"/></saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name="Trusted"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_BIOS"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Name"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">Intel_Corporation</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Version"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">01.00.0063</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_OEM"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">Intel Corporation</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_VMM"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VMM_Name"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">Intel_Thurley_VMware_ESXi</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VMM_Version"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">5.1.0-1065491</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VMM_OSName"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">VMware_ESXi</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="VMM_OSVersion"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">5.1.0</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Asset_Tag"><saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">false</saml2:AttributeValue></saml2:Attribute></saml2:AttributeStatement></saml2:Assertion>
* 
     */
    @Test
    public void testGenerateAndVerifySaml() throws Exception {
        // generate SAML
//        Extensions.register(VendorHostAgentFactory.class, IntelHostAgentFactory.class); // because 10.1.70.64 is trust agent
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class); // because 10.1.71.175 is esxi
        HostTrustBO hostTrustBO = ASComponentFactory.getHostTrustBO();
        String saml = hostTrustBO.getTrustWithSaml(hostname, forceVerify);
        log.debug("saml = {}", saml);
        // verify SAML
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getSamlKeystoreFile(), My.configuration().getSamlKeystorePassword());
//        X509Certificate[] trusted = keystore.getTrustedCertificates(SimpleKeystore.SAML); // this works for the api client's keystore
        X509Certificate[] trusted = new X509Certificate[] { keystore.getX509Certificate(keystore.aliases()[0]) }; // this works for mtwilson's mtwilson-saml.jks keystore
        TrustAssertion trustAssertion = new TrustAssertion(trusted, saml);
        print(trustAssertion);
    }
    
    @Test
    public void testGenerateAndVerifyMultihostSaml() throws Exception {
        // generate SAML
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class); // because 10.1.71.175 is esxi
        HostTrustBO hostTrustBO = ASComponentFactory.getHostTrustBO();
        ArrayList<String> hostnames = new ArrayList<String>();
        hostnames.add(hostname);
        hostnames.add(hostname);
        String saml = hostTrustBO.getTrustWithSamlForHostnames(hostnames);
        log.debug("saml = {}", saml);
        // verify SAML
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getSamlKeystoreFile(), My.configuration().getSamlKeystorePassword());
//        X509Certificate[] trusted = keystore.getTrustedCertificates(SimpleKeystore.SAML); // this works for the api client's keystore
        X509Certificate[] trusted = new X509Certificate[] { keystore.getX509Certificate(keystore.aliases()[0]) }; // this works for mtwilson's mtwilson-saml.jks keystore
        TrustAssertion trustAssertion = new TrustAssertion(trusted, saml);
        print(trustAssertion);
    }
    
    
    /**
     * Example output (no aik or certificate because it was a vmware esxi host):
2014-02-22 20:59:37,111 DEBUG [main] t.s.SAMLTest [SAMLTest.java:66] isValid true
2014-02-22 20:59:37,112 DEBUG [main] t.s.SAMLTest [SAMLTest.java:71] aikCertificate null
2014-02-22 20:59:37,112 DEBUG [main] t.s.SAMLTest [SAMLTest.java:72] aikPublicKey null
2014-02-22 20:59:37,113 DEBUG [main] t.s.SAMLTest [SAMLTest.java:73] date Sat Feb 22 20:46:05 PST 2014
2014-02-22 20:59:37,113 DEBUG [main] t.s.SAMLTest [SAMLTest.java:74] issuer https://127.0.0.1:8080
2014-02-22 20:59:37,114 DEBUG [main] t.s.SAMLTest [SAMLTest.java:75] subjectFormat urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified
2014-02-22 20:59:37,114 DEBUG [main] t.s.SAMLTest [SAMLTest.java:76] subject 10.1.71.175
2014-02-22 20:59:37,114 DEBUG [main] t.s.SAMLTest [SAMLTest.java:77] isHostBiosTrusted true
2014-02-22 20:59:37,116 DEBUG [main] t.s.SAMLTest [SAMLTest.java:78] isHostVmmTrusted true
2014-02-22 20:59:37,116 DEBUG [main] t.s.SAMLTest [SAMLTest.java:79] isHostLocationTrusted false
2014-02-22 20:59:37,116 DEBUG [main] t.s.SAMLTest [SAMLTest.java:80] isHostTrusted true
2014-02-22 20:59:37,116 DEBUG [main] t.s.SAMLTest [SAMLTest.java:82] 11 attributes
2014-02-22 20:59:37,117 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute BIOS_Name value Intel_Corporation
2014-02-22 20:59:37,117 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute VMM_Name value Intel_Thurley_VMware_ESXi
2014-02-22 20:59:37,117 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute VMM_OSName value VMware_ESXi
2014-02-22 20:59:37,117 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute BIOS_OEM value Intel Corporation
2014-02-22 20:59:37,117 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute Trusted_BIOS value true
2014-02-22 20:59:37,117 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute Trusted_VMM value true
2014-02-22 20:59:37,118 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute BIOS_Version value 01.00.0063
2014-02-22 20:59:37,118 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute VMM_Version value 5.1.0-1065491
2014-02-22 20:59:37,118 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute Trusted value true
2014-02-22 20:59:37,118 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute VMM_OSVersion value 5.1.0
2014-02-22 20:59:37,118 DEBUG [main] t.s.SAMLTest [SAMLTest.java:84] attribute Asset_Tag value false
     * 
     * @param trustAssertion
     * @throws Exception 
     */
    private void print(TrustAssertion trustAssertion) throws Exception {
        log.debug("isValid {}", trustAssertion.isValid());
        if( !trustAssertion.isValid() ) {
            log.debug("error {}", trustAssertion.error());
            return;
        }
        Set<String> hostnames = trustAssertion.getHosts();
        for(String hostname : hostnames) {
            HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostname);
        log.debug("aikCertificate {}", hostTrustAssertion.getAikCertificate());
        log.debug("aikPublicKey {}", hostTrustAssertion.getAikPublicKey());
        log.debug("date {}", hostTrustAssertion.getDate());
        log.debug("issuer {}", hostTrustAssertion.getIssuer());
        log.debug("subjectFormat {}", hostTrustAssertion.getSubjectFormat());
        log.debug("subject {}", hostTrustAssertion.getSubject());
        log.debug("isHostBiosTrusted {}", hostTrustAssertion.isHostBiosTrusted());
        log.debug("isHostVmmTrusted {}", hostTrustAssertion.isHostVmmTrusted());
        log.debug("isHostLocationTrusted {}", hostTrustAssertion.isHostLocationTrusted());
        log.debug("isHostTrusted {}", hostTrustAssertion.isHostTrusted());
        Set<String> attributeNames = hostTrustAssertion.getAttributeNames();
        log.debug("{} attributes", attributeNames.size());
        for(String attributeName : attributeNames) {
            log.debug("attribute {} value {}", attributeName,  hostTrustAssertion.getStringAttribute(attributeName));            
        }
        }
        
    }
    
    @Test
    public void testGenerateSamlWithAssetTags() throws Exception {
        // equivalent of private SamlGenerator getSamlGenerator() in HostTrustBO:
        SamlGenerator samlGenerator = new SamlGenerator(new CommonsConfiguration(My.configuration().getConfiguration()));
        samlGenerator.setIssuer("junit-test"); // String defaultIssuer = "https://" + localhost.getHostAddress() + ":8181/AttestationService"; // TODO:  need  to get our local address from the web container or configuration (mtwilson.api.baseurl) instead of hard-coding this here
        // generate assertion
        HostTrustStatus trust = new HostTrustStatus();
        trust.asset_tag = true;
        trust.bios = true;
        trust.location = true;
        trust.vmm = true;
        TxtHostRecord data = new TxtHostRecord();
        data.BIOS_Name = "Generic BIOS";
        data.BIOS_Version = "1.0";
        data.BIOS_Oem = "Generic OEM";
        data.Hardware_Uuid = new UUID().toString();
        data.HostName = "localhost";
        data.IPAddress = "127.0.0.1";
        data.Location = "here";
        data.VMM_Name = "Generic VMM";
        data.VMM_Version = "1.0";
        data.VMM_OSName = "Generic OS";
        data.VMM_OSVersion = "1.0";
        TxtHost host = new TxtHost(data, trust);
        // generate asset tag cert
        // first, create the CA key pair and certificate
        KeyPair cakey = RsaUtil.generateRsaKeyPair(2048);
        X509Certificate cacert = X509Builder.factory().selfSigned("CN=Attr CA,OU=CPG,OU=DCSG,O=Intel,ST=CA,C=US", cakey).build();
        // second, create the attribute certificate
        X509AttrBuilder builder = X509AttrBuilder.factory();
        byte[] attrCertBytes = builder
                .issuerName(cacert).issuerPrivateKey(cakey.getPrivate())
                .expires(1, TimeUnit.HOURS)
                .subjectUuid(new UUID())
                .attribute("Country", "US")
                .attribute("State", "CA", "TX")
                .build();
        X509AttributeCertificate cert = X509AttributeCertificate.valueOf(attrCertBytes);
        // generate the assertion with trutsed host information and trusted tag certificate
        SamlAssertion samlAssertion = samlGenerator.generateHostAssertion(host, cert, null);
        // verify and print the assertion contents
//        SamlConfiguration samlConfiguration = new SamlConfiguration(new CommonsConfigurationAdapter(My.configuration().getConfiguration()));
         SimpleKeystore keystore = new SimpleKeystore(My.configuration().getSamlKeystoreFile(), My.configuration().getSamlKeystorePassword());
//        X509Certificate[] trusted = keystore.getTrustedCertificates(SimpleKeystore.SAML); // this works for the api client's keystore
        X509Certificate[] trusted = new X509Certificate[] { keystore.getX509Certificate(keystore.aliases()[0]) }; // this works for mtwilson's mtwilson-saml.jks keystore
       TrustAssertion trustAssertion = new TrustAssertion(trusted, samlAssertion.assertion);        
        print(trustAssertion);
    }
}
