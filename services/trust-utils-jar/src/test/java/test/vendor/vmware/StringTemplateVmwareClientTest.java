/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Login & two requests completed in 1.7 seconds
 * 
Running test.vendor.vmware.StringTemplateVmwareClientTest
22:02:15.990 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy
22:02:16.086 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - Login 1...
22:02:16.086 [main] DEBUG t.v.v.StringTemplateVmwareClient - Login request: <?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><Login xmlns="urn:vim25"><_this type="SessionManager">SessionManager</_this><userName>Administrator</userName><password>intel123!</password></Login></S:Body></S:Envelope>
22:02:16.086 [main] DEBUG t.v.v.StringTemplateVmwareClient - preparing post request...
22:02:16.483 [main] DEBUG c.i.m.t.TrustFirstCertificateTlsPolicy - TrustFirstCertificatePolicy with 0 trusted certificates
22:02:16.483 [main] DEBUG c.i.m.t.TrustFirstCertificateTlsPolicy - server certificate: CN=10.1.71.162,O=\00D\00O\00_\00N\00O\00T\00_\00T\00R\00U\00S\00T,OU=Created by http://www.fiddler2.com
22:02:16.484 [main] DEBUG c.i.m.t.TrustFirstCertificateTlsPolicy - Saving certificate CN=10.1.71.162,O=\00D\00O\00_\00N\00O\00T\00_\00T\00R\00U\00S\00T,OU=Created by http://www.fiddler2.com
22:02:16.484 [main] INFO  c.i.m.t.KeystoreCertificateRepository - Saving keystore
22:02:16.508 [main] DEBUG t.v.v.StringTemplateVmwareClient - sending post request...
22:02:17.087 [main] DEBUG t.v.v.StringTemplateVmwareClient - Got cookie: vmware_soap_session="526d2d47-4722-db5a-309d-2920eb415a6a"; Path=/; HttpOnly; = {}
22:02:17.088 [main] DEBUG t.v.v.StringTemplateVmwareClient - got response, reading...
22:02:17.108 [main] DEBUG t.v.v.StringTemplateVmwareClient - releasing connection...
22:02:17.109 [main] DEBUG t.v.v.StringTemplateVmwareClient - Login response: <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<LoginResponse xmlns="urn:vim25"><returnval><key>527fb0d4-7ce7-1482-3785-a3ac05b83b80</key><userName>Administrator</userName><fullName> </fullName><loginTime>2013-11-05T06:02:17.080476Z</loginTime><lastActiveTime>2013-11-05T06:02:17.080476Z</lastActiveTime><locale>en</locale><messageLocale>en</messageLocale></returnval></LoginResponse>
</soapenv:Body>
</soapenv:Envelope>
22:02:17.109 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - Login 1 done
22:02:17.109 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 1...
22:02:17.109 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host request: <?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><FindByDnsName xmlns="urn:vim25"><_this type="SearchIndex">SearchIndex</_this><dnsName>10.1.71.173</dnsName><vmSearch>false</vmSearch></FindByDnsName></S:Body></S:Envelope>
22:02:17.109 [main] DEBUG t.v.v.StringTemplateVmwareClient - preparing post request...
22:02:17.110 [main] DEBUG t.v.v.StringTemplateVmwareClient - sending post request...
22:02:17.288 [main] DEBUG t.v.v.StringTemplateVmwareClient - got response, reading...
22:02:17.289 [main] DEBUG t.v.v.StringTemplateVmwareClient - releasing connection...
22:02:17.290 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host response: <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
22:02:17.290 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 1 done
22:02:17.290 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 2...
22:02:17.290 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host request: <?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><FindByDnsName xmlns="urn:vim25"><_this type="SearchIndex">SearchIndex</_this><dnsName>10.1.71.173</dnsName><vmSearch>false</vmSearch></FindByDnsName></S:Body></S:Envelope>
22:02:17.290 [main] DEBUG t.v.v.StringTemplateVmwareClient - preparing post request...
22:02:17.291 [main] DEBUG t.v.v.StringTemplateVmwareClient - sending post request...
22:02:17.492 [main] DEBUG t.v.v.StringTemplateVmwareClient - got response, reading...
22:02:17.493 [main] DEBUG t.v.v.StringTemplateVmwareClient - releasing connection...
22:02:17.493 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host response: <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
22:02:17.493 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 3 done
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.748 sec

 * 
 * @author jbuhacoff
 */
public class StringTemplateVmwareClientTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringTemplateVmwareClientTest.class);
    
    @BeforeClass
    public static void setupProxy() {
        // these settings force the jvm to use the local fiddler proxy so we can see the traffic...
  System.setProperty("http.proxyHost", "127.0.0.1");
    System.setProperty("https.proxyHost", "127.0.0.1");
    System.setProperty("http.proxyPort", "8888");
    System.setProperty("https.proxyPort", "8888");
    System.setProperty("com.sun.management.jmxremote","true"); // to inform jconsole we want to be monitored
        
    }
    
    /**
     * Still have a 12 second delay even when we already have the http request ready to post!
     * 
20:31:29.368 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.InsecureTlsPolicy
20:31:29.565 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - Login 1...
20:31:29.565 [main] DEBUG t.v.v.StringTemplateVmwareClient - Login request: <?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><Login xmlns="urn:vim25"><_this type="SessionManager">SessionManager</_this><userName>Administrator</userName><password>intel123!</password></Login></S:Body></S:Envelope>
20:31:29.565 [main] DEBUG t.v.v.StringTemplateVmwareClient - preparing post request...
20:31:29.582 [main] DEBUG t.v.v.StringTemplateVmwareClient - sending post request...
20:31:37.649 [main] DEBUG t.v.v.StringTemplateVmwareClient - Got cookie: Set-Cookie = vmware_soap_session="52fafa78-08e7-4ed8-6126-0011fb6eef1f"; Path=/; HttpOnly;
20:31:37.650 [main] DEBUG t.v.v.StringTemplateVmwareClient - got response, reading...
20:31:37.669 [main] DEBUG t.v.v.StringTemplateVmwareClient - releasing connection...
20:31:37.670 [main] DEBUG t.v.v.StringTemplateVmwareClient - Login response: <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<LoginResponse xmlns="urn:vim25"><returnval><key>52dc613a-84c5-899c-9fc5-e72fe77eae70</key><userName>Administrator</userName><fullName> </fullName><loginTime>2013-11-05T04:31:37.745892Z</loginTime><lastActiveTime>2013-11-05T04:31:37.745892Z</lastActiveTime><locale>en</locale><messageLocale>en</messageLocale></returnval></LoginResponse>
</soapenv:Body>
</soapenv:Envelope>
20:31:37.670 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - Login 1 done
20:31:37.671 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 1...
20:31:37.671 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host request: <?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><FindByDnsName xmlns="urn:vim25"><_this type="SearchIndex">SearchIndex</_this><dnsName>10.1.71.173</dnsName><vmSearch>false</vmSearch></FindByDnsName></S:Body></S:Envelope>
20:31:37.672 [main] DEBUG t.v.v.StringTemplateVmwareClient - preparing post request...
20:31:37.673 [main] DEBUG t.v.v.StringTemplateVmwareClient - sending post request...
20:31:37.729 [main] DEBUG t.v.v.StringTemplateVmwareClient - got response, reading...
20:31:37.730 [main] DEBUG t.v.v.StringTemplateVmwareClient - releasing connection...
20:31:37.730 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host response: <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
20:31:37.730 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 1 done
20:31:37.731 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 2...
20:31:37.731 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host request: <?xml version="1.0" ?><S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/"><S:Body><FindByDnsName xmlns="urn:vim25"><_this type="SearchIndex">SearchIndex</_this><dnsName>10.1.71.173</dnsName><vmSearch>false</vmSearch></FindByDnsName></S:Body></S:Envelope>
20:31:37.731 [main] DEBUG t.v.v.StringTemplateVmwareClient - preparing post request...
20:31:37.731 [main] DEBUG t.v.v.StringTemplateVmwareClient - sending post request...
20:31:37.785 [main] DEBUG t.v.v.StringTemplateVmwareClient - got response, reading...
20:31:37.785 [main] DEBUG t.v.v.StringTemplateVmwareClient - releasing connection...
20:31:37.786 [main] DEBUG t.v.v.StringTemplateVmwareClient - find host response: <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
20:31:37.786 [main] DEBUG t.v.v.StringTemplateVmwareClientTest - find host 3 done
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.674 sec
* 
     * @throws Exception 
     */
    @Test
    public void testStringTemplateVmwareClient() throws Exception {
        StringTemplateVmwareClient client = new StringTemplateVmwareClient();
        log.debug("Login 1...");
        client.login();
        log.debug("Login 1 done");
        log.debug("find host 1...");
        client.findHost();
        log.debug("find host 1 done");
        log.debug("find host 2...");
        client.findHost();
        log.debug("find host 3 done");
    }
}
