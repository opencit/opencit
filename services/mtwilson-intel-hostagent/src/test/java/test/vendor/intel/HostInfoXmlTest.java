/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.intel;

import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.codec.binary.Base64;
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
    public void testParseEventLog2() throws Exception {
        String eventLogBase64 = "PG1lYXN1cmVMb2c+PHR4dD48dHh0U3RhdHVzPjE8L3R4dFN0YXR1cz48b3NTaW5pdERhdGFDYXBhYmlsaXRpZXM+MDAwMDAwMDA8L29zU2luaXREYXRhQ2FwYWJpbGl0aWVzPjxzaW5pdE1sZURhdGE+PHZlcnNpb24+ODwvdmVyc2lvbj48c2luaXRIYXNoPjE1MjcwNTIxMTc0OGIwNDVlNzRlOGNiNWRiZjRkMjZhNzhhYzRhYTM8L3Npbml0SGFzaD48bWxlSGFzaD5lOWY3YjQzNWQ4NjVlOWZmZGE3YmQ5N2Q3MDczNzM1NTg1NmM2YzE2PC9tbGVIYXNoPjxiaW9zQWNtSWQ+ODAwMDAwMDAyMDEzMDkwNjAwMDAxZDAwZmZmZmZmZmZmZmZmZmZmZjwvYmlvc0FjbUlkPjxtc2VnVmFsaWQ+MDwvbXNlZ1ZhbGlkPjxzdG1IYXNoPjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA8L3N0bUhhc2g+PHBvbGljeUNvbnRyb2w+MDAwMDAwMDA8L3BvbGljeUNvbnRyb2w+PGxjcFBvbGljeUhhc2g+MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDwvbGNwUG9saWN5SGFzaD48cHJvY2Vzc29yU0NSVE1TdGF0dXM+MDAwMDAwMDE8L3Byb2Nlc3NvclNDUlRNU3RhdHVzPjxlZHhTZW50ZXJGbGFncz4wMDAwMDAwMDwvZWR4U2VudGVyRmxhZ3M+PC9zaW5pdE1sZURhdGE+PG1vZHVsZXM+PG1vZHVsZT48cGNyTnVtYmVyPjE3PC9wY3JOdW1iZXI+PG5hbWU+dGJfcG9saWN5PC9uYW1lPjx2YWx1ZT45NzA0MzUzNjMwNjc0YmZlMjFiODZiNjRhN2IwZjk5YzI5N2NmOTAyPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT52bWxpbnV6PC9uYW1lPjx2YWx1ZT4xYjIwYWYxZjVjZDExMjM4NDQxOTA0NTk2NDQyN2RkZmZiZGQ1ZDEwPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTk8L3Bjck51bWJlcj48bmFtZT5pbml0cmQ8L25hbWU+PHZhbHVlPmViNGVhMzY0NDdkNTczYTcwZDMwYzE3MjU0ZGQyMmVmODI0ODAxNTk8L3ZhbHVlPjwvbW9kdWxlPjwvbW9kdWxlcz48L3R4dD48L21lYXN1cmVMb2c+Cg==";
        String eventLog = new String(Base64.decodeBase64(eventLogBase64));
        log.debug("event log: {}", eventLog);
    }
    
    
    @Test
    public void testReadXml() throws JAXBException, IOException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><host_info><clientIp>client-ip</clientIp><errorCode>0</errorCode><osName>os-name</osName><osVersion>os-version</osVersion><biosOem>bios-oem</biosOem><biosVersion>bios-version</biosVersion><vmmName>vmm-name</vmmName><vmmVersion>vmm-version</vmmVersion><processorInfo>processor-info</processorInfo></host_info>";
        JAXB jaxb = new JAXB();
        HostInfo bean = jaxb.read(xml, HostInfo.class);
        log.debug("bios oem: {}", bean.getBiosOem());
    }
}
