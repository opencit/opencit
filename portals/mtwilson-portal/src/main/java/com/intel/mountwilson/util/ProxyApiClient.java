/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.util;

import com.intel.dcsg.cpg.crypto.HmacCredential;
import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
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

    /**
     * Facilitates integration of tag management UI into mtwilson-portal by
     * allowing it to access mtwilson APIs using the credentials of the 
     * portal user.
     * See also V2Proxy.jsp
     * 
     * @param request
     * @param response
     * @throws Exception 
     */
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String proxyUrl = request.getParameter("proxyUrl"); /*request.getPathInfo()*/
        /*
        if( proxyUrl.startsWith("/v2proxy") ) {
            proxyUrl = proxyUrl.replaceFirst("/v2proxy", "");
        }*/
        if( proxyUrl.startsWith("/") ) {
            proxyUrl = proxyUrl.replaceFirst("/", "");
        }
        String urltext = String.format("https://%s:%d/mtwilson/v2/%s?%s", request.getLocalName(), request.getLocalPort(), proxyUrl, request.getQueryString());
        log.debug("Proxy URL: {}", urltext);
        log.debug("Proxy Content-Type: {}", request.getContentType()); // example:    application/json; charset=UTF-8
        //  MediaType.valueof(...)  can't handle the  parameters like "; charset=UTF-8"  so we have to strip them out ... TODO parse the content type and then use the MediaType constructor that accepts a map of already-parsed parameters,
        String contentType = request.getContentType();
        if( contentType != null ) {
            String[] contentTypeParts = contentType.split(";");
            if( contentTypeParts.length > 0 ) {
                contentType = contentTypeParts[0];
            }
        }
        String content = null;
        ApiRequest proxyRequest = null;
        ApiResponse proxyResponse = null;
        switch (request.getMethod()) {
            case "GET":
                proxyResponse = httpGet(urltext);
                break;
            case "DELETE":
                proxyResponse = httpDelete(urltext);
                break;
            case "PUT":
                content = IOUtils.toString(request.getInputStream());
                proxyRequest = new ApiRequest(MediaType.valueOf(contentType), content);
                proxyResponse = httpPut(urltext, proxyRequest);
                break;
            case "POST":
                content = IOUtils.toString(request.getInputStream());
                proxyRequest = new ApiRequest(MediaType.valueOf(contentType), content);
                proxyResponse = httpPost(urltext, proxyRequest);
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
}
