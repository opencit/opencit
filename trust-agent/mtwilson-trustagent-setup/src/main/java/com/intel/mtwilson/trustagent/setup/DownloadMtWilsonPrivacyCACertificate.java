/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.impl.AnyProtocolSelector;
import com.intel.mtwilson.client.jaxrs.CaCertificates;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

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
            X509Certificate certificate = keystore.getX509Certificate("privacy");
            if( certificate == null ) {
                validation("Missing Privacy CA certificate");
            }
            if( certificate != null ) {
                log.debug("Found Privacy CA certificate {}", Sha1Digest.valueOf(certificate.getEncoded()).toHexString());
            }
            X509Certificate endorsementCertificate = keystore.getX509Certificate("endorsement");
            if( endorsementCertificate == null ) {
                validation("Missing Endorsement CA certificate");
            }
            if( endorsementCertificate != null ) {
                log.debug("Found Endorsement CA certificate {}", Sha1Digest.valueOf(endorsementCertificate.getEncoded()).toHexString());
            }
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            validation("Cannot load Privacy CA certificate", e);
        }
    }

    @Override
    protected void execute() throws Exception {
        CaCertificates client = new CaCertificates(trustagentConfiguration.getConfiguration());
        X509Certificate certificate = client.retrieveCaCertificate("privacy");
        keystore.addTrustedCaCertificate(certificate, "privacy");
        X509Certificate endorsementCertificate = client.retrieveCaCertificate("endorsement");
        keystore.addTrustedCaCertificate(endorsementCertificate, "endorsement");
        keystore.save();
    }
    
}
