/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.agent;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class HostInfoXmlTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostInfoXmlTest.class);

    /**
     * sample output:
     *  <?xml version="1.0" encoding="UTF-8" standalone="yes"?><host_info><clientIp>client-ip</clientIp><errorCode>0</errorCode><osName>os-name</osName><osVersion>os-version</osVersion><biosOem>bios-oem</biosOem><biosVersion>bios-version</biosVersion><vmmName>vmm-name</vmmName><vmmVersion>vmm-version</vmmVersion><processorInfo>processor-info</processorInfo></host_info>
     * @throws JAXBException 
     */
    @Test
    public void testWriteXml() throws JAXBException {
        HostInfo bean = new HostInfo();
        bean.setBiosOem("bios-oem");;
        bean.setBiosVersion("bios-version");
        bean.setClientIp("client-ip");
        bean.setOsName("os-name");;
        bean.setOsVersion("os-version");
        bean.setProcessorInfo("processor-info");
        bean.setVmmName("vmm-name");
        bean.setVmmVersion("vmm-version");
        JAXB jaxb = new JAXB();
        String xml = jaxb.write(bean);
        log.debug("xml: {}", xml);
    }
    
    @Test
    public void testReadXml() throws JAXBException, IOException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><host_info><clientIp>client-ip</clientIp><errorCode>0</errorCode><osName>os-name</osName><osVersion>os-version</osVersion><biosOem>bios-oem</biosOem><biosVersion>bios-version</biosVersion><vmmName>vmm-name</vmmName><vmmVersion>vmm-version</vmmVersion><processorInfo>processor-info</processorInfo></host_info>";
        JAXB jaxb = new JAXB();
        HostInfo bean = jaxb.read(xml, HostInfo.class);
        log.debug("bios oem: {}", bean.getBiosOem());
    }
}
