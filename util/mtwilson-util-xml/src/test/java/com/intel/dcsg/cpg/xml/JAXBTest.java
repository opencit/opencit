/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.xml;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import static org.junit.Assert.*;
import test.xml.model.*;
/**
 * REferences: 
 * http://docs.oracle.com/javaee/5/api/javax/xml/bind/Unmarshaller.html#supportedProps
 * 
 * XXX TODO  replace the c:/globdata.ini file with a sample file in this project's test resources package for repeatability on different systems
 * @author jbuhacoff
 */
public class JAXBTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JAXBTest.class);
    
    /**
     * Sample output:
     * 
<?xml version="1.0" encoding="UTF-8" standalone="yes"?><hosts_trust_report><Host Host_Name="host123" Trust_Status="1"><mleInfo>mle-4.3.2</mleInfo></Host></hosts_trust_report>
     * 
     * 
     * @throws JAXBException 
     */
    @Test
    public void testGenerateXml() throws JAXBException {
        HostType host = new HostType();
        host.setHostName("host123");
        host.setMLEInfo("mle-4.3.2");
        host.setTrustStatus(1);
        HostsTrustReportType report = new HostsTrustReportType();
        report.getHost().add(host);
        JAXBContext jc = JAXBContext.newInstance( report.getClass().getPackage().getName() );
        Marshaller m = jc.createMarshaller();
        m.marshal(report, System.out);
    }
    
    /**
     * Sample output:
     * 
 <?xml version="1.0" encoding="UTF-8" standalone="yes"?><hosts_trust_report><Host Host_Name="host123" Trust_Status="1"><mleInfo>mle-4.3.2</mleInfo></Host></hosts_trust_report>
     * 
     * @throws JAXBException 
     */
    @Test
    public void testWriteXml() throws JAXBException {
        HostType host = new HostType();
        host.setHostName("host123");
        host.setMLEInfo("mle-4.3.2");
        host.setTrustStatus(1);
        HostsTrustReportType report = new HostsTrustReportType();
        report.getHost().add(host);
        JAXB jaxb = new JAXB();
        log.debug(jaxb.write(report));
    }
    
    @Test
    public void testParseXml() throws IOException, JAXBException, XMLStreamException {
        String xml = "<hosts_trust_report><Host Host_Name=\"host123\" Trust_Status=\"1\"><mleInfo>mle-4.3.2 &gt;&gt;</mleInfo></Host></hosts_trust_report>";
        HostsTrustReportType report = readInsecure(xml, HostsTrustReportType.class);
        assertEquals("host123", report.getHost().get(0).getHostName());
        assertEquals("mle-4.3.2 >>", report.getHost().get(0).getMLEInfo());
    }

    @Test
    public void testParseXmlWithHeader() throws IOException, JAXBException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><hosts_trust_report><Host Host_Name=\"host123\" Trust_Status=\"1\"><mleInfo>mle-4.3.2 &gt;&gt;</mleInfo></Host></hosts_trust_report>";
        HostsTrustReportType report = readInsecure(xml, HostsTrustReportType.class);
        assertEquals("host123", report.getHost().get(0).getHostName());
        assertEquals("mle-4.3.2 >>", report.getHost().get(0).getMLEInfo());
    }
    
    
    /**
     * Sample output:
     * 
2013-12-03 12:32:19,225 DEBUG [main] c.i.d.c.x.JAXBTest [JAXBTest.java:61] INJECTED MLE INFO: [ProductNames]
ProductName.1033=Microsoft Visual C++ 2008 Redistributable
ProductName.1041=Microsoft Visual C++ 2008 Redistributable
ProductName.1042=Microsoft Visual C++ 2008 Redistributable
ProductName.1028=Microsoft Visual C++ 2008 Redistributable
ProductName.2052=Microsoft Visual C++ 2008 Redistributable
ProductName.1036=Microsoft Visual C++ 2008 Redistributable
ProductName.1040=Microsoft Visual C++ 2008 Redistributable
ProductName.1031=Microsoft Visual C++ 2008 Redistributable
ProductName.3082=Microsoft Visual C++ 2008 Redistributable

     * 
     * @throws IOException
     * @throws JAXBException 
     */
    @Test
    public void testXmlEntityInjection() throws IOException, JAXBException, XMLStreamException {
        // doctype xml doesn't work
        String xml = "<!DOCTYPE foo [<!ENTITY inc SYSTEM \"file:///c:/globdata.ini\">]>\n" +
                     "<hosts_trust_report><Host Host_Name=\"host123\" Trust_Status=\"1\"><mleInfo>&inc;</mleInfo></Host></hosts_trust_report>";
        HostsTrustReportType report = readInsecure(xml, HostsTrustReportType.class);
        log.debug("INJECTED MLE INFO: {}", report.getHost().get(0).getMLEInfo());
        assertEquals("host123", report.getHost().get(0).getHostName());
        assertNotEquals("mle-4.3.2", report.getHost().get(0).getMLEInfo());
        assertTrue(report.getHost().get(0).getMLEInfo().startsWith("[ProductNames]")); // first word in the external entity's content
    }

    /**
     * the DTD will eb processed but the entity injection will fail and the MLE_INFO value will be empty string
     * @throws IOException
     * @throws JAXBException 
     */
