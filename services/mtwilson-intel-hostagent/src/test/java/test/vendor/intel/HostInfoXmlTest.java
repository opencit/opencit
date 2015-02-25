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
    
    @Test
    public void testParseEventLog() throws Exception {
        // these two came from the same server
        
        // timestamp 1423269813358
        // <measureLog><txt><mleHash>e9f7b435d865e9ffda7bd97d70737355856c6c16</mleHash><txtStatus>1</txtStatus><osSinitDataCapabilities>00000000</osSinitDataCapabilities><sinitMleData><biosAcmId>800000002013090600001d00ffffffffffffffff</biosAcmId><version>8</version><msegValid>0</msegValid><sinitHash>152705211748b045e74e8cb5dbf4d26a78ac4aa3</sinitHash><stmHash>0000000000000000000000000000000000000000</stmHash><policyControl>00000000</policyControl><mleHash>e9f7b435d865e9ffda7bd97d70737355856c6c16</mleHash><lcpPolicyHash>0000000000000000000000000000000000000000</lcpPolicyHash><processorSCRTMStatus>00000001</processorSCRTMStatus><biosAcmId>800000002013090600001d00ffffffffffffffff</biosAcmId><edxSenterFlags>00000000</edxSenterFlags></sinitMleData><modules><msegValid>0</msegValid><stmHash>0000000000000000000000000000000000000000</stmHash><policyControl>00000000</policyControl><lcpPolicyHash>0000000000000000000000000000000000000000</lcpPolicyHash><processorSCRTMStatus>00000001</processorSCRTMStatus><edxSenterFlags>00000000</edxSenterFlags></sinitMleData><modules><module><pcrNumber>17</pcrNumber><name>tb_policy</name><value>9704353630674bfe21b86b64a7b0f99c297cf902</value></module><module><pcrNumber>17</pcrNumber><name>tb_policy</name><value>9704353630674bfe21b86b64a7b0f99c297cf902</value></module><module><pcrNumber>18</pcrNumber><name>vmlinuz</name><value>1b20af1f5cd112384419045964427ddffbdd5d10</value></module><module><pcrNumber>18</pcrNumber><name>vmlinuz</name><value>1b20af1f5cd112384419045964427ddffbdd5d10</value></module><module><pcrNumber>19</pcrNumber><name>initrd</name><value>eb4ea36447d573a70d30c17254dd22ef82480159</value></module></modules></txt></measureLog>
//        String eventLogBase64 = "PG1lYXN1cmVMb2c+PHR4dD48bWxlSGFzaD5lOWY3YjQzNWQ4NjVlOWZmZGE3YmQ5N2Q3MDczNzM1NTg1NmM2YzE2PC9tbGVIYXNoPjx0eHRTdGF0dXM+MTwvdHh0U3RhdHVzPjxvc1Npbml0RGF0YUNhcGFiaWxpdGllcz4wMDAwMDAwMDwvb3NTaW5pdERhdGFDYXBhYmlsaXRpZXM+PHNpbml0TWxlRGF0YT48Ymlvc0FjbUlkPjgwMDAwMDAwMjAxMzA5MDYwMDAwMWQwMGZmZmZmZmZmZmZmZmZmZmY8L2Jpb3NBY21JZD48dmVyc2lvbj44PC92ZXJzaW9uPjxtc2VnVmFsaWQ+MDwvbXNlZ1ZhbGlkPjxzaW5pdEhhc2g+MTUyNzA1MjExNzQ4YjA0NWU3NGU4Y2I1ZGJmNGQyNmE3OGFjNGFhMzwvc2luaXRIYXNoPjxzdG1IYXNoPjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA8L3N0bUhhc2g+PHBvbGljeUNvbnRyb2w+MDAwMDAwMDA8L3BvbGljeUNvbnRyb2w+PG1sZUhhc2g+ZTlmN2I0MzVkODY1ZTlmZmRhN2JkOTdkNzA3MzczNTU4NTZjNmMxNjwvbWxlSGFzaD48bGNwUG9saWN5SGFzaD4wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwPC9sY3BQb2xpY3lIYXNoPjxwcm9jZXNzb3JTQ1JUTVN0YXR1cz4wMDAwMDAwMTwvcHJvY2Vzc29yU0NSVE1TdGF0dXM+PGJpb3NBY21JZD44MDAwMDAwMDIwMTMwOTA2MDAwMDFkMDBmZmZmZmZmZmZmZmZmZmZmPC9iaW9zQWNtSWQ+PGVkeFNlbnRlckZsYWdzPjAwMDAwMDAwPC9lZHhTZW50ZXJGbGFncz48L3Npbml0TWxlRGF0YT48bW9kdWxlcz48bXNlZ1ZhbGlkPjA8L21zZWdWYWxpZD48c3RtSGFzaD4wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwPC9zdG1IYXNoPjxwb2xpY3lDb250cm9sPjAwMDAwMDAwPC9wb2xpY3lDb250cm9sPjxsY3BQb2xpY3lIYXNoPjAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA8L2xjcFBvbGljeUhhc2g+PHByb2Nlc3NvclNDUlRNU3RhdHVzPjAwMDAwMDAxPC9wcm9jZXNzb3JTQ1JUTVN0YXR1cz48ZWR4U2VudGVyRmxhZ3M+MDAwMDAwMDA8L2VkeFNlbnRlckZsYWdzPjwvc2luaXRNbGVEYXRhPjxtb2R1bGVzPjxtb2R1bGU+PHBjck51bWJlcj4xNzwvcGNyTnVtYmVyPjxuYW1lPnRiX3BvbGljeTwvbmFtZT48dmFsdWU+OTcwNDM1MzYzMDY3NGJmZTIxYjg2YjY0YTdiMGY5OWMyOTdjZjkwMjwvdmFsdWU+PC9tb2R1bGU+PG1vZHVsZT48cGNyTnVtYmVyPjE3PC9wY3JOdW1iZXI+PG5hbWU+dGJfcG9saWN5PC9uYW1lPjx2YWx1ZT45NzA0MzUzNjMwNjc0YmZlMjFiODZiNjRhN2IwZjk5YzI5N2NmOTAyPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT52bWxpbnV6PC9uYW1lPjx2YWx1ZT4xYjIwYWYxZjVjZDExMjM4NDQxOTA0NTk2NDQyN2RkZmZiZGQ1ZDEwPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT52bWxpbnV6PC9uYW1lPjx2YWx1ZT4xYjIwYWYxZjVjZDExMjM4NDQxOTA0NTk2NDQyN2RkZmZiZGQ1ZDEwPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTk8L3Bjck51bWJlcj48bmFtZT5pbml0cmQ8L25hbWU+PHZhbHVlPmViNGVhMzY0NDdkNTczYTcwZDMwYzE3MjU0ZGQyMmVmODI0ODAxNTk8L3ZhbHVlPjwvbW9kdWxlPjwvbW9kdWxlcz48L3R4dD48L21lYXN1cmVMb2c+Cg==";
        
        // timestamp 1423269814034
        // <module><pcrNumber>19</pcrNumber><name>initrd</name><value>eb4ea36447d573a70d30c17254dd22ef82480159</value></module></modules></txt></measureLog>
        String eventLogBase64 = "ICAgICAgPG1vZHVsZT48cGNyTnVtYmVyPjE5PC9wY3JOdW1iZXI+PG5hbWU+aW5pdHJkPC9uYW1lPjx2YWx1ZT5lYjRlYTM2NDQ3ZDU3M2E3MGQzMGMxNzI1NGRkMjJlZjgyNDgwMTU5PC92YWx1ZT48L21vZHVsZT48L21vZHVsZXM+PC90eHQ+PC9tZWFzdXJlTG9nPgo=";

        String eventLog = new String(Base64.decodeBase64(eventLogBase64));
        log.debug("event log: {}", eventLog);
    }
}
