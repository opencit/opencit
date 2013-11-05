/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ApiResponse;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.KeystoreCertificateRepository;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TlsPolicyManager;
import com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy;
import com.vmware.vim25.FindByDnsNameRequestType;
import com.vmware.vim25.LoginRequestType;
import com.vmware.vim25.ManagedObjectReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SignatureException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;

/**
 * This implementation replaces the apache http client with java's built-in  urlconnection.  login and two requests atkes 22 seconds:
 * 

Running test.vendor.vmware.JaxbVmwareClientTest
22:07:07.273 [main] DEBUG c.i.mtwilson.tls.TlsPolicyManager - TlsPolicyManager: adding 10.1.71.162 with policy: class com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy
22:07:07.377 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - Login 1...
22:07:18.727 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - Login request: <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns2:Login xmlns:ns2="urn:vim25"><ns2:_this type="SessionManager">SessionManager</ns2:_this><ns2:userName>Administrator</ns2:userName><ns2:password>intel123!</ns2:password></ns2:Login></SOAP-ENV:Body></SOAP-ENV:Envelope>
22:07:18.728 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - preparing post request...
22:07:19.215 [main] DEBUG c.i.m.t.TrustFirstCertificateTlsPolicy - TrustFirstCertificatePolicy with 0 trusted certificates
22:07:19.216 [main] DEBUG c.i.m.t.TrustFirstCertificateTlsPolicy - server certificate: 1.2.840.113549.1.9.1=#1612737570706f727440766d776172652e636f6d,CN=mwtstvc01vm.fm.intel.comvCenterServer_MWTSTVC01VM,OU=VMware\, Inc.,O=VMware\, Inc.
22:07:19.216 [main] DEBUG c.i.m.t.TrustFirstCertificateTlsPolicy - Saving certificate 1.2.840.113549.1.9.1=#1612737570706f727440766d776172652e636f6d,CN=mwtstvc01vm.fm.intel.comvCenterServer_MWTSTVC01VM,OU=VMware\, Inc.,O=VMware\, Inc.
22:07:19.216 [main] INFO  c.i.m.t.KeystoreCertificateRepository - Saving keystore
22:07:19.382 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - sending post request...
22:07:19.682 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - Got cookie: vmware_soap_session="5285cacc-fd86-d018-e976-8acc14001e2f"; Path=/; HttpOnly; = {}
22:07:19.682 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - got response, reading...
22:07:19.704 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - releasing connection...
22:07:19.704 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - Login response; <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<LoginResponse xmlns="urn:vim25"><returnval><key>52d2c386-e2b1-ecf0-d661-98fb6ac075de</key><userName>Administrator</userName><fullName> </fullName><loginTime>2013-11-05T06:07:19.710045Z</loginTime><lastActiveTime>2013-11-05T06:07:19.710045Z</lastActiveTime><locale>en</locale><messageLocale>en</messageLocale></returnval></LoginResponse>
</soapenv:Body>
</soapenv:Envelope>
22:07:19.705 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - Login 1 done
22:07:19.705 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 1...
22:07:24.523 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - find host request: <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns2:FindByDnsName xmlns:ns2="urn:vim25"><ns2:_this type="SearchIndex">SearchIndex</ns2:_this><ns2:dnsName>10.1.71.173</ns2:dnsName><ns2:vmSearch>false</ns2:vmSearch></ns2:FindByDnsName></SOAP-ENV:Body></SOAP-ENV:Envelope>
22:07:24.523 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - preparing post request...
22:07:24.523 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - sending post request...
22:07:24.732 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - got response, reading...
22:07:24.733 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - releasing connection...
22:07:24.734 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - find host response; <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
22:07:24.734 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 1 done
22:07:24.735 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 2...
22:07:29.041 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - find host request: <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"><SOAP-ENV:Header/><SOAP-ENV:Body><ns2:FindByDnsName xmlns:ns2="urn:vim25"><ns2:_this type="SearchIndex">SearchIndex</ns2:_this><ns2:dnsName>10.1.71.173</ns2:dnsName><ns2:vmSearch>false</ns2:vmSearch></ns2:FindByDnsName></SOAP-ENV:Body></SOAP-ENV:Envelope>
22:07:29.042 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - preparing post request...
22:07:29.042 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - sending post request...
22:07:29.193 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - got response, reading...
22:07:29.194 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - releasing connection...
22:07:29.194 [main] DEBUG t.vendor.vmware.JaxbVmwareUrlClient - find host response; <?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<soapenv:Body>
<FindByDnsNameResponse xmlns="urn:vim25"><returnval type="HostSystem">host-353</returnval></FindByDnsNameResponse>
</soapenv:Body>
</soapenv:Envelope>
22:07:29.194 [main] DEBUG t.vendor.vmware.JaxbVmwareClientTest - find host 2 done
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 22.184 sec
 * 
 * 
 * @author jbuhacoff
 */
