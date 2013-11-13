/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.dcsg.cpg.performance.AlarmClock;
import com.vmware.vim25.ManagedObjectReference;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class OtherVmwareClientTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OtherVmwareClientTest.class);

    /**
     * Output:
Running test.vendor.vmware.OtherVmwareClientTest
14:38:04.086 [main] DEBUG t.v.vmware.OtherVmwareClientTest - Logging in...
14:38:04.099 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.InsecureTlsPolicy
14:38:04.196 [main] DEBUG test.vendor.vmware.OtherVmwareClient - new VimService(...)
14:38:04.799 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getVimPort()
14:38:13.931 [main] DEBUG test.vendor.vmware.OtherVmwareClient - retrieveServiceContent(serviceInstance)
14:38:14.948 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getSessionManager()
14:38:14.948 [main] DEBUG test.vendor.vmware.OtherVmwareClient - login(...)
14:38:21.739 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS)
14:38:21.740 [main] DEBUG test.vendor.vmware.OtherVmwareClient - get cookie
14:38:21.740 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getRequestContext()..put(MessageContext.HTTP_REQUEST_HEADERS, map)
14:38:21.740 [main] DEBUG test.vendor.vmware.OtherVmwareClient - queryOptions
14:38:21.755 [main] DEBUG test.vendor.vmware.OtherVmwareClient - should use port 443
14:38:21.756 [main] DEBUG test.vendor.vmware.OtherVmwareClient - Connected
14:38:21.768 [main] DEBUG t.v.vmware.OtherVmwareClientTest - Success! esxi ref type HostSystem
     * 
     * Or if I use a wrong port number (8089 instead of 443):
14:39:24.773 [main] DEBUG t.v.vmware.OtherVmwareClientTest - Logging in...
14:39:24.790 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.InsecureTlsPolicy
14:39:24.886 [main] DEBUG test.vendor.vmware.OtherVmwareClient - new VimService(...)
14:39:25.467 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getVimPort()
14:39:33.665 [main] DEBUG test.vendor.vmware.OtherVmwareClient - retrieveServiceContent(serviceInstance)
and THEN the exception.... it's already been 8 seconds before it even tries to connect to the vcenter??
     * 
     * When trying to connect multiple times,  first delay is 10 seconds and subsequent delays are 5 seconds (between getVimPort and the next action)
15:57:28.091 [main] DEBUG t.v.vmware.OtherVmwareClientTest - Logging in 3...
15:57:28.091 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.InsecureTlsPolicy
15:57:28.091 [main] DEBUG test.vendor.vmware.OtherVmwareClient - new VimService(...)
15:57:28.214 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getVimPort()
15:57:33.515 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getRequestContext()
15:57:33.515 [main] DEBUG test.vendor.vmware.OtherVmwareClient - retrieveServiceContent(serviceInstance)
15:57:34.041 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getSessionManager()
15:57:34.042 [main] DEBUG test.vendor.vmware.OtherVmwareClient - login(...)
15:57:34.136 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS)
15:57:34.136 [main] DEBUG test.vendor.vmware.OtherVmwareClient - get cookie
15:57:34.137 [main] DEBUG test.vendor.vmware.OtherVmwareClient - getRequestContext()..put(MessageContext.HTTP_REQUEST_HEADERS, map)
15:57:34.137 [main] DEBUG test.vendor.vmware.OtherVmwareClient - queryOptions
15:57:34.156 [main] DEBUG test.vendor.vmware.OtherVmwareClient - should use port 443
15:57:34.157 [main] DEBUG test.vendor.vmware.OtherVmwareClient - Connected
15:57:34.175 [main] DEBUG t.v.vmware.OtherVmwareClientTest - Success! esxi ref type HostSystem

* 
     * @throws Exception 
     */
    @Test
    public void testAlternateConnectionCode() throws Exception {
        // these settings force the jvm to use the local fiddler proxy so we can see the traffic...
  System.setProperty("http.proxyHost", "127.0.0.1");
    System.setProperty("https.proxyHost", "127.0.0.1");
    System.setProperty("http.proxyPort", "8888");
    System.setProperty("https.proxyPort", "8888");
    System.setProperty("com.sun.management.jmxremote","true"); // to inform jconsole we want to be monitored
//    AlarmClock clock = new AlarmClock();
//    clock.sleep(30, TimeUnit.SECONDS);
    
        OtherVmwareClient client = new OtherVmwareClient();
        client.setHost("10.1.71.162"); // vcenter host
        client.setUsername("Administrator");
        client.setPassword("intel123!");
        log.debug("Logging in 1..."); // 10 second delay between getVimPort() and getRequestContext()
        client.login();
        ManagedObjectReference esxi = client.getHostReference("10.1.71.173"); // esxi hostname
        log.debug("Success! esxi ref type {}", esxi.getType());
        
        
        // ok, now run it again !! and see if there is still a 10 secondd elay seince we already warmed up:
        log.debug("Logging in 2..."); // 5 second delay this time between getVimPort() and getRequestContext()
        client.login();
        esxi = client.getHostReference("10.1.71.173"); // esxi hostname
        log.debug("Success! esxi ref type {}", esxi.getType());
        
        // ok, now run it again !! and see if there is still a 10 secondd elay seince we already warmed up:
        log.debug("Logging in 3..."); // 5 second delay again between getVimPort() and getRequestContext()
        client.login();
        esxi = client.getHostReference("10.1.71.173"); // esxi hostname
        log.debug("Success! esxi ref type {}", esxi.getType());
        
    }
}
