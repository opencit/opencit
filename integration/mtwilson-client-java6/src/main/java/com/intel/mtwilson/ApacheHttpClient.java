/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;

import com.intel.dcsg.cpg.i18n.LocaleUtil;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.tls.policy.ProtocolSelector;
import com.intel.mtwilson.security.http.apache.ApacheHttpAuthorization;
import com.intel.dcsg.cpg.x509.repository.KeystoreCertificateRepository;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.ConfigurableProtocolSelector;
import com.intel.dcsg.cpg.tls.policy.impl.FirstCertificateTrustDelegate;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Locale;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.rfc822.Headers;
import com.intel.dcsg.cpg.rfc822.Rfc822Date;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateTlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.PublicKeyTlsPolicy;
import com.intel.dcsg.cpg.x509.repository.PublicKeyCertificateRepository;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApacheHttpClient implements java.io.Closeable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String ACCEPT_LANGUAGE = "Accept-Language";
//    private SchemeRegistry sr;
    private ClientConnectionManager connectionManager;
    private HttpClient httpClient;
//    private SimpleKeystore keystore;
    private String protocol = "https";
//    private String tlsProtocol = "TLS"; // issue #870  possible values are SSL, SSLv2, SSLv3, TLS, TLSv1, TLSv1.1, and TLSv1.2, and we use TLS as the default
    private int port = 443;
    private TlsPolicy apacheTlsPolicy;
//    private boolean requireTrustedCertificate = true;
//    private boolean verifyHostname = true;
    private Locale locale = null;
    private ApacheHttpAuthorization authority = null; // can be any implementation - Hmac256 or RSA
    protected static final ObjectMapper mapper = new ObjectMapper();
    private int timeDeltaMs = 0; // the difference in time between the client and the server, computed  timeDeltaMs = serverDate - clientDate;  when client sends a request to server,  client sends requestDate = clientDate + timeDeltaMs which should match serverDate;  the timeDeltaMs is updated after every response received from the server

    /**
     * If you don't have a specific configuration, you can pass in
     * SystemConfiguration() so that users can set system properties and have
     * them passed through to this object.
     *
     * @param baseURL for the server to access (all requests are based on this
     * URL)
     * @param credentials to use when signing HTTP requests, or null if you want
     * to skip the Authorization header
     * @param sslKeystore containing trusted SSL certificates
     * @param config with parameters requireTrustedCertificates and
     * verifyHostname; if null a SystemConfiguration will be used.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public ApacheHttpClient(URL baseURL, ApacheHttpAuthorization credentials, SimpleKeystore sslKeystore, Configuration config) throws NoSuchAlgorithmException, KeyManagementException {
        authority = credentials;
//        keystore = sslKeystore;

        protocol = baseURL.getProtocol();
        port = baseURL.getPort();
        if (port == -1) {
            port = baseURL.getDefaultPort();
        }
        //log.debug("ApacheHttpClient: Protocol: {}", protocol);
        //log.debug("ApacheHttpClient: Port: {}", port);

        if (config == null) {
            config = new SystemConfiguration();
            log.debug("ApacheHttpClient: using system configuration");
        }
//        requireTrustedCertificate = config.getBoolean("mtwilson.api.ssl.requireTrustedCertificate", true);
//        verifyHostname = config.getBoolean("mtwilson.api.ssl.verifyHostname", true);
        apacheTlsPolicy = createTlsPolicy(config, sslKeystore);
//        tlsProtocol = config.getString("mtwilson.api.ssl.protocol", "TLS");
        //log.debug("ApacheHttpClient: TLS Policy Name: {}", tlsPolicy.getClass().getName());
        initHttpClient();
    }

    /**
     * Uses default protocol "TLS" , if you want to use something else such as
     * SSLv3 or TLSv1.2 then call setTlsProtocol("TLSv1.2") after instantiating
     * the ApacheHttpClient
     *
     * @param baseURL
     * @param credentials
     * @param sslKeystore
     * @param tlsPolicy
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public ApacheHttpClient(URL baseURL, ApacheHttpAuthorization credentials, SimpleKeystore sslKeystore, TlsPolicy tlsPolicy) throws NoSuchAlgorithmException, KeyManagementException {
        authority = credentials;
//        keystore = sslKeystore;

        protocol = baseURL.getProtocol();
        port = baseURL.getPort();
        if (port == -1) {
            port = baseURL.getDefaultPort();
        }
        //log.debug("ApacheHttpClient: Protocol: {}", protocol);
        //log.debug("ApacheHttpClient: Port: {}", port);

//        requireTrustedCertificate = config.getBoolean("mtwilson.api.ssl.requireTrustedCertificate", true);
//        verifyHostname = config.getBoolean("mtwilson.api.ssl.verifyHostname", true);
        apacheTlsPolicy = tlsPolicy; // createApacheTlsPolicy(tlsPolicy/*, sslKeystore*/);
        // tlsProtocol = "TLS"; // the default  
        //log.debug("ApacheHttpClient: TLS Policy Name: {}", tlsPolicy.getClass().getName());
        initHttpClient();
    }

    private void initHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
