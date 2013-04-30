/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package test.io;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.ApiException;
import com.intel.mtwilson.ClientException;
import com.intel.mtwilson.TrustAssertion;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponseList;
import com.intel.mtwilson.io.ConfigurationUtil;
import com.intel.mtwilson.model.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

/**
 *
 * @author jbuhacoff
 */
public class SamlTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    public void testLoadSettings() throws IOException {
        Configuration conf = ConfigurationUtil.fromResource("/localhost-0.5.2.properties");
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
        Configuration config = ConfigurationUtil.fromResource("/localhost-0.5.2.properties");
        ApiClient api = new ApiClient(config);
        X509Certificate certificate = api.getSamlCertificate();
        log.debug("SAML Certificate Subject: {}", certificate.getSubjectX500Principal().getName());
        log.debug("SAML Certificate Issuer: {}", certificate.getIssuerX500Principal().getName());
        URL attestationService = new URL(config.getString("mtwilson.api.baseurl"));
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        keystore.addTrustedSamlCertificate(certificate, attestationService.getHost());
        keystore.save();
        log.debug("Saved SAML certificate in keystore");
    }
    
    @Test
    public void testGetSamlForHost() throws IOException, NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException, ClientException {
        ApiClient api = new ApiClient(ConfigurationUtil.fromResource("/mtwilson-0.5.2.properties"));
        String xmloutput = api.getSamlForHost(new Hostname("10.1.71.149"));
        log.debug(xmloutput);
        TrustAssertion trustAssertion = api.verifyTrustAssertion(xmloutput);
        if( trustAssertion.isValid() ) {
            log.debug("Valid assertion for {}", trustAssertion.getSubject());
            for(String attr : trustAssertion.getAttributeNames()) {
                log.debug("Signed attribute {}: {}", new String[] { attr, trustAssertion.getStringAttribute(attr) });
            }
        }
        else {
            log.debug("Invalid assertion", trustAssertion.error());
            
        }
    }
    
    @Test
    public void testSamlVerifierSamlForHost() throws IOException, NoSuchAlgorithmException, KeyManagementException, MalformedURLException, KeyStoreException, CertificateException, UnrecoverableEntryException, ApiException, SignatureException, ParserConfigurationException, SAXException, UnmarshallingException, ClassNotFoundException, InstantiationException, IllegalAccessException, MarshalException, XMLSignatureException, ConfigurationException {
        String xml = IOUtils.toString(getClass().getResourceAsStream("/host-149.saml.xml")); // or get via apiclient like in testGetSamlForHost()
        Configuration config = ConfigurationUtil.fromResource("/localhost-0.5.2.properties");
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        X509Certificate[] trustedCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
        TrustAssertion trustAssertion = new TrustAssertion(trustedCertificates, xml);
        if( trustAssertion.isValid() ) {
            System.out.println("Assertion is valid");
            log.debug("Assertion is valid");
            log.debug("Subject: {}", trustAssertion.getSubject());
            log.debug("Issuer: {}", trustAssertion.getIssuer());
            Set<String> attributes = trustAssertion.getAttributeNames();
            for(String attribute : attributes) {
                log.debug("Attribute: {} = {}", new String[] { attribute, trustAssertion.getStringAttribute(attribute) });
            }
        }
        else {
            log.debug("Assertion is NOT valid", trustAssertion.error());
            System.out.println("Assertion is NOT valid");
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
    private <T> T xml(String document, Class<T> valueType) throws Exception {
            JAXBContext jc = JAXBContext.newInstance( valueType.getPackage().getName() ); // was just valueType
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
//            Object o = u.unmarshal( new StreamSource( new StringReader( document ) ) );
            JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( new StreamSource( new StringReader( document ) ) );
            return doc.getValue();
    }
    
    @Test
    public void testCreateBulkHostTrustXmlResponse() throws Exception {
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
    public void testBulkHostTrustXmlResponse() throws Exception {
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