//    @Test(expected=javax.xml.bind.UnmarshalException.class)
    @Test
    public void testPreventXmlEntityInjectionAllowDtd() throws IOException, JAXBException, XMLStreamException {
        String xml = "<!DOCTYPE foo [<!ENTITY inc SYSTEM \"file:///c:/globdata.ini\">]>\n" +
                     "<hosts_trust_report><Host Host_Name=\"host123\" Trust_Status=\"1\"><mleInfo>&inc;</mleInfo></Host></hosts_trust_report>";
        HostsTrustReportType report = readSecureWithDtd(xml, HostsTrustReportType.class);
        assertEquals("host123", report.getHost().get(0).getHostName());
        assertEquals("", report.getHost().get(0).getMLEInfo());
    }

    /**
     * The DTD will cause an error whiel parsing because DTD support is disabled
     * @throws IOException
     * @throws JAXBException
     * @throws XMLStreamException 
     */
    @Test(expected=javax.xml.bind.UnmarshalException.class)
    public void testPreventXmlEntityInjection() throws IOException, JAXBException, XMLStreamException {
        String xml = "<!DOCTYPE foo [<!ENTITY inc SYSTEM \"file:///c:/globdata.ini\">]>\n" +
                     "<hosts_trust_report><Host Host_Name=\"host123\" Trust_Status=\"1\"><mleInfo>&inc;</mleInfo></Host></hosts_trust_report>";
        HostsTrustReportType report = readSecure(xml, HostsTrustReportType.class);
        assertEquals("host123", report.getHost().get(0).getHostName());
        assertEquals("", report.getHost().get(0).getMLEInfo()); // shouldn't get here, exception will be thrown while unmarshalling
    }
    
    public <T> T readSecureWithDtd(String document, Class<T> valueType) throws IOException, JAXBException, XMLStreamException {
        JAXBContext jc = JAXBContext.newInstance( valueType.getPackage().getName() );
        XMLInputFactory xif = XMLInputFactory.newFactory();        
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource( new StringReader( document ) ));
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( xsr );
        return doc.getValue();
    }
    
    public <T> T readSecure(String document, Class<T> valueType) throws IOException, JAXBException, XMLStreamException {
        JAXBContext jc = JAXBContext.newInstance( valueType.getPackage().getName() );
        XMLInputFactory xif = XMLInputFactory.newFactory();        
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource( new StringReader( document ) ));
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( xsr );
        return doc.getValue();
    }
    
    public <T> T readInsecureWithXmlInputFactory(String document, Class<T> valueType) throws IOException, JAXBException, XMLStreamException {
        JAXBContext jc = JAXBContext.newInstance( valueType.getPackage().getName() );
        Unmarshaller u = jc.createUnmarshaller();
        XMLInputFactory xif = XMLInputFactory.newFactory();        
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource( new StringReader( document ) ));
        JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( xsr );
        return doc.getValue();
    }    

    public <T> T readInsecure(String document, Class<T> valueType) throws IOException, JAXBException, XMLStreamException {
        JAXBContext jc = JAXBContext.newInstance( valueType.getPackage().getName() );
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( new StreamSource( new StringReader( document ) ) );
        return doc.getValue();
    }    
    
}
