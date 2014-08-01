/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.attestation.client.jaxrs.CaCertificates;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * Prerequisites:  Trust Agent Keystore must already be created
 * 
 * @author jbuhacoff
 */
public class DownloadMtWilsonPrivacyCACertificate extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DownloadMtWilsonPrivacyCACertificate.class);

    private TrustagentConfiguration trustagentConfiguration;
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;
    private SimpleKeystore keystore;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        url = trustagentConfiguration.getMtWilsonApiUrl();
        if( url == null || url.isEmpty() ) {
            configuration("Mt Wilson URL is not set");
        }
        username = trustagentConfiguration.getMtWilsonApiUsername();
        password = trustagentConfiguration.getMtWilsonApiPassword();
        if( username == null || username.isEmpty() ) {
            configuration("Mt Wilson username is not set");
        }
        if( password == null || password.isEmpty() ) {
            configuration("Mt Wilson password is not set");
        }
        keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        if( keystoreFile == null || !keystoreFile.exists() ) {
            configuration("Trust Agent keystore does not exist");
        }
        keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        if( keystorePassword == null || keystorePassword.isEmpty() ) {
            configuration("Trust Agent keystore password is not set");
        }
        keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
    }

    @Override
    protected void validate() throws Exception {
        try {
            X509Certificate certificate = keystore.getX509Certificate("privacy", SimpleKeystore.CA);
            if( certificate == null ) {
                validation("Missing Privacy CA certificate");
            }
            if( certificate != null ) {
                log.debug("Found Privacy CA certificate {}", Sha1Digest.digestOf(certificate.getEncoded()).toHexString());
            }
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            log.debug("Cannot load Privacy CA certificate", e);
            validation("Cannot load Privacy CA certificate", e);
        }
    }

    @Override
    protected void execute() throws Exception {
        /*
        System.setProperty("javax.net.ssl.trustStore", trustagentConfiguration.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", trustagentConfiguration.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", trustagentConfiguration.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", trustagentConfiguration.getTrustagentKeystorePassword());
        */
        log.debug("Creating TLS policy");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), trustagentConfiguration.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);
        
        CaCertificates client = new CaCertificates(clientConfiguration, tlsConnection);
        X509Certificate certificate = client.retrieveCaCertificate("privacy");
        keystore.addTrustedCaCertificate(certificate, "privacy");
        /*
        X509Certificate endorsementCertificate = client.retrieveCaCertificate("endorsement");
        keystore.addTrustedCaCertificate(endorsementCertificate, "endorsement");
        */
        keystore.save();
    }
    
}
