/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.TrustAssertion;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponseList;
import com.intel.dcsg.cpg.configuration.CommonsConfigurationUtil;
import com.intel.mtwilson.model.*;
import com.intel.mtwilson.saml.TrustAssertion.HostTrustAssertion;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import static org.junit.Assert.*;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Statement;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;

/**
 *
 * @author jbuhacoff
 */
public class SamlTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testLoadSettings() throws IOException {
        Configuration conf = CommonsConfigurationUtil.fromResource("/localhost-0.5.2.properties");
        System.out.println(conf.getString("mtwilson.api.baseurl", "unable to load settings"));
    }
    
    @Test
    public void testJoinHostnamesWithComma() {
        ArrayList<Hostname> hostnames = new ArrayList<Hostname>();
        hostnames.add(new Hostname("1.2.3.4"));
        hostnames.add(new Hostname("5.6.7.8"));
        String hostnamesCSV = StringUtils.join(hostnames, ","); // calls toString() on each hostname
        System.out.println(hostnamesCSV);
    }
    
    @Test
    public void testGetSamlCertificate() throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, IOException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException, ClientException {
        Configuration config = CommonsConfigurationUtil.fromResource("/localhost-0.5.2.properties");
        ApiClient api = new ApiClient(config);
        X509Certificate certificate = api.getSamlCertificate();
        log.debug("SAML Certificate Subject: {}", certificate.getSubjectX500Principal().getName());
        log.debug("SAML Certificate Issuer: {}", certificate.getIssuerX500Principal().getName());
        URL attestationService = new URL(config.getString("mtwilson.api.baseurl"));
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        keystore.addTrustedSamlCertificate(certificate, attestationService.getHost());
        keystore.save();
        log.info("Saved SAML certificate in keystore");
    }
    
    @Test
    public void testGetSamlForHost() throws IOException, NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException, ClientException {
        ApiClient api = new ApiClient(CommonsConfigurationUtil.fromResource("/mtwilson-0.5.2.properties"));
        String xmloutput = api.getSamlForHost(new Hostname("10.1.71.149"));
        log.debug(xmloutput);
        TrustAssertion trustAssertion = api.verifyTrustAssertion(xmloutput);
        if( trustAssertion.isValid() ) {
            Set<String> hostnames = trustAssertion.getHosts();
            for(String hostname : hostnames) {
                HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostname);
                log.debug("Valid assertion for {}", hostTrustAssertion.getSubject());
                for(String attr : hostTrustAssertion.getAttributeNames()) {
                    log.debug("Signed attribute {}: {}", new String[] { attr, hostTrustAssertion.getStringAttribute(attr) });
                }
            }
        }
        else {
            log.debug("Invalid assertion", trustAssertion.error());
            
        }
    }
    
    @Test
    public void testResourceExists() throws IOException {
        InputStream in = getClass().getResourceAsStream("/host-149.saml.xml");
        assertNotNull(in);
        String xml = IOUtils.toString(in);
        log.debug("SAML: {}", xml);
    }
    
    @Test
    public void testSamlVerifierSamlForHost() throws IOException, NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException, ParserConfigurationException, SAXException, UnmarshallingException, ClassNotFoundException, InstantiationException, IllegalAccessException, MarshalException, XMLSignatureException, ConfigurationException {
        String xml = IOUtils.toString(getClass().getResourceAsStream("/host-149.saml.xml")); // or get via apiclient like in testGetSamlForHost()
//        Configuration config = My.configuration().getConfiguration(); //ConfigurationUtil.fromResource("/localhost-0.5.2.properties");        
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getKeystoreFile(), My.configuration().getKeystorePassword()); // used to be mtwilson.api.keystore and mtwilson.api.keystore.password properties from configuration
        X509Certificate[] trustedCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
        TrustAssertion trustAssertion = new TrustAssertion(trustedCertificates, xml);
        if( trustAssertion.isValid() ) {
            System.out.println("Assertion is valid");
            log.info("Assertion is valid");
            Set<String> hostnames = trustAssertion.getHosts();
            for(String hostname : hostnames) {
                HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostname);
                log.debug("Subject: {}", hostTrustAssertion.getSubject());
                log.debug("Issuer: {}", hostTrustAssertion.getIssuer());
                Set<String> attributes = hostTrustAssertion.getAttributeNames();
                for(String attribute : attributes) {
                    log.debug("Attribute: {} = {}", new String[] { attribute, hostTrustAssertion.getStringAttribute(attribute) });
                }
            }
        }
        else {
            log.debug("Assertion is NOT valid", trustAssertion.error());
            System.out.println("Assertion is NOT valid");
        }
    }
    
    /**
     * bug #1038 
     * opensaml xml parser is vulnerable to xml external entity injection ... look at the BIOS_Name  value in output, which you can get by commentnig out the two lines in the method marked with "bug #1038" :
     * 
2013-12-03 14:44:14,564 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: Trusted = true
2013-12-03 14:44:14,565 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: Trusted_BIOS = true
2013-12-03 14:44:14,565 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: BIOS_Name = [ProductNames]
ProductName.1033=Microsoft Visual C++ 2008 Redistributable
ProductName.1041=Microsoft Visual C++ 2008 Redistributable
ProductName.1042=Microsoft Visual C++ 2008 Redistributable
ProductName.1028=Microsoft Visual C++ 2008 Redistributable
ProductName.2052=Microsoft Visual C++ 2008 Redistributable
ProductName.1036=Microsoft Visual C++ 2008 Redistributable
ProductName.1040=Microsoft Visual C++ 2008 Redistributable
ProductName.1031=Microsoft Visual C++ 2008 Redistributable
ProductName.3082=Microsoft Visual C++ 2008 Redistributable
2013-12-03 14:44:14,566 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: BIOS_Version = v60
2013-12-03 14:44:14,566 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: BIOS_OEM = EPSD
2013-12-03 14:44:14,566 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: Trusted_VMM = true
2013-12-03 14:44:14,567 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: VMM_Name = Xen
2013-12-03 14:44:14,567 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: VMM_Version = 4.1.0
2013-12-03 14:44:14,567 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: VMM_OSName = SUSE
2013-12-03 14:44:14,568 DEBUG [main] t.i.SamlTest [SamlTest.java:196] Attribute: VMM_OSVersion = 11 P2
     * 
     * 
     * With the bug #1038 fix,  the XXE is prevented and BIOS_Name appears as null:
     * 
2013-12-03 14:54:50,992 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: Trusted = true
2013-12-03 14:54:50,993 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: Trusted_BIOS = true
2013-12-03 14:54:50,993 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: BIOS_Name = null
2013-12-03 14:54:50,993 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: BIOS_Version = v60
2013-12-03 14:54:50,994 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: BIOS_OEM = EPSD
2013-12-03 14:54:50,994 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: Trusted_VMM = true
2013-12-03 14:54:50,995 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: VMM_Name = Xen
2013-12-03 14:54:50,996 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: VMM_Version = 4.1.0
2013-12-03 14:54:50,996 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: VMM_OSName = SUSE
2013-12-03 14:54:50,996 DEBUG [main] t.i.SamlTest [SamlTest.java:230] Attribute: VMM_OSVersion = 11 P2
     * 
     * 
     * @throws Exception
     */
    @Test
    public void testSamlVerifierSamlForHostWithXXE() throws Exception {
        String xmlstr = IOUtils.toString(getClass().getResourceAsStream("/host-149.saml_xxe.xml")); // or get via apiclient like in testGetSamlForHost()
//        Configuration config = My.configuration().getConfiguration(); //ConfigurationUtil.fromResource("/localhost-0.5.2.properties");        
        SimpleKeystore keystore = new SimpleKeystore(My.configuration().getKeystoreFile(), My.configuration().getKeystorePassword()); // used to be mtwilson.api.keystore and mtwilson.api.keystore.password properties from configuration
        X509Certificate[] trustedCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
        TrustAssertion trustAssertion = new TrustAssertion(trustedCertificates, xmlstr);
        if( trustAssertion.isValid() ) {
            Set<String> hostnames = trustAssertion.getHosts();
            for(String hostname : hostnames) {
                HostTrustAssertion hostTrustAssertion = trustAssertion.getTrustAssertion(hostname);
                System.out.println("Assertion is valid");
                log.info("Assertion is valid");
                log.debug("Subject: {}", hostTrustAssertion.getSubject());
                log.debug("Issuer: {}", hostTrustAssertion.getIssuer());
                Set<String> attributes = hostTrustAssertion.getAttributeNames();
                for(String attribute : attributes) {
                    log.debug("Attribute: {} = {}", new String[] { attribute, hostTrustAssertion.getStringAttribute(attribute) });
                }
            }
        }
        else {
            // above code validates signature but for bug #1038 we want to see if the opensaml xml parser is vulnerable to XXE regardless of the signature - so below we copy code from TrustAssertion to parse the xml anyway and retrieve the attribute values.  BIOS_Name is replaced with XXE and you can see sample output in comment above.
            log.debug("Assertion is NOT valid", trustAssertion.error());
            System.out.println("Assertion is NOT valid");
            // simulate here what the TrustAssertion object does -- because it will not make anything available if the signature can't be verified 
                DefaultBootstrap.bootstrap(); // required to load default configs that ship with opensaml that specify how to build and parse the xml (if you don't do this you will get a null unmarshaller when you try to parse xml)
        // simulate readXml
        DocumentBuilderFactory factory1 =  DocumentBuilderFactory.newInstance ();
        factory1.setNamespaceAware (true);
        factory1.setExpandEntityReferences(false); // bug #1038    need to prevent XXE
        factory1.setXIncludeAware(false); // bug #1038
        DocumentBuilder builder = factory1.newDocumentBuilder(); // ParserConfigurationException
        ByteArrayInputStream in = new ByteArrayInputStream(xmlstr.getBytes());
        Element document = builder.parse(in).getDocumentElement (); // SAXException, IOException
        in.close(); // IOExeception
                // simulate readAssertion
                UnmarshallerFactory factory2 = org.opensaml.xml.Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = factory2.getUnmarshaller(document);
        XMLObject xml = unmarshaller.unmarshall(document); // UnmarshallingException
        Assertion samlAssertion = (Assertion) xml;
        for (Statement statement : samlAssertion.getStatements ()) {
            if (statement instanceof AttributeStatement) {
                for (Attribute attribute : 
                        ((AttributeStatement) statement).getAttributes ())
                {
                    String attributeValue = null;
                    // XXX TODO currently this only grabs the last value if there was more than one value in the attribute... full implementation should handle all possibilities but we do provide a getAssertion() function so the client can navigate the assertion tree directly in case they need something not covered here
                    for (XMLObject value : attribute.getAttributeValues ()) {
                        if (value instanceof XSAny) {
                            attributeValue = (((XSAny) value).getTextContent()); // boolean attributes are the text "true" or "false"
                        }
                        if( value instanceof XSString ) {
                            attributeValue = (((XSString) value).getValue()); 
                        }
                    }
                log.debug("Attribute: {} = {}", new String[] { attribute.getName(), attributeValue });
                }
            }
        }
        }
    }    
    
    @Test
    public void testAvailableUnmarshallers() throws ParserConfigurationException, IOException, SAXException, UnmarshallingException, ConfigurationException {
        DefaultBootstrap.bootstrap(); // required to load default configs that ship with opensaml that specify how to build and parse the xml (if you don't do this you will get a null unmarshaller when you try to parse xml)
        String xml = IOUtils.toString(getClass().getResourceAsStream("/host-149.saml.xml")); // or get via apiclient like in testGetSamlForHost()
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        // parse the xml
        DocumentBuilderFactory builderFactory =  DocumentBuilderFactory.newInstance ();
        builderFactory.setNamespaceAware (true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder(); // ParserConfigurationException
        Element document = builder.parse(in).getDocumentElement (); // SAXException, IOException
        in.close(); // IOExeception
        assert document != null;
        log.debug("Reading assertion from element {}", document.getTagName());
        log.debug("Element local name: {}", document.getLocalName());
        log.debug("Element namespace: {}", document.getNamespaceURI());
        UnmarshallerFactory factory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
        Map<QName,Unmarshaller> availableUnmarshallers = factory.getUnmarshallers();
        Set<QName> registeredQNames = availableUnmarshallers.keySet();
        log.debug("There are {} registered QNames", registeredQNames.size());
        for(QName q : availableUnmarshallers.keySet()) {
            log.debug("Unmarshaller for QName {} . {} is null? {}", new String[] { q.getNamespaceURI(), q.toString(),  availableUnmarshallers.get(q) == null ? "NULL":"not null" });
        }
        log.debug("Default unmarshaller provider QName namespace {} prefix {} local name {}", new String[] { org.opensaml.xml.Configuration.getDefaultProviderQName().getNamespaceURI(),  org.opensaml.xml.Configuration.getDefaultProviderQName().getPrefix(),  org.opensaml.xml.Configuration.getDefaultProviderQName().getLocalPart() } );
        Unmarshaller unmarshaller = factory.getUnmarshaller(document);
        assert unmarshaller != null;
        XMLObject xmlObject = unmarshaller.unmarshall(document);
        assert xmlObject != null;
        Assertion samlAssertion = (Assertion) xmlObject; // UnmarshallingException
        log.debug("SAML Subject: {}", samlAssertion.getSubject().getNameID().getValue());
    }

    /**
     * same as the xml function in ApiClient, for testing purposes
     */
    private <T> T xml(String document, Class<T> valueType) throws JAXBException  {
            JAXBContext jc = JAXBContext.newInstance( valueType.getPackage().getName() ); // was just valueType
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
//            Object o = u.unmarshal( new StreamSource( new StringReader( document ) ) );
            JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( new StreamSource( new StringReader( document ) ) );
            return doc.getValue();
    }
    
    @Test
    public void testCreateBulkHostTrustXmlResponse() throws JAXBException  {
        JAXBContext jc = JAXBContext.newInstance( "com.intel.mtwilson.datatypes.xml" );
        com.intel.mtwilson.datatypes.xml.ObjectFactory factory = new com.intel.mtwilson.datatypes.xml.ObjectFactory();
        HostTrustXmlResponseList list = factory.createHostTrustXmlResponseList();
        HostTrustXmlResponse item1 = factory.createHostTrustXmlResponse();
        item1.setName("1.1.1.1");
        item1.setErrorCode("OK");
//        item1.setAssertion("<?xml version=\"1.0\"?><Assertion></Assertion>"); // results in a tag with embedded xml escaped like this: <Assertion>&lt;?xml version=&quot;1.0&quot;?&gt;&lt;Assertion&gt;&lt;/Assertion&gt;</Assertion>
//        item1.setAssertion("<![CDATA[<?xml version=\"1.0\"?><Assertion></Assertion>]]>"); // also results in a tag with embedded xml escaped like this: <Assertion>&lt;![CDATA[&lt;?xml version=&quot;1.0&quot;?&gt;&lt;Assertion&gt;&lt;/Assertion&gt;]]&gt;</Assertion>
        HostTrustXmlResponse item2 = factory.createHostTrustXmlResponse();
        item2.setName("1.1.1.2");
        item2.setErrorCode("VALIDATION_ERROR");
        list.getHost().add(item1);
        list.getHost().add(item2);
        // serialize
        JAXBElement<HostTrustXmlResponseList> listXml = factory.createHosts(list);
        Marshaller m = jc.createMarshaller();
        m.marshal(listXml, System.out);
        // marshall the jaxb ... 
    }
    
    @Test
    public void testBulkHostTrustXmlResponse() throws IOException, JAXBException  {
        String xmlResponse = IOUtils.toString(getClass().getResourceAsStream("/bulk-hosts-saml-155,205.xml")); // or get via apiclient like in testGetSamlForHost()
//        String xmlResponse = IOUtils.toString(getClass().getResourceAsStream("/bulk-hosts-saml-155,205-simple.xml")); // or get via apiclient like in testGetSamlForHost()
        log.debug(xmlResponse);
        /*
        BulkHostTrustXmlResponse bulkHostTrustXmlResponse = xml(xmlResponse, BulkHostTrustXmlResponse.class);
        List<HostTrustXmlResponse> hostTrustXmlResponses = bulkHostTrustXmlResponse.getHostTrustXml();
        for( HostTrustXmlResponse hostTrustXmlResponse : hostTrustXmlResponses ) {
            log.debug("Response for hostname {} status {}", new String[] { hostTrustXmlResponse.getHostname().toString(), hostTrustXmlResponse.getErrorCode().name() });
            log.debug(hostTrustXmlResponse.getAssertionXML());
        }
        * 
        */
        // for some reason the log.debug statements don't always make it to the log... looks like something is not flushing them at the end of the test.  adding a System.out.println() makes it better most of the time... 
        HostTrustXmlResponseList list = xml(xmlResponse, HostTrustXmlResponseList.class);
        for( HostTrustXmlResponse item : list.getHost() ) {
            System.out.println("Host name: "+ item.getName());
            System.out.println("Host error code: "+ item.getErrorCode());
            System.out.println("Host assertion: "+ item.getAssertion());
        }
//        System.out.println();
    }
}
