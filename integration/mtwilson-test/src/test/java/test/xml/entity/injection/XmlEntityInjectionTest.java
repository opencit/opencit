/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.xml.entity.injection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mountwilson.as.hosttrustreport.data.HostType;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import com.thoughtworks.xstream.XStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author jbuhacoff
 */
public class XmlEntityInjectionTest extends RemoteIntegrationTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlEntityInjectionTest.class);

    @Test
    public void testXmlEntityInjectionXstream() throws IOException, JAXBException, XMLStreamException {
        ObjectMapper mapper = new ObjectMapper();
        String xml = "<!DOCTYPE foo [<!ENTITY inc SYSTEM \"file:///c:/globdata.ini\">]>\n"
                + "<hosts_trust_report>&inc;</hosts_trust_report>";
//        xml = "<hosts_trust_report><Host Host_Name=\"host123\" Trust_Status=\"1\"><mleInfo>&inc;</mleInfo></Host></hosts_trust_report>";
//        xml = "<hosts_trust_report></hosts_trust_report>";
        XStream xs = new XStream();
        xs.alias("hosts_trust_report", HostsTrustReportType.class);
        Object xml_xstream = xs.fromXML(xml);
        log.debug("output pojo: {}", xml_xstream.getClass().getName());
        log.debug("Input object: {}", mapper.writeValueAsString(xml_xstream));
    }

    @XmlRootElement(name = "hosts_trust_report") // added to support deserialization -jabuhacx 20120614
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "hosts_trust_reportType", propOrder = {
        "host"
    })
    public class HostsTrustReportType {

        @XmlElement(name = "Host", required = true)
        protected String host;

        public String getHost() {
            if (host == null) {
                host = new String();
            }
            return this.host;
        }

    }
}
