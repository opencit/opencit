/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;


import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TlsPolicyManager;
import com.vmware.vim25.*;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class OtherVmwareClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OtherVmwareClient.class);

    private String username;
    private String password;
    private String host;
    private int port;
    private String cookie;
   private static VimPortType globalVimPort;
   private VimPortType vimPort;
   private ServiceContent serviceContent;
       private final ManagedObjectReference serviceInstance = new ManagedObjectReference();
    private TlsPolicy tlsPolicy;
    
    static {
         URL wsdlURL;
         /*
         // This is the base url which either will be the jar file or the bin
         // directory
         URL baseURL = VimService.class.getProtectionDomain()
                                       .getCodeSource()
                                       .getLocation();

         if (baseURL.toString().endsWith(".jar")) {
            String jarPath = "jar:"
                  + VimService.class.getProtectionDomain().getCodeSource().getLocation()
                  //+ "!/wsdl/vim25/vimService.wsdl";
                    + "!/com/vmware/vim25/vimService.wsdl";
            wsdlURL = new URL(jarPath);
         } else {
            wsdlURL = new URL(baseURL,  "..\\wsdl\\vim25\\vimService.wsdl");
         }*/
         /*
         try {
            wsdlURL = new URL("file:///C:/Users/jbuhacof/workspace/tmp/vmware/vimService.wsdl");

         log.debug("new VimService(...)");
         VimService locator = new VimService(wsdlURL, new QName("urn:vim25Service",
                                                                "VimService"));
         log.debug("getVimPort()");
//         vimPort = locator.getPort(VimPortType.class); // same delay as getVimPort... these are equivalent
         globalVimPort = locator.getVimPort();
         log.debug("vimPort ok");
         }
         catch(Exception e) {
             log.error("Cannot initialize vmware client", e);
         }
         * */
    }
    
   public OtherVmwareClient() {
      serviceInstance.setType("ServiceInstance");
      serviceInstance.setValue("ServiceInstance");
   }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
   
   
   
   public String getCookie() { return cookie; }
   
   public void login() throws MalformedURLException, RuntimeFaultFaultMsg, InvalidLocaleFaultMsg, InvalidLoginFaultMsg, InvalidNameFaultMsg, NoSuchAlgorithmException, KeyManagementException {
//       if( globalVimPort == null ) { throw new IllegalStateException("Cannot login to vcenter without valid vimport"); }
       
       tlsPolicy = new InsecureTlsPolicy(); // would be replaced by what we currently do for getting the policy
        TlsPolicyManager.getInstance().setTlsPolicy(host, tlsPolicy);
               javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, new javax.net.ssl.TrustManager[]{tlsPolicy.getTrustManager()}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(tlsPolicy.getHostnameVerifier());

        log.debug("Getting a new vimport proxy");
//        ((BindingProvider)globalVimPort).
//         vimPort = ((BindingProvider)globalVimPort).getEndpointReference().getPort(VimPortType.class); // still 5 second delay on subsequent requests...

        URL     wsdlURL = new URL("file:///C:/Users/jbuhacof/workspace/tmp/vmware/vimService.wsdl");
     vimPort = Service.create(wsdlURL, new QName("urn:vim25Service", "VimService")).getPort(VimPortType.class); 
        
         log.debug("getRequestContext()");
         ((BindingProvider) vimPort).getRequestContext()
                                  .put(BindingProvider.SESSION_MAINTAIN_PROPERTY,
                                       Boolean.TRUE); 
         ((BindingProvider) vimPort).getRequestContext()
                                  .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                       /*"https://sdkTunnel:8089/sdk"*/ 
                                  "https://"+host+":443/sdk" );
         
         log.debug("retrieveServiceContent(serviceInstance)");
         
         serviceContent = vimPort.retrieveServiceContent(serviceInstance);
         log.debug("getSessionManager()");
         ManagedObjectReference sessionManager = serviceContent.getSessionManager();
         log.debug("login(...)");
         vimPort.login(sessionManager, username, password, null);
         log.debug("getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS)");
         Map<String, List<String>> headers = (Map<String, List<String>>) ((BindingProvider) vimPort).getResponseContext()
                                                                                                  .get(MessageContext.HTTP_RESPONSE_HEADERS);
         Map<String, List<String>> map = new HashMap<String, List<String>>();
         log.debug("get cookie");
         List<String> cookies = headers.get("Set-cookie");
         if( cookies != null ) {
             if( !cookies.isEmpty() ) {
                 cookie = cookies.get(0); // get the value of the cookie
                 map.put("Cookie", Collections.singletonList(cookie));
             }
         }
         log.debug("getRequestContext()..put(MessageContext.HTTP_REQUEST_HEADERS, map)");
         ((BindingProvider) vimPort).getRequestContext()
                                  .put(MessageContext.HTTP_REQUEST_HEADERS, map);
         log.debug("queryOptions");
         List<OptionValue> queryOptions = vimPort.queryOptions(serviceContent.getSetting(),  "WebService.Ports.https");
         if (queryOptions.size() == 0 || queryOptions.get(0) == null
               || queryOptions.get(0).getValue() == null) {
            log.debug("should use port 443"); //_httpsPort = 443;
         } else {
            log.debug("should use port {}", (Integer) queryOptions.get(0).getValue() ); //_httpsPort = (Integer) queryOptions.get(0).getValue();
         }
         
         log.debug("Connected");
   }
   
   
    public ManagedObjectReference getHostReference(String hostname) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference hostRef = null;

        //ManagedObjectReference searchIndex;
        //vimPort.findByDnsName and vimPort.findByIp ....  first parameter is the searchindex mor, second is datacenter (optional, can be null), , third is the dnsname/ip,  fourth is true for vm or false for host.
        // page 20: obtain manageed obejct reference by accessor method, for searchindex
//        ServiceContent sc = vimPort.retrieveServiceContent(hostRef)
        ManagedObjectReference searchIndex = serviceContent.getSearchIndex();
        hostRef = vimPort.findByDnsName(searchIndex, null, hostname, false);
        if (hostRef == null) {
            hostRef = vimPort.findByIp(searchIndex, null, hostname, false);
        }
        return hostRef;

    }
   
 }
