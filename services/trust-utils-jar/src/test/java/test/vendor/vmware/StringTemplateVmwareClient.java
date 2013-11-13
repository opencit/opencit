/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.vendor.vmware;

import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TlsPolicyManager;
import com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy;
import com.intel.mtwilson.tls.KeystoreCertificateRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
public class StringTemplateVmwareClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringTemplateVmwareClient.class);
    private String endpoint = "https://10.1.71.162/sdk";
    private URL endpointURL;
    private TlsPolicy tlsPolicy;
    /*
    private DefaultHttpClient httpClient;
    private SchemeRegistry sr = new SchemeRegistry();
    * */
    private String cookie = null;
    
    public StringTemplateVmwareClient() throws Exception {
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
        /*
        // for apache http client
            SSLSocketFactory sf = new SSLSocketFactory(
                sc,
                new AllowAllHostnameVerifier() // tlsPolicy.getHostnameVerifier() //.getApacheHostnameVerifier()
                );
            Scheme https = new Scheme("https", 443, sf); // URl defaults to 443 for https but if user specified a different port we use that instead
        
         sr.register(https);            
        ClientConnectionManager connectionManager = new BasicClientConnectionManager(sr);         
        httpClient = new DefaultHttpClient(connectionManager);
        */
    }
        
    public void login() throws IOException {
        String template = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><Login xmlns=\"urn:vim25\"><_this type=\"SessionManager\">SessionManager</_this><userName>Administrator</userName><password>intel123!</password></Login></S:Body></S:Envelope>";
        log.debug("Login request: {}", template);
        String response = post(template);
        log.debug("Login response: {}", response);
 
    }
    
    public void findHost() throws IOException {
        String template = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><FindByDnsName xmlns=\"urn:vim25\"><_this type=\"SearchIndex\">SearchIndex</_this><dnsName>10.1.71.173</dnsName><vmSearch>false</vmSearch></FindByDnsName></S:Body></S:Envelope>";
        log.debug("find host request: {}", template);
        String response = post(template);
        log.debug("find host response: {}", response);
    }
/*
    private String postHttpClient(String message) throws IOException {
        //log.debug("POST url: {}", requestURL);
        //log.debug("POST content-type: {}", message == null ? "(empty)" : message.content.toString());
        //log.debug("POST content: {}", message == null ? "(empty)" : message.content);
        log.debug("preparing post request...");
        HttpPost request = new HttpPost(endpoint);
        if( message != null ) {
            request.setEntity(new StringEntity(message, ContentType.create("text/xml", "UTF-8")));
        }
        if( cookie != null ) {
            request.addHeader("Cookie", cookie);
        }
        log.debug("sending post request...");
        HttpResponse httpResponse = httpClient.execute(request);
        Header cookieHeader = httpResponse.getFirstHeader("Set-Cookie");
        if( cookieHeader != null ) {
            log.debug("Got cookie: {} = {}", cookieHeader.getName(), cookieHeader.getValue()); // vmware_soap_session="52fd22f3-d3ec-9a95-2871-d94a6a61ae48"; Path=/; HttpOnly;
            String parts[] = cookieHeader.getValue().split(";");
            cookie = parts[0];
        }
        log.debug("got response, reading...");
        String response = readResponse(httpResponse);
        log.debug("releasing connection...");
        request.releaseConnection();
        return response;
    }*/
    
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
