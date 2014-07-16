/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.util;

import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.http.MutableQuery;
import com.intel.dcsg.cpg.rfc822.Headers;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.ApiRequest;
import com.intel.mtwilson.api.ApiResponse;
import com.intel.mtwilson.api.ClientException;
import java.awt.PageAttributes;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class ProxyApiClient extends ApiClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyApiClient.class);

    public ProxyApiClient(Configuration config) throws ClientException {
        super(config);
    }

    public ProxyApiClient(File configurationFile) throws ClientException, IOException {
        super(configurationFile);
    }

    public ProxyApiClient(URL baseURL, HmacCredential credential, Properties properties) throws ClientException {
        super(baseURL, credential, properties);
    }

    public ProxyApiClient(URL baseURL, RsaCredential credential, Properties properties) throws ClientException {
        super(baseURL, credential, properties);
    }

    public ProxyApiClient(URL baseURL, HmacCredential credential, SimpleKeystore keystore, Configuration config) throws ClientException {
        super(baseURL, credential, keystore, config);
    }

    public ProxyApiClient(URL baseURL, RsaCredential credential, SimpleKeystore keystore, Configuration config) throws ClientException {
        super(baseURL, credential, keystore, config);
    }

    public ProxyApiClient(URL baseURL, RsaCredential credential, SimpleKeystore keystore, TlsPolicy tlsPolicy) throws ClientException {
        super(baseURL, credential, keystore, tlsPolicy);
    }

    private Headers copyRequestHeaders(HttpServletRequest request) {
        Headers headers = new Headers();
        Enumeration headerNames = request.getHeaderNames();
        if( headerNames == null ) { return headers; }
        while(headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            Enumeration headerValues = request.getHeaders(headerName);
            if( headerValues == null ) { continue; }
            while(headerValues.hasMoreElements()) {
                String headerValue = (String)headerValues.nextElement();
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }
    
    /**
     * Facilitates integration of tag management UI into mtwilson-portal by
     * allowing it to access mtwilson APIs using the credentials of the 
     * portal user.
     * See also V2Proxy.java
     * 
     * @param request
     * @param response
     * @throws Exception 
     */
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("path info: {}", request.getPathInfo()); // example:  path info: /configurations
//        log.debug("context path: {}", request.getContextPath()); // example:   context path: /mtwilson-portal
//        log.debug("path translated: {}", request.getPathTranslated()); // example:  path translated: /usr/share/glassfish4/glassfish/domains/domain1/applications/mtwilson-portal-2.0-SNAPSHOT/configurations  (assumes file on disk which of course doesn't make sense for an api unless we have some static content for some GET urls)
//        log.debug("request uri: {}", request.getRequestURI()); // example: request uri: /mtwilson-portal/v2proxy/configurations
//        log.debug("sevlet path: {}", request.getServletPath()); // example: servlet path: /v2proxy
        log.debug("query string: {}", request.getQueryString());
        
        String proxyUrl  = request.getPathInfo();
        /*
        String proxyUrl = request.getParameter("proxyUrl");
//        if( proxyUrl.startsWith("/v2proxy") ) {
//            proxyUrl = proxyUrl.replaceFirst("/v2proxy", "");
//        }
        */
        if( proxyUrl.startsWith("/") ) {
            proxyUrl = proxyUrl.replaceFirst("/", "");
        }
        
        /*
        // reconstruct the query string without the proxyUrl parameter, instead of using  request.getQueryString() which would also include it
        // unfortunately because of the way the proxy is configured we get every parameter twice 
        MutableQuery query = new MutableQuery(request.getParameterMap());
        query.removeAll("proxyUrl");
        removeDuplicateParameters(query);
        String queryString = query.toString();
        */
        String queryString = request.getQueryString();
        if( queryString == null ) { queryString = ""; }
        if( queryString.startsWith("?") ) {
            queryString = queryString.replaceFirst("?", "");
        }
        
        String querySeparator = "?";
        if( queryString.isEmpty()) { querySeparator = ""; }
            
        log.debug("Request URI: {}", request.getRequestURI()); // looks like this:  /mtwilson-portal/v2proxy/configurations
        log.debug("Request URL: {}", request.getRequestURL()); // looks like this:  https://10.1.71.49:8443/mtwilson-portal/v2proxy/configurations
        int pathIndex = request.getRequestURL().toString().indexOf(request.getRequestURI());
        String server = request.getRequestURL().toString().substring(0, pathIndex);
//        String server = String.format("%s://%s:%d", request.getScheme(), request.getLocalName(), request.getLocalPort()); // this is wrong because if client sends https://192.168.1.100:8443  but the local /etc/hosts file has 192.168.1.100 mapped to "testserver"  then we would see here https://testserver:8443 which is fine for networking but will cause the signature on the original request to be unverifiable by the server (since the client signed the URL *they* used, not the URL we are rewriting - so we have to keep it the same)
        String urltext = String.format("%s/mtwilson/v2/%s%s%s", server, proxyUrl, querySeparator, queryString);
        log.debug("Proxy URL: {}", urltext);
        log.debug("Proxy Content-Type: {}", request.getContentType()); // example:    application/json; charset=UTF-8
        //  MediaType.valueof(...)  can't handle the  parameters like "; charset=UTF-8"  so we have to strip them out 
        String contentType = request.getContentType();
        if( contentType != null ) {
            String[] contentTypeParts = contentType.split(";");
            if( contentTypeParts.length > 0 ) {
                contentType = contentTypeParts[0];
            }
        }
        String content = null;
        Headers headers = copyRequestHeaders(request);
        ApiRequest proxyRequest;
        ApiResponse proxyResponse;
        switch (request.getMethod()) {
            case "GET":
                proxyResponse = httpGet(urltext, headers);
                break;
            case "DELETE":
                proxyResponse = httpDelete(urltext, headers);
                break;
            case "PUT":
                content = IOUtils.toString(request.getInputStream());
                proxyRequest = new ApiRequest(MediaType.valueOf(contentType), content);
                proxyResponse = httpPut(urltext, proxyRequest, headers);
                break;
            case "POST":
                content = IOUtils.toString(request.getInputStream());
                proxyRequest = new ApiRequest(MediaType.valueOf(contentType), content);
                proxyResponse = httpPost(urltext, proxyRequest, headers);
                break;
            default:
                throw new UnsupportedOperationException("Method not supported by proxy: " + request.getMethod());
        }
        if (proxyResponse != null) {
            response.setStatus(proxyResponse.httpStatusCode);
            response.setContentType(proxyResponse.contentType.toString());
            OutputStream out = response.getOutputStream();
            IOUtils.write(proxyResponse.content, out);
            out.close();
            return;
        }
        throw new IOException("Proxy failed for request: " + urltext);
    }
    
    /***** UNUSED
    private void removeDuplicateParameters(MutableQuery query) {
        HashSet<String> keys = new HashSet<String>();
        keys.addAll(query.keySet()); // iterate on this copy instead of on the query's keySet so we don't get ConcurrentModificationException when we remove keys inside the loop
        for(String key : keys) {
            List<String> values = query.getAll(key);
            HashSet<String> uniqueValues = new HashSet<String>();
            uniqueValues.addAll(values);
            query.removeAll(key);
            query.add(key, uniqueValues);
        }
    }*/
}
