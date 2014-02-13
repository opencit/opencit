/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.as.rest.v2.model.FileCollection;
import java.io.File;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import java.util.HashMap;
import com.intel.mtwilson.security.http.jaxrs.HmacAuthorizationFilter;
import com.intel.mtwilson.security.http.jaxrs.X509AuthorizationFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.util.Properties;
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
    
    public MtWilsonClient() {
        baseurl = "http://localhost:8080/v2";
        clientConfig = new ClientConfig();
        clientConfig.register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
        clientConfig.register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
        clientConfig.register(new HmacAuthorizationFilter("username", "password"));
        client = ClientBuilder.newClient(clientConfig);
        target = client.target(baseurl);
    }

    public MtWilsonClient(Properties properties) throws KeyManagementException, IOException, CryptographyException, GeneralSecurityException {
        baseurl = properties.getProperty("mtwilson.api.baseurl"); // example: "http://localhost:8080/v2";
        clientConfig = new ClientConfig();
        clientConfig.register(com.intel.mtwilson.jersey.provider.JacksonObjectMapperProvider.class);
        clientConfig.register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
        // HMAC authorization
        if( properties.containsKey("mtwilson.api.clientId") && properties.containsKey("mtwilson.api.secretKey")) {
            clientConfig.register( new HmacAuthorizationFilter(properties.getProperty("mtwilson.api.clientId"), properties.getProperty("mtwilson.api.secretKey")));
        }
        // X509 authorization 
        SimpleKeystore keystore = null;
        if( properties.containsKey("mtwilson.api.keystore") && properties.containsKey("mtwilson.api.keystore.password") ) {
            FileResource resource = new FileResource(new File(properties.getProperty("mtwilson.api.keystore")));
            keystore = new SimpleKeystore(resource, properties.getProperty("mtwilson.api.keystore.password"));
        }
        if( keystore != null && properties.containsKey("mtwilson.api.key.alias") && properties.containsKey("mtwilson.api.key.password") ) {
            RsaCredentialX509 credential = keystore.getRsaCredentialX509(properties.getProperty("mtwilson.api.key.alias"), properties.getProperty("mtwilson.api.key.password"));
            clientConfig.register( new X509AuthorizationFilter(credential));
        }
        client = ClientBuilder.newClient(clientConfig);
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
    
}
