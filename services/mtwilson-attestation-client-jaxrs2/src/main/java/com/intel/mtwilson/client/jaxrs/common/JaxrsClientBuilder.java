/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.jaxrs.common;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.net.URL;
import org.glassfish.jersey.client.ClientConfig;
import com.intel.mtwilson.security.http.jaxrs.HmacAuthorizationFilter;
import com.intel.mtwilson.security.http.jaxrs.X509AuthorizationFilter;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.security.KeyManagementException;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.filter.LoggingFilter;
import com.intel.mtwilson.jersey2.JacksonFeature;

/**
 *
 * @author jbuhacoff
 */
public class JaxrsClientBuilder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JaxrsClientBuilder.class);
    
    public static JaxrsClientBuilder factory() {
        return new JaxrsClientBuilder();
    }

    private ClientConfig clientConfig;
    private Configuration configuration;
    private TlsPolicy tlsPolicy;
    private URL url;
    private TlsConnection tlsConnection;
    
    public JaxrsClientBuilder() {
        clientConfig = new ClientConfig();
        clientConfig.register(JacksonFeature.class);        
        clientConfig.register(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class); 
        clientConfig.register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
//        clientConfig.register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);        
        clientConfig.register(com.intel.mtwilson.jersey.provider.X509CertificateArrayPemProvider.class);
        clientConfig.register(com.intel.mtwilson.jersey.provider.X509CertificateDerProvider.class);
        clientConfig.register(com.intel.mtwilson.jersey.provider.X509CertificatePemProvider.class);        
        clientConfig.register(com.intel.mtwilson.jersey.provider.DateParamConverterProvider.class);
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
     * @return 
     */
    public JaxrsClientBuilder configuration(Properties properties) {
        configuration = new PropertiesConfiguration(properties);
        return this;
    }
    public JaxrsClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }
    
    private void authentication() throws Exception {
        if( configuration == null ) { return; }
        // X509 authorization 
        SimpleKeystore keystore = null;
        if (configuration.getString("mtwilson.api.keystore") != null && configuration.getString("mtwilson.api.keystore.password") != null ) {
            FileResource resource = new FileResource(new File(configuration.getString("mtwilson.api.keystore")));
            keystore = new SimpleKeystore(resource, configuration.getString("mtwilson.api.keystore.password"));
        }
        if (keystore != null && configuration.getString("mtwilson.api.key.alias") != null && configuration.getString("mtwilson.api.key.password") != null ) {
            log.debug("Registering X509 credentials for {}", configuration.getString("mtwilson.api.key.alias"));
//            log.debug("Loading key {} from keystore {}", configuration.getString("mtwilson.api.key.alias"), configuration.getString("mtwilson.api.keystore"));
            RsaCredentialX509 credential = keystore.getRsaCredentialX509(configuration.getString("mtwilson.api.key.alias"), configuration.getString("mtwilson.api.key.password"));
            clientConfig.register(new X509AuthorizationFilter(credential));
        }
        // HMAC authorization
        if( configuration.getString("mtwilson.api.clientId") != null && configuration.getString("mtwilson.api.secretKey") != null) {
            log.debug("Registering HMAC credentials for {}", configuration.getString("mtwilson.api.clientId"));
            clientConfig.register( new HmacAuthorizationFilter(configuration.getString("mtwilson.api.clientId"), configuration.getString("mtwilson.api.secretKey")));
        }
        // BASIC authorization will only be registered if configuration is present but also the feature itself will only add an Authorization header if there isn't already one present
        if( configuration.getString("mtwilson.api.username") != null && configuration.getString("mtwilson.api.password") != null ) {
            log.debug("Registering BASIC credentials for {}", configuration.getString("mtwilson.api.username"));
//            clientConfig.register( new BasicPasswordAuthorizationFilter(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password")));
//            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password"));
//            clientConfig.register(feature);
            
            clientConfig.register( new HttpBasicAuthFilter(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password")));
        }
    }

    private void url() throws Exception {
        if( url == null ) {
            try {
                if( configuration != null ) {
                    url = new URL(configuration.getString("mtwilson.api.url", configuration.getString("mtwilson.api.baseurl"))); // example: "http://localhost:8080/v2";
                }
            } catch (Exception ex) {
                throw new IllegalStateException("URL must be set");
            }
        }
    }
    
    private void tls() {
        assert url != null;
        if( tlsConnection == null ) {
            if( tlsPolicy != null ) {
                log.debug("creating TlsConnection from URL and TlsPolicy");
                tlsConnection = new TlsConnection(url,tlsPolicy);
            }
            else {
                log.debug("creating TlsConnection with InsecureTlsPolicy");
                tlsConnection = new TlsConnection(url,new InsecureTlsPolicy());
            }
        }
    }
    
    // you can set this instead of url and tlsPolicy
    public JaxrsClientBuilder tlsConnection(TlsConnection tlsConnection) {
        log.debug("set TlsConnection");
        this.tlsConnection = tlsConnection;
        this.url = tlsConnection.getURL();
        this.tlsPolicy = tlsConnection.getTlsPolicy();
        log.debug("TlsPolicy is {}", this.tlsPolicy.getClass().getName());
        return this;
    }
    public JaxrsClientBuilder url(URL url) {
        this.url = url;
        return this;
    }
    public JaxrsClientBuilder tlsPolicy(TlsPolicy tlsPolicy) {
        this.tlsPolicy = tlsPolicy;
        return this;
    }
    
    public JaxrsClient build() throws Exception {
        url();
        tls(); // sets tls connection
        authentication(); // adds to clientConfig
//        client = ClientBuilder.newClient(clientConfig);
        // TODO: if URL is http and not https then we should skip the ssl configuration
        Client client = ClientBuilder.newBuilder().sslContext(tlsConnection.getSSLContext()).withConfig(clientConfig).build();
        if( configuration != null && configuration.getBoolean("org.glassfish.jersey.filter.LoggingFilter.printEntity", false) ) {
            client.register(new LoggingFilter(Logger.getLogger("org.glassfish.jersey.filter.LoggingFilter"), true));
        }
        else {
            client.register(new LoggingFilter());
        }
//        client.register(com.intel.mtwilson.jersey.provider.JacksonXmlMapperProvider.class); 
//        client.register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
        WebTarget target = client.target(url.toExternalForm());
        
        return new JaxrsClient(client, target);
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
    
    
}