public class JaxbVmwareUrlClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxbVmwareUrlClient.class);
    private String endpoint = "https://10.1.71.162/sdk";
    private URL endpointURL;
    private TlsPolicy tlsPolicy;
    private String cookie = null;
    
    public JaxbVmwareUrlClient() throws Exception {
        endpointURL = new URL(endpoint);
        initTls();
    }
    
    private void initTls() throws Exception {
//       tlsPolicy = new InsecureTlsPolicy(); // would be replaced by what we currently do for getting the policy
        ByteArrayResource resource = new ByteArrayResource();
        SimpleKeystore keystore  = new SimpleKeystore(resource, "password");
        KeystoreCertificateRepository repository = new KeystoreCertificateRepository(keystore);
        tlsPolicy = new TrustFirstCertificateTlsPolicy(repository);
        TlsPolicyManager.getInstance().setTlsPolicy("10.1.71.162", tlsPolicy);
        // regular java
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS"); // or SSL
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, new javax.net.ssl.TrustManager[]{tlsPolicy.getTrustManager()}, null); // key manager, trust manager, securerandom
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(tlsPolicy.getHostnameVerifier());    
    }
    
    public void login() throws JAXBException, SOAPException, IOException {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage message = mf.createMessage();
        SOAPBody body = message.getSOAPBody();
        
        JAXBContext jc = JAXBContext.newInstance( "com.vmware.vim25" );
        Marshaller serializer = jc.createMarshaller();
        LoginRequestType loginRequest = new LoginRequestType();
        ManagedObjectReference sessionManager = new ManagedObjectReference();
        sessionManager.setType("SessionManager");
        sessionManager.setValue("SessionManager");
        loginRequest.setThis(sessionManager);
        loginRequest.setUserName("Administrator");
        loginRequest.setPassword("intel123!");
//        serializer.marshal(loginRequest, body); // won't work because no @XmlRootElement annotation on this type (since the soap message is the root)
        serializer.marshal(new JAXBElement(new QName("urn:vim25", "Login"),LoginRequestType.class,loginRequest), body);
        message.saveChanges();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        log.debug("Login request: {}", out.toString());
        
        String response = post(out.toString());
        log.debug("Login response; {}", response);
        
    }
    
    public void findHost() throws JAXBException, SOAPException, IOException {
        MessageFactory mf = MessageFactory.newInstance();
        SOAPMessage message = mf.createMessage();
        SOAPBody body = message.getSOAPBody();
        
        JAXBContext jc = JAXBContext.newInstance( "com.vmware.vim25" );
        Marshaller serializer = jc.createMarshaller();
        FindByDnsNameRequestType findByDnsName = new FindByDnsNameRequestType();
        ManagedObjectReference searchIndex = new ManagedObjectReference();
        searchIndex.setType("SearchIndex");
        searchIndex.setValue("SearchIndex");
        findByDnsName.setThis(searchIndex);
        findByDnsName.setDnsName("10.1.71.173");
        findByDnsName.setVmSearch(false);
//        serializer.marshal(loginRequest, body); // won't work because no @XmlRootElement annotation on this type (since the soap message is the root)
        serializer.marshal(new JAXBElement(new QName("urn:vim25", "FindByDnsName"),FindByDnsNameRequestType.class,findByDnsName), body);
        message.saveChanges();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        log.debug("find host request: {}", out.toString());
        
        String response = post(out.toString());
        log.debug("find host response; {}", response);
        
    }
    
    private String post(String message) throws IOException {
        //log.debug("POST url: {}", requestURL);
        //log.debug("POST content-type: {}", message == null ? "(empty)" : message.content.toString());
        //log.debug("POST content: {}", message == null ? "(empty)" : message.content);
        log.debug("preparing post request...");
        HttpURLConnection connection = (HttpURLConnection)endpointURL.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml");
        if( cookie != null ) {
            connection.setRequestProperty("Cookie", cookie);
        }
        if( message != null ) {
            OutputStream out = connection.getOutputStream();
            out.write(message.getBytes());
            out.close();
        }
        log.debug("sending post request...");
        InputStream in = connection.getInputStream();
        String cookieHeader = connection.getHeaderField("Set-Cookie");
        if( cookieHeader != null ) {
            log.debug("Got cookie: {} = {}", cookieHeader); // vmware_soap_session="52fd22f3-d3ec-9a95-2871-d94a6a61ae48"; Path=/; HttpOnly;
            String parts[] = cookieHeader.split(";");
            cookie = parts[0];
        }
        log.debug("got response, reading...");
        String response = IOUtils.toString(in);
        in.close();
        log.debug("releasing connection...");
        connection.disconnect(); // does not necessarily close the underlying socket... it maybe reused transparently by the enxt connection to the same server
//        request.releaseConnection();
        return response;
    }    
    
}
