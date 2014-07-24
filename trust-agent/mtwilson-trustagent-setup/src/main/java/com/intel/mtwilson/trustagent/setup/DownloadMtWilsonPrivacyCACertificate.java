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
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.attestation.client.jaxrs.CaCertificates;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

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
    private File endorsementAuthoritiesFile;
    
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
        endorsementAuthoritiesFile = trustagentConfiguration.getEndorsementAuthoritiesFile();
        if( endorsementAuthoritiesFile == null ) {
            configuration("Endorsement authorities file location is not set");
        }
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
        try {
            /*
            X509Certificate endorsementCertificate = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
            if( endorsementCertificate == null ) {
                validation("Missing Endorsement CA certificate");
            }
            if( endorsementCertificate != null ) {
                log.debug("Found Endorsement CA certificate {}", Sha1Digest.digestOf(endorsementCertificate.getEncoded()).toHexString());
            }
            */
            if( endorsementAuthoritiesFile.exists() ) {
                try(InputStream in = new FileInputStream(endorsementAuthoritiesFile)) {
                    String pem = IOUtils.toString(in);
                    List<X509Certificate> endorsementAuthorities = X509Util.decodePemCertificates(pem);
                    log.debug("Found {} endorsement authorities in {}", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
                }
            }
            else {
                validation("Missing endorsement authorities file");
            }
            
        }
        catch(IOException e) {
            validation(e, "Cannot read endorsement authorities file");
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
        keystore.save();
        */
        
        // we create or replace our endorsement.pem file with what mtwilson provides
        // because it's mtwilson that will be evaluating it anyway in order to 
        // issue AIK certiicates later
        CaCertificateFilterCriteria criteria = new CaCertificateFilterCriteria();
        criteria.domain = "ek"; // or "endorsement"
        String endorsementAuthoritiesPem = client.searchCaCertificatesPem(criteria);
        try(OutputStream out = new FileOutputStream(endorsementAuthoritiesFile)) {
            IOUtils.write(endorsementAuthoritiesPem, out);
        }        
        
    }
    
}