//        log.debug("Using TLS protocol {}", tlsProtocol);
        SchemeRegistry sr = initSchemeRegistryWithPolicy(protocol, port, apacheTlsPolicy/*, tlsProtocol*/);
        connectionManager = new PoolingClientConnectionManager(sr);

        // the http client is re-used for all the requests
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        httpClient = new DefaultHttpClient(connectionManager, httpParams);
    }

    /*
     public void setTlsProtocol(String protocol) throws KeyManagementException, NoSuchAlgorithmException {
     //        tlsProtocol = protocol;
     initHttpClient(); // reset the client with the updated protocol
     }
     */
    /**
     * Used in Mt Wilson 1.1
     *
     * If the configuration mentions a specific TLS Policy (new in 1.1) that one
     * is used, otherwise the trusted certificate and verify hostname settings
     * used in 1.0-RC2 are used to choose an appropriate TLS Policy.
     *
     * @param config
     * @param sslKeystore
     * @return
     */
    private TlsPolicy createTlsPolicy(Configuration config, SimpleKeystore sslKeystore) {
        String tlsProtocol = config.getString("mtwilson.api.ssl.protocol", "TLS");
        ProtocolSelector pSelector = new ConfigurableProtocolSelector(tlsProtocol);
        String tlsPolicyName = config.getString("mtwilson.api.ssl.policy", "");
        if (tlsPolicyName == null || tlsPolicyName.isEmpty()) {
            // no 1.1 policy name, so use 1.0-RC2 settings to pick a policy
            boolean requireTrustedCertificate = config.getBoolean("mtwilson.api.ssl.requireTrustedCertificate", true);
            boolean verifyHostname = config.getBoolean("mtwilson.api.ssl.verifyHostname", true);
            if (requireTrustedCertificate && verifyHostname) {
                log.debug("Using TLS Policy TRUST_CA_VERIFY_HOSTNAME");
                return new CertificateTlsPolicy(sslKeystore.getRepository(), pSelector);
            } else if (requireTrustedCertificate && !verifyHostname) {
                // two choices: trust first certificate or trust known certificate;  we choose trust first certificate as a usability default
                // furthermore we assume that the api client keystore is a server-specific keystore (it's a client configured for a specific mt wilson server)
                // that either has a server instance ssl cert or a cluster ssl cert.  either should work.
                log.debug("Using TLS Policy TRUST_FIRST_CERTIFICATE");
                KeystoreCertificateRepository repository = sslKeystore.getRepository();
                return new PublicKeyTlsPolicy(new PublicKeyCertificateRepository(repository), new FirstCertificateTrustDelegate(repository), pSelector);
            } else { // !requireTrustedCertificate && (verifyHostname || !verifyHostname)
                log.warn("Using TLS Policy INSECURE");
                return new InsecureTlsPolicy();
            }
        } else if (tlsPolicyName.equals("TRUST_CA_VERIFY_HOSTNAME")) {
            log.debug("TLS Policy: TRUST_CA_VERIFY_HOSTNAME");
            return new CertificateTlsPolicy(sslKeystore.getRepository(), pSelector);
        } else if (tlsPolicyName.equals("TRUST_FIRST_CERTIFICATE")) {
            log.debug("TLS Policy: TRUST_FIRST_CERTIFICATE");
            KeystoreCertificateRepository repository = sslKeystore.getRepository();
            return new PublicKeyTlsPolicy(new PublicKeyCertificateRepository(repository), new FirstCertificateTrustDelegate(repository), pSelector);
        } else if (tlsPolicyName.equals("TRUST_KNOWN_CERTIFICATE")) {
            log.debug("TLS Policy: TRUST_KNOWN_CERTIFICATE");
            return new PublicKeyTlsPolicy(new PublicKeyCertificateRepository(sslKeystore.getRepository()), pSelector);
        } else if (tlsPolicyName.equals("INSECURE")) {
            log.warn("TLS Policy: INSECURE");
            return new InsecureTlsPolicy();
        } else {
            // unrecognized 1.1 policy defined, so use a secure default
            log.warn("Unknown TLS Policy Name: {}", tlsPolicyName);
            return new CertificateTlsPolicy(sslKeystore.getRepository(), pSelector); // issue #871 default should be secure
        }
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
     * Used in Mt Wilson 1.0-RC2
     *
     * Base URL and other configuration must already be set before calling this
     * method.
     *
     * @param protocol either "http" or "https"
     * @param port such as 80 for http, 443 for https
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    /*
     private SchemeRegistry initSchemeRegistry(String protocol, int port) throws KeyManagementException, NoSuchAlgorithmException {
     SchemeRegistry sr = new SchemeRegistry();
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
     trustManager = SslUtil.createX509TrustManagerWithKeystore(keystore);                
     }
     else if( requireTrustedCertificate ) { // config.getBoolean("mtwilson.api.ssl.requireTrustedCertificate", true) ) {
     //String truststore = config.getString("mtwilson.api.keystore", System.getProperty("javax.net.ssl.trustStorePath")); // if null use default java trust store...
     //String truststorePassword = config.getString("mtwilson.api.keystore.password", System.getProperty("javax.net.ssl.trustStorePassword"));
     //                String truststore = System.getProperty("javax.net.ssl.trustStorePath");
     String truststore = System.getProperty("javax.net.ssl.trustStore");
     String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
                
     // create a trust manager using only our trusted ssl certificates
     if( truststore == null || truststorePassword == null ) {
     throw new IllegalArgumentException("Require trusted certificates is enabled but truststore is not configured");
     }
     keystore = new SimpleKeystore(new File(truststore), truststorePassword);
     trustManager = SslUtil.createX509TrustManagerWithKeystore(keystore);
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
     return sr;
     }
     */
    /**
     * Used in Mt Wilson 1.1
     *
     * @param protocol
     * @param port
     * @param policy
     * @param tlsProtocol like SSL, SSLv2, SSLv3, TLS, TLSv1.1, TLSv1.2
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    private SchemeRegistry initSchemeRegistryWithPolicy(String protocol, int port, TlsPolicy policy /*, String tlsProtocol*/) throws KeyManagementException, NoSuchAlgorithmException {
        SchemeRegistry sr = new SchemeRegistry();
        if ("http".equals(protocol)) {
            Scheme http = new Scheme("http", port, PlainSocketFactory.getSocketFactory());
            sr.register(http);
        }
        if ("https".equals(protocol)) {
            log.debug("Initializing {} connection", policy.getProtocolSelector().preferred());
            SSLContext sslcontext = SSLContext.getInstance(TlsUtil.getSafeContextName(policy.getProtocolSelector().preferred()) /*tlsProtocol*/); // issue #870 allow client to configure TLS protocol version with mtwilson.api.ssl.protocol
            sslcontext.init(null, new X509TrustManager[]{policy.getTrustManager()}, null); // key manager, trust manager, securerandom
            SSLSocketFactory sf = new SSLSocketFactory(
                    sslcontext,
                    policy.getHostnameVerifier());
            Scheme https = new Scheme("https", port, sf); // URL defaults to 443 for https but if user specified a different port we use that instead
            sr.register(https);
        }
        return sr;
    }

    /**
     * Call this to ensure that all HTTP connections and files are closed when
     * your are done using the API Client.
     */
    @Override
    public void close() {
        connectionManager.shutdown();
    }

    private MediaType createMediaType(HttpResponse response) {
        if (response.getFirstHeader("Content-Type") != null) {
            String contentType = response.getFirstHeader("Content-Type").getValue();
            log.debug("We got Content-Type: " + contentType);
            return MediaType.valueOf(contentType);
        }
        log.warn("Missing content type header from server, assuming application/octet-stream");
        return MediaType.APPLICATION_OCTET_STREAM_TYPE;
    }

    /**
     * 
     * Typically the HttpEntity is NOT chunked, is streaming, and is not repeatable:
     * <pre>
2014-06-01 23:42:11,041 DEBUG [http-bio-8443-exec-17] c.i.m.ApacheHttpClient [ApacheHttpClient.java:354] We got Content-Type: text/plain
2014-06-01 23:42:11,041 DEBUG [http-bio-8443-exec-17] c.i.m.ApacheHttpClient [ApacheHttpClient.java:371] HttpEntity Content Length = 2
2014-06-01 23:42:11,041 DEBUG [http-bio-8443-exec-17] c.i.m.ApacheHttpClient [ApacheHttpClient.java:372] HttpEntity is chunked? false
2014-06-01 23:42:11,041 DEBUG [http-bio-8443-exec-17] c.i.m.ApacheHttpClient [ApacheHttpClient.java:373] HttpEntity is streaming? true
2014-06-01 23:42:11,041 DEBUG [http-bio-8443-exec-17] c.i.m.ApacheHttpClient [ApacheHttpClient.java:374] HttpEntity is repeatable? false
     * </pre>
     * 
     * @param response
     * @return
     * @throws IOException 
     */
    private ApiResponse readResponse(HttpResponse response) throws IOException {
        calculateTimeDelta(response);
        MediaType contentType = createMediaType(response);
        byte[] content = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream contentStream = entity.getContent();
            if (contentStream != null) {
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

    private void calculateTimeDelta(HttpResponse response) {
        Calendar now = Calendar.getInstance();
        Date serverTime = Rfc822Date.parse(response.getFirstHeader("Date").getValue());
        timeDeltaMs = (int)(serverTime.getTime() - now.getTimeInMillis());
        log.debug("calculated time delta {} = server time {} - client time {}", timeDeltaMs, serverTime, now.getTime());
    }
    
    private void addHeaders(HttpRequestBase request, Headers headers) {
        for (String name : headers.names()) {
            // skip "content-length" as org.apache.http.client.HttpClient.execute method needs to set it and will cause an error if it's already set
            if (name.equalsIgnoreCase("content-length")) {
                continue;
            }
            for (String value : headers.getAll(name)) {
                request.addHeader(name, value);
            }
        }
    }
    
    private void addLocaleHeader(HttpRequestBase request) {
        if (locale != null) {
            request.addHeader(ACCEPT_LANGUAGE, LocaleUtil.toAcceptHeader(locale));
        }
    }
    private void setDateHeader(HttpRequestBase request) {
        log.debug("current time is {}", new Date());
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MILLISECOND, timeDeltaMs);
        request.setHeader("Date", Rfc822Date.format(now.getTime()));
        log.debug("set date header to {} using time delta {}", request.getFirstHeader("Date").getValue(), timeDeltaMs);
    }

    public ApiResponse get(String requestURL) throws IOException, ApiException, SignatureException {
        return get(requestURL, null);
    }

    public ApiResponse get(String requestURL, Headers headers) throws IOException, ApiException, SignatureException {
        log.debug("GET url: {}", requestURL);        
        HttpGet request = new HttpGet(requestURL);
        addLocaleHeader(request);
        setDateHeader(request);
        if (headers != null) {
            addHeaders(request, headers);
        }
        if (authority != null) {
            authority.addAuthorization(request); // add authorization header
        }
        // send the request and print the response
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }

    public ApiResponse delete(String requestURL) throws IOException, SignatureException {
        return delete(requestURL, null);
    }

    public ApiResponse delete(String requestURL, Headers headers) throws IOException, SignatureException {
        log.debug("DELETE url: {}", requestURL);
        HttpDelete request = new HttpDelete(requestURL);
        addLocaleHeader(request);
        setDateHeader(request);
        if (headers != null) {
            addHeaders(request, headers);
        }
        if (authority != null) {
            authority.addAuthorization(request); // add authorization header
        }
        // send the request and print the response
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }

    public ApiResponse put(String requestURL, ApiRequest message) throws IOException, SignatureException {
        return put(requestURL, message, null);
    }

    public ApiResponse put(String requestURL, ApiRequest message, Headers headers) throws IOException, SignatureException {
        log.debug("PUT url: {}", requestURL);
        //log.debug("PUT content: {}", message == null ? "(empty)" : message.content);
        HttpPut request = new HttpPut(requestURL);
        if (message != null && message.content != null) {
            request.setEntity(new StringEntity(message.content, ContentType.create(message.contentType.toString(), "UTF-8")));
        }
        addLocaleHeader(request);
        setDateHeader(request);
        if (headers != null) {
            addHeaders(request, headers);
        }
        if (authority != null) {
            authority.addAuthorization((HttpEntityEnclosingRequest) request); // add authorization header
        }
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        request.releaseConnection();
        return apiResponse;
    }

    public ApiResponse post(String requestURL, ApiRequest message) throws IOException, SignatureException {
        return post(requestURL, message, null);
    }

    public ApiResponse post(String requestURL, ApiRequest message, Headers headers) throws IOException, SignatureException {
        log.debug("POST url: {}", requestURL);
        //log.debug("POST content-type: {}", message == null ? "(empty)" : message.content.toString());
        //log.debug("POST content: {}", message == null ? "(empty)" : message.content);
        HttpPost request = new HttpPost(requestURL);
        if (message != null && message.content != null) {
            request.setEntity(new StringEntity(message.content, ContentType.create(message.contentType.toString(), "UTF-8")));
        }
        //System.out.println("debug|HTTP POST message content: " + message.content);

        addLocaleHeader(request);
        setDateHeader(request);
        if (headers != null) {
            addHeaders(request, headers);
        }
        if (authority != null) {
            authority.addAuthorization((HttpEntityEnclosingRequest) request); // add authorization header
        }
        HttpResponse httpResponse = httpClient.execute(request);
        ApiResponse apiResponse = readResponse(httpResponse);
        //System.out.println("debug|HTTP Response content: " + new String(apiResponse.content, Charset.forName("UTF-8")));
        request.releaseConnection();
        return apiResponse;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
