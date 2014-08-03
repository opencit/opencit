/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.client;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import java.net.URL;
import org.glassfish.jersey.client.ClientConfig;
import com.intel.mtwilson.security.http.jaxrs.HmacAuthorizationFilter;
import com.intel.mtwilson.security.http.jaxrs.X509AuthorizationFilter;
//import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature; //jersey 2.10.1
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter; //jersey 2.4.1
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import java.io.File;
import java.util.Properties;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyManager;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import java.util.logging.Logger;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.filter.LoggingFilter;
import com.intel.mtwilson.jaxrs2.feature.JacksonFeature;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
//import org.glassfish.jersey.client.HttpUrlConnectorProvider; // jersey 2.10.1
import org.glassfish.jersey.client.HttpUrlConnector; // jersey 2.4.1

/**
 * Examples:
 *
 * <pre>
 * JaxrsClient client = JaxrsClientBuilder.factory().url(url).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().configuration(properties).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().configuration(configuration).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().tlsConnection(tlsConnection).build();
 * JaxrsClient client = JaxrsClientBuilder.factory().url(url).tlsPolicy(tlsPolicy).build();
 * </pre>
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
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.JacksonXmlMapperProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class);
//        clientConfig.register(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);        
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.X509CertificateArrayPemProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.X509CertificateDerProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.X509CertificatePemProvider.class);
        clientConfig.register(com.intel.mtwilson.jaxrs2.provider.DateParamConverterProvider.class);
    }

    /**
     * Configures a client using authentication settings in the properties
     * argument. The API URL must be set as mtwilson.api.url or
     * mtwilson.api.baseurl in the properties.
     *
     * To use BASIC password authentication, set mtwilson.api.username and
     * mtwilson.api.password.
     *
     * To use HMAC (MtWilson-specific) authentication, set mtwilson.api.clientId
     * and mtwilson.api.secretKey
     *
     * To use X509 (MtWilson-specific) authentication, set:
     * mtwilson.api.keystore = path to client-keystore.jks
     * mtwilson.api.keystore.password = password protecting client-keystore.jks
     * mtwilson.api.key.alias = alias of private key in the keystore; usually
     * same as username or name of keystore like "client-keystore"
     * mtwilson.api.key.password = password protecting the key, usually same as
     * the keystore password
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

    private void authentication() throws KeyManagementException, FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException, CryptographyException {
        if (configuration == null) {
            return;
        }
        // X509 authorization 
        SimpleKeystore keystore = null;
        if (configuration.getString("mtwilson.api.keystore") != null && configuration.getString("mtwilson.api.keystore.password") != null) {
            FileResource resource = new FileResource(new File(configuration.getString("mtwilson.api.keystore")));
            keystore = new SimpleKeystore(resource, configuration.getString("mtwilson.api.keystore.password"));
        }
        if (keystore != null && configuration.getString("mtwilson.api.key.alias") != null && configuration.getString("mtwilson.api.key.password") != null) {
            log.debug("Registering X509 credentials for {}", configuration.getString("mtwilson.api.key.alias"));
            log.debug("Loading key {} from keystore {}", configuration.getString("mtwilson.api.key.alias"), configuration.getString("mtwilson.api.keystore"));
            RsaCredentialX509 credential = keystore.getRsaCredentialX509(configuration.getString("mtwilson.api.key.alias"), configuration.getString("mtwilson.api.key.password"));
            log.debug(credential.getPublicKey().toString());
            clientConfig.register(new X509AuthorizationFilter(credential));
        }
        // HMAC authorization
        if (configuration.getString("mtwilson.api.clientId") != null && configuration.getString("mtwilson.api.secretKey") != null) {
            log.debug("Registering HMAC credentials for {}", configuration.getString("mtwilson.api.clientId"));
            clientConfig.register(new HmacAuthorizationFilter(configuration.getString("mtwilson.api.clientId"), configuration.getString("mtwilson.api.secretKey")));
        }
        // BASIC authorization will only be registered if configuration is present but also the feature itself will only add an Authorization header if there isn't already one present
        if (configuration.getString("mtwilson.api.username") != null && configuration.getString("mtwilson.api.password") != null) {
            log.debug("Registering BASIC credentials for {}", configuration.getString("mtwilson.api.username"));
//            clientConfig.register( new BasicPasswordAuthorizationFilter(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password")));
//            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password"));
//            clientConfig.register(feature);

            clientConfig.register(new HttpBasicAuthFilter(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password"))); // jersey 2.4.1
//            clientConfig.register(HttpAuthenticationFeature.basic(configuration.getString("mtwilson.api.username"), configuration.getString("mtwilson.api.password"))); // jersey 2.10.1
        }
    }

    private void url() throws MalformedURLException {
        if (url == null) {
            if (configuration != null) {
                url = new URL(configuration.getString("mtwilson.api.url", configuration.getString("mtwilson.api.baseurl"))); // example: "http://localhost:8080/v2";
            }
        }
    }

    private void tls() {
        assert url != null;
        if (tlsConnection == null) {
            if (tlsPolicy != null) {
                log.debug("creating TlsConnection from URL and TlsPolicy");
                tlsConnection = new TlsConnection(url, tlsPolicy);
            } else if (configuration != null) {
                tlsPolicy = PropertiesTlsPolicyFactory.createTlsPolicy(configuration);
                log.debug("TlsPolicy is {}", this.tlsPolicy.getClass().getName());
                tlsConnection = new TlsConnection(url, tlsPolicy);
                log.debug("set TlsConnection from configuration");
            }
        }
        if (tlsConnection != null) {
//            log.debug("setting HttpUrlConnector with TlsPolicyAwareConnectionFactory");
            clientConfig.connector(new HttpUrlConnector(clientConfig, new TlsPolicyAwareConnectionFactory(tlsConnection.getTlsPolicy())));  // jersey 2.4.1
//            clientConfig.connectorProvider(new HttpUrlConnectorProvider().connectionFactory(new TlsPolicyAwareConnectionFactory(tlsConnection)));
//            log.debug("setting HttpsURLConnection defaults");
//            TlsUtil.setHttpsURLConnectionDefaults(tlsConnection);
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

    public JaxrsClient build() {
        try {
            url();
            tls(); // sets tls connection
            authentication(); // adds to clientConfig
//        client = ClientBuilder.newClient(clientConfig);
//            Client client = ClientBuilder.newBuilder().sslContext(tlsConnection.getSSLContext()).hostnameVerifier(tlsConnection.getTlsPolicy().getHostnameVerifier()).withConfig(clientConfig).build();
            Client client = ClientBuilder.newBuilder()
                    .withConfig(clientConfig)
                    .sslContext(tlsConnection.getSSLContext()) // when commented out,  get pkix path building failure from java's built-in ssl context... when enabled, our custom ssl context doesn't get called at all.
//                    .hostnameVerifier(TlsPolicyManager.getInstance().getHostnameVerifier())
                    .hostnameVerifier(tlsConnection.getTlsPolicy().getHostnameVerifier())
                    .build();
            if (configuration != null && configuration.getBoolean("org.glassfish.jersey.filter.LoggingFilter.printEntity", true)) {
                client.register(new LoggingFilter(Logger.getLogger("org.glassfish.jersey.filter.LoggingFilter"), true));
            } else {
                client.register(new LoggingFilter());
            }
//        client.register(com.intel.mtwilson.jaxrs2.provider.JacksonXmlMapperProvider.class); 
//        client.register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class);
            WebTarget target = client.target(url.toExternalForm());

            return new JaxrsClient(client, target);
        } catch (MalformedURLException | KeyManagementException | FileNotFoundException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException | CryptographyException e) {
            throw new IllegalArgumentException("Cannot construct client", e);
        }
    }
    /*
     register(com.intel.mtwilson.jaxrs2.provider.JacksonXmlMapperProvider.class); 
     register(com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider.class); // added
     register(com.intel.mtwilson.jaxrs2.provider.JacksonYamlObjectMapperProvider.class);
     register(com.intel.mtwilson.jaxrs2.provider.ApplicationYamlProvider.class);
     register(com.intel.mtwilson.jaxrs2.provider.X509CertificatePemProvider.class);
     register(com.intel.mtwilson.jaxrs2.provider.X509CertificateDerProvider.class);
     register(com.intel.mtwilson.jaxrs2.provider.X509CertificateArrayPemProvider.class);
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
