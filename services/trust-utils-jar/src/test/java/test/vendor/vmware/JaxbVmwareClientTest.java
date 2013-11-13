/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import javax.xml.bind.JAXBException;
import org.junit.Test;

/**
 * 9 second delay just to create the login requst (before sending it) 
 * 22 second delay to get the response and parse it!!
 * 
 * 
Running test.vendor.vmware.JaxbVmwareClientTest
22:00:30.373 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.InsecureTlsPolicy
22:00:30.539 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - Login 1...
22:00:39.213 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - Login request: <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns2:Login xmlns:ns2="urn:vim25"><ns2:_this type="SessionManager">SessionManager</ns2:_this><ns2:userName>Administrator</ns2:userName><ns2:password>intel123!</ns2:password></ns2:Login></SOAP-ENV:Body></SOAP-ENV:Envelope>
22:01:02.324 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity Content Length = 662
22:01:02.325 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is chunked? false
22:01:02.325 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is streaming? true
22:01:02.325 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is repeatable? false
22:01:02.325 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - Login response; <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<LoginResponse xmlns="urn:vim25"><returnval><key>523d4bc2-7b66-e19e-7ba9-e1d7ad821a2b</key><userName>Administrator</userName><fullName> </fullName><loginTime>2013-11-05T06:01:02.40269Z</loginTime><lastActiveTime>2013-11-05T06:01:02.40269Z</lastActiveTime><locale>en</locale><messageLocale>en</messageLocale></returnval></LoginResponse>
</soapenv:Body>
</soapenv:Envelope>
22:01:02.325 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - Login 1 done
22:01:02.325 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 1...
22:01:07.396 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - find host request: <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns2:FindByDnsName xmlns:ns2="urn:vim25"><ns2:_this type="SearchIndex">SearchIndex</ns2:_this><ns2:dnsName>10.1.71.173</ns2:dnsName><ns2:vmSearch>false</ns2:vmSearch></ns2:FindByDnsName></SOAP-ENV:Body></SOAP-ENV:Envelope>
22:01:07.469 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity Content Length = 441
22:01:07.470 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is chunked? false
22:01:07.470 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is streaming? true
22:01:07.470 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is repeatable? false
22:01:07.470 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - find host response; <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
22:01:07.470 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 1 done
22:01:07.471 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 2...
22:01:11.916 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - find host request: <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns2:FindByDnsName xmlns:ns2="urn:vim25"><ns2:_this type="SearchIndex">SearchIndex</ns2:_this><ns2:dnsName>10.1.71.173</ns2:dnsName><ns2:vmSearch>false</ns2:vmSearch></ns2:FindByDnsName></SOAP-ENV:Body></SOAP-ENV:Envelope>
22:01:11.979 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity Content Length = 441
22:01:11.979 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is chunked? false
22:01:11.979 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is streaming? true
22:01:11.979 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - HttpEntity is repeatable? false
22:01:11.980 [main] DEBUG test.vendor.vmware.JaxbVmwareClient - find host response; <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
22:01:11.980 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 2 done
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 41.82 sec
 * 
 * @author jbuhacoff
 */
public class JaxbVmwareClientTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxbVmwareClientTest.class);
    
    @Test
    public void testJaxbVmwareHttpClient() throws Exception {
        JaxbVmwareHttpClient client = new JaxbVmwareHttpClient();
        log.debug("Login 1...");
        client.login();
        log.debug("Login 1 done");
        log.debug("find host 1...");
        client.findHost();
        log.debug("find host 1 done");
        log.debug("find host 2...");
        client.findHost();
        log.debug("find host 2 done");
    }
    @Test
    public void testJaxbVmwareUrlClient() throws Exception {
        JaxbVmwareUrlClient client = new JaxbVmwareUrlClient();
        log.debug("Login 1...");
        client.login();
        log.debug("Login 1 done");
        log.debug("find host 1...");
        client.findHost();
        log.debug("find host 1 done");
        log.debug("find host 2...");
        client.findHost();
        log.debug("find host 2 done");
    }

}
