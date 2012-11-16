/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;
import com.intel.mtwilson.crypto.NopX509TrustManager;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.security.http.ApacheHttpAuthorization;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApacheHttpClient implements java.io.Closeable {
    private final Logger log = LoggerFactory.getLogger(getClass());    
    private SchemeRegistry sr;
    private ClientConnectionManager connectionManager;
    private HttpClient httpClient;
    private SimpleKeystore keystore;
    private String protocol = "https";
    private int port = 443;
    private boolean requireTrustedCertificate = true;
    private boolean verifyHostname = true;
    
    private ApacheHttpAuthorization authority = null; // can be any implementation - Hmac256 or RSA
    protected static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * If you don't have a specific configuration, you can pass in SystemConfiguration() so that users can set
     * system properties and have them passed through to this object.
     * @param baseURL for the server to access (all requests are based on this URL)
     * @param credentials to use when signing HTTP requests, or null if you want to skip the Authorization header
     * @param sslKeystore containing trusted SSL certificates
     * @param config with parameters requireTrustedCertificates and verifyHostname; if null a SystemConfiguration will be used.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException 
     */
    public ApacheHttpClient(URL baseURL, ApacheHttpAuthorization credentials, SimpleKeystore sslKeystore, Configuration config) throws NoSuchAlgorithmException, KeyManagementException {
        authority = credentials;
        keystore = sslKeystore;
        
        protocol = baseURL.getProtocol();
        port = baseURL.getPort();
        if( port == -1 ) {
            port = baseURL.getDefaultPort();
        }

        if( config == null ) {
            config = new SystemConfiguration();
        }
        requireTrustedCertificate = config.getBoolean("mtwilson.api.ssl.requireTrustedCertificate", true);
        verifyHostname = config.getBoolean("mtwilson.api.ssl.verifyHostname", true);
        initSchemeRegistry(protocol, port);
        connectionManager = new PoolingClientConnectionManager(sr);

        // the http client is re-used for all the requests
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        httpClient = new DefaultHttpClient(connectionManager, httpParams);
    }
    
    
    /*
    public final void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }
    public final void setKeystore(SimpleKeystore keystore) {
        this.keystore = keystore;
    }    
    public final void setRequireTrustedCertificate(boolean value) {
        requireTrustedCertificate = value;
    }
    public final void setVerifyHostname(boolean value) {
        verifyHostname = value;
    }
    * 
    */
    
    /**
     * Base URL and other configuration must already be set before calling this
     * method.
     * 
     * @param protocol either "http" or "https"
     * @param port such as 80 for http, 443 for https
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException 
     */
    private void initSchemeRegistry(String protocol, int port) throws KeyManagementException, NoSuchAlgorithmException {
        sr = new SchemeRegistry();
        if( "http".equals(protocol) ) {
            Scheme http = new Scheme("http", port, PlainSocketFactory.getSocketFactory());
            sr.register(http);
        }
        if( "https".equals(protocol) ) {
            X509HostnameVerifier hostnameVerifier; // secure by default (default verifyHostname = true)
            X509TrustManager trustManager; // secure by default, using Java's implementation which verifies the peer and using java's trusted keystore as default if user does not provide a specific keystore
            if( verifyHostname ) {
                hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
            }
            else { // if( !config.getBoolean("mtwilson.api.ssl.verifyHostname", true) ) {
                hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            }
            
            if( requireTrustedCertificate && keystore != null ) {
                trustManager = KeystoreUtil.createX509TrustManagerWithKeystore(keystore);                
            }
            else if( requireTrustedCertificate ) { // config.getBoolean("mtwilson.api.ssl.requireTrustedCertificate", true) ) {
                //String truststore = config.getString("mtwilson.api.keystore", System.getProperty("javax.net.ssl.trustStorePath")); // if null use default java trust store...
                //String truststorePassword = config.getString("mtwilson.api.keystore.password", System.getProperty("javax.net.ssl.trustStorePassword"));
//                String truststore = System.getProperty("javax.net.ssl.trustStorePath");
                String truststore = System.getProperty("javax.net.ssl.trustStore");
                String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
                
    /**
     * XXX TODO the implementation of this method needs careful review.
     * We are checking the server's SSL certificate chain against our trusted
     * certificates in the keystore.
     * 
     * Very basic implementation checks server cert then server's CA cert; http://stackoverflow.com/questions/6629473/validate-x-509-certificate-agains-concrete-ca-java
     * Example how to use a non-default keystore with the default trust manager: http://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
     */
                // create a trust manager using only our trusted ssl certificates
                if( truststore == null || truststorePassword == null ) {
                    throw new IllegalArgumentException("Require trusted certificates is enabled but truststore is not configured");
                }
                keystore = new SimpleKeystore(new File(truststore), truststorePassword);
                trustManager = KeystoreUtil.createX509TrustManagerWithKeystore(keystore);
            }
            else {
                // user does not want to ensure certificates are trusted, so use a no-op trust manager
                trustManager = new NopX509TrustManager();
            }
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new X509TrustManager[] { trustManager }, null); // key manager, trust manager, securerandom
            SSLSocketFactory sf = new SSLSocketFactory(
                sslcontext,
                hostnameVerifier
                );
            Scheme https = new Scheme("https", port, sf); // URl defaults to 443 for https but if user specified a different port we use that instead
            sr.register(https);            
        }        
    }
    
    /**
     * Call this to ensure that all HTTP connections and files are closed
     * when your are done using the API Client.
     */
    @Override
    public void close() {
        connectionManager.shutdown();
    }
    
    private MediaType createMediaType(HttpResponse response) {
        if( response.getFirstHeader("Content-Type") != null ) {
            String contentType = response.getFirstHeader("Content-Type").getValue();
            log.debug("We got Content-Type: "+contentType );
            if( "text/plain".equals(contentType) ) {
                return MediaType.TEXT_PLAIN_TYPE;
            }
            if( "text/xml".equals(contentType) ) {
                return MediaType.TEXT_XML_TYPE;
            }
            if( "text/html".equals(contentType) ) {
                return MediaType.TEXT_HTML_TYPE;
            }
            if( "application/json".equals(contentType) ) {
                return MediaType.APPLICATION_JSON_TYPE;
            }
            if( "application/xml".equals(contentType) ) {
                return MediaType.APPLICATION_XML_TYPE;
            }
            if( "application/samlassertion+xml".equals(contentType) ) {
                return MediaType.APPLICATION_XML_TYPE;
            }
            if( "application/octet-stream".equals(contentType) ) {
                return MediaType.APPLICATION_OCTET_STREAM_TYPE;
            }
            log.error("Got unsupported content type from server: "+contentType);
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }
        log.error("Missing content type header from server, assuming application/octet-stream");
        return MediaType.APPLICATION_OCTET_STREAM_TYPE;
    }
    
    private ApiResponse readResponse(HttpResponse response) throws IOException {
        MediaType contentType = createMediaType(response);
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
        }
        return new ApiResponse(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), contentType, content);
    }
    
    public ApiResponse get(String requestURL) throws IOException, ApiException, SignatureException {
        log.debug("GET url: {}", requestURL);        
        HttpGet request = new HttpGet(requestURL);
        if( authority != null ) {
            authority.addAuthorization(request); // add authorization header
        }
        // send the request and print the response
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }

    public ApiResponse delete(String requestURL) throws IOException, SignatureException {
        log.debug("DELETE url: {}", requestURL);
        HttpDelete request = new HttpDelete(requestURL);
        if( authority != null ) {
            authority.addAuthorization(request); // add authorization header
        }
        // send the request and print the response
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }
    
    public ApiResponse put(String requestURL, ApiRequest message) throws IOException, SignatureException {
        log.debug("PUT url: {}", requestURL);
        log.debug("PUT content: {}", message == null ? "(empty)" : message.content);
        HttpPut request = new HttpPut(requestURL);
        if( message != null && message.content != null ) {
            request.setEntity(new StringEntity(message.content, ContentType.create(message.contentType.toString(), "UTF-8")));
        }
        if( authority != null ) {
            authority.addAuthorization((HttpEntityEnclosingRequest)request); // add authorization header
        }
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }
    
    public ApiResponse post(String requestURL, ApiRequest message) throws IOException, SignatureException {
        log.debug("POST url: {}", requestURL);
        log.debug("POST content: {}", message == null ? "(empty)" : message.content);
        HttpPost request = new HttpPost(requestURL);
        if( message != null && message.content != null ) {
            request.setEntity(new StringEntity(message.content, ContentType.create(message.contentType.toString(), "UTF-8")));
        }
        if( authority != null ) {
            authority.addAuthorization((HttpEntityEnclosingRequest)request); // add authorization header
        }
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }
}
