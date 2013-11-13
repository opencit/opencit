/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ApiResponse;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TlsPolicyManager;
import com.vmware.vim25.FindByDnsNameRequestType;
import com.vmware.vim25.LoginRequestType;
import com.vmware.vim25.ManagedObjectReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 *
 * @author jbuhacoff
 */
public class JaxbVmwareHttpClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxbVmwareHttpClient.class);
    private String endpoint = "https://10.1.71.162/sdk";
    private DefaultHttpClient httpClient;
    private TlsPolicy tlsPolicy;
    private SchemeRegistry sr = new SchemeRegistry();
    
    public JaxbVmwareHttpClient() throws Exception {
        initTls();
    }
    
    private void initTls() throws Exception {
       tlsPolicy = new InsecureTlsPolicy(); // would be replaced by what we currently do for getting the policy
        TlsPolicyManager.getInstance().setTlsPolicy("10.1.71.162", tlsPolicy);
        // regular java
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS"); // or SSL
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, new javax.net.ssl.TrustManager[]{tlsPolicy.getTrustManager()}, null); // key manager, trust manager, securerandom
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(tlsPolicy.getHostnameVerifier());    
        // for apache http client
            SSLSocketFactory sf = new SSLSocketFactory(
                sc,
                new AllowAllHostnameVerifier() // tlsPolicy.getHostnameVerifier() //.getApacheHostnameVerifier()
                );
            Scheme https = new Scheme("https", 443, sf); // URl defaults to 443 for https but if user specified a different port we use that instead
        
         sr.register(https);            
        ClientConnectionManager connectionManager = new BasicClientConnectionManager(sr);         
        httpClient = new DefaultHttpClient(connectionManager);
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
        HttpPost request = new HttpPost(endpoint);
        if( message != null ) {
            request.setEntity(new StringEntity(message, ContentType.create("text/xml", "UTF-8")));
        }
        HttpResponse httpResponse = httpClient.execute(request);
        String response = readResponse(httpResponse);
        request.releaseConnection();
        return response;
    }
    
    private String readResponse(HttpResponse response) throws IOException {
        byte[] content = null;
        HttpEntity entity = response.getEntity();
        if( entity != null ) {
            InputStream contentStream = entity.getContent();
            if( contentStream != null ) {
                content = IOUtils.toByteArray(contentStream);
                contentStream.close();
            }
            log.debug("HttpEntity Content Length = {}", entity.getContentLength());
            log.debug("HttpEntity is chunked? {}", entity.isChunked());
            log.debug("HttpEntity is streaming? {}", entity.isStreaming());
            log.debug("HttpEntity is repeatable? {}", entity.isRepeatable());
            return new String(content);
        }
        return null;
    }
}
