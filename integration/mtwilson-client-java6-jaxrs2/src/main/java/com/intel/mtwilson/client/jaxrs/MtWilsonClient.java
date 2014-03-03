/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import java.io.File;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import com.intel.mtwilson.security.http.jaxrs.HmacAuthorizationFilter;
import com.intel.mtwilson.security.http.jaxrs.X509AuthorizationFilter;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.util.Map;
import java.util.Properties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
/**
 *
 * @author jbuhacoff
 */
public class MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MtWilsonClient.class);
    private ClientConfig clientConfig;
    private Client client;
    private WebTarget target;
    private String baseurl;
    
    protected MtWilsonClient() {
        clientConfig = new ClientConfig();
        clientConfig.register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
        clientConfig.register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);        
        log.debug("configured client in empty constructor");
    }

    /**
     * Configures a client without any authentication for the given API URL
     * @param url like http://server.com/mtwilson/v2
     */
    public MtWilsonClient(URL url) {
        this();
        baseurl = url.toExternalForm(); // for example "http://localhost:8080/v2"
        client = ClientBuilder.newClient(clientConfig);
        target = client.target(baseurl);
    }

    /**
     * Configures a client using authentication settings in the properties
     * argument. The API URL must be set as mtwilson.api.url or mtwilson.api.baseurl
     * in the properties.
     * 
     * To use BASIC password authentication, set mtwilson.api.username and mtwilson.api.password.
     * 
     * To use HMAC (MtWilson-specific) authentication, set mtwilson.api.clientId and mtwilson.api.secretKey
     * 
     * To use X509 (MtWilson-specific) authentication, set:
     * mtwilson.api.keystore = path to client-keystore.jks
     * mtwilson.api.keystore.password = password protecting client-keystore.jks
     * mtwilson.api.key.alias = alias of private key in the keystore; usually same as username or name of keystore like "client-keystore"
     * mtwilson.api.key.password = password protecting the key, usually same as the keystore password
     * 
     * @param properties
     * @throws KeyManagementException
     * @throws IOException
     * @throws CryptographyException
     * @throws GeneralSecurityException 
     */
    public MtWilsonClient(Properties properties) throws KeyManagementException, IOException, CryptographyException, GeneralSecurityException {
        this();
        baseurl = properties.getProperty("mtwilson.api.url", properties.getProperty("mtwilson.api.baseurl")); // example: "http://localhost:8080/v2";
        // X509 authorization 
        SimpleKeystore keystore = null;
        if (properties.containsKey("mtwilson.api.keystore") && properties.containsKey("mtwilson.api.keystore.password")) {
            FileResource resource = new FileResource(new File(properties.getProperty("mtwilson.api.keystore")));
            keystore = new SimpleKeystore(resource, properties.getProperty("mtwilson.api.keystore.password"));
        }
        if (keystore != null && properties.containsKey("mtwilson.api.key.alias") && properties.containsKey("mtwilson.api.key.password")) {
            log.debug("Loading key {} from keystore {}", properties.getProperty("mtwilson.api.key.alias"), properties.getProperty("mtwilson.api.keystore"));
            RsaCredentialX509 credential = keystore.getRsaCredentialX509(properties.getProperty("mtwilson.api.key.alias"), properties.getProperty("mtwilson.api.key.password"));
            clientConfig.register(new X509AuthorizationFilter(credential));
        }
        // HMAC authorization
        if( properties.containsKey("mtwilson.api.clientId") && properties.containsKey("mtwilson.api.secretKey")) {
            log.debug("Registering HMAC credentials for {}", properties.getProperty("mtwilson.api.clientId"));
            clientConfig.register( new HmacAuthorizationFilter(properties.getProperty("mtwilson.api.clientId"), properties.getProperty("mtwilson.api.secretKey")));
        }
        // BASIC authorization will only be registered if configuration is present but also the feature itself will only add an Authorization header if there isn't already one present
        if( properties.containsKey("mtwilson.api.username") && properties.containsKey("mtwilson.api.password") ) {
            log.debug("Registering BASIC credentials for {}", properties.getProperty("mtwilson.api.username"));
//            clientConfig.register( new BasicPasswordAuthorizationFilter(properties.getProperty("mtwilson.api.username"), properties.getProperty("mtwilson.api.password")));
//            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(properties.getProperty("mtwilson.api.username"), properties.getProperty("mtwilson.api.password"));
//            clientConfig.register(feature);
            
            clientConfig.register( new HttpBasicAuthFilter(properties.getProperty("mtwilson.api.username"), properties.getProperty("mtwilson.api.password")));
        }
        client = ClientBuilder.newClient(clientConfig);
        client.register(new LoggingFilter());
        target = client.target(baseurl);
    }
    /*
     register(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class); 
     register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class); // added
     register(com.intel.mtwilson.jersey.provider.JacksonYamlObjectMapperProvider.class);
     register(com.intel.mtwilson.jersey.provider.ApplicationYamlProvider.class);
     register(com.intel.mtwilson.jersey.provider.X509CertificatePemProvider.class);
     register(com.intel.mtwilson.jersey.provider.X509CertificateDerProvider.class);
     register(com.intel.mtwilson.jersey.provider.X509CertificateArrayPemProvider.class);
     register(com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper.class);
     register(com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper.class); 
     register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class); // added 
     //register(com.fasterxml.jackson.jaxrs.json.JsonParseExceptionMapper.class);
     register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
     //register(com.fasterxml.jackson.jaxrs.json.JsonMappingExceptionMapper.class);
     register(com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider.class); 
     //register(com.fasterxml.jackson.jaxrs.xml.JsonParseExceptionMapper.class); 
     register(com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider.class); 
     //register(com.fasterxml.jackson.jaxrs.xml.JsonMappingExceptionMapper.class);
     * 
     */
    
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public Client getClient() {
        return client;

    }

    public WebTarget getTarget() {
        return target;
    }

    public WebTarget getTargetPath(String path) {
        return target.path(path);
    }
    
    public WebTarget getTargetWithQueryParams(Object bean) {
        return addQueryParams(getTarget(), bean);
    }

    public WebTarget getTargetPathWithQueryParams(String path, Object bean) {
        return addQueryParams(getTarget().path(path), bean);
    }
    
    protected WebTarget addQueryParams(WebTarget target, Object bean) {
        try {
            Map<String, Object> properties = ReflectionUtil.getQueryParams(bean);
            for (Map.Entry<String, Object> queryParam : properties.entrySet()) {
                if (queryParam.getValue() == null) {
                    continue;
                }
//                log.debug("queryParam {} = {}", queryParam.getKey(), queryParam.getValue()); // for example: queryParam nameContains = test
                target = target.queryParam(queryParam.getKey(), queryParam.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate query parameters", e);
        }
//        log.debug("with query params: {}", target.getUri().toString()); // for example: with query params: http://localhost:8080/v2/files?nameContains=test
        return target;
    }
}
