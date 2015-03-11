/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.as.rest.v2.model.SigningKeyEndorsementRequest;
import com.intel.mtwilson.attestation.client.jaxrs.HostTpmKeys;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ssbangal
 */
public class CertifySigningKey extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifySigningKey.class);
    
    private TrustagentConfiguration trustagentConfiguration;
    private File signingKeyPem;
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;
    private File aikPemCertificate;
    private File signingKeyModulus;
    private File signingKeyTCGCertificate;
    private File signingKeyTCGCertificateSignature;
    
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
        
        signingKeyPem = trustagentConfiguration.getSigningKeyX509CertificateFile();
    }

    @Override
    protected void validate() throws Exception {
        
        // Now check for the existence of the MTW signed PEM file.
        if (signingKeyPem == null || !signingKeyPem.exists()) {
            validation("MTW signed Signing Key certificate does not exist.");
        }        
    }

    @Override
    protected void execute() throws Exception {

        aikPemCertificate = trustagentConfiguration.getAikCertificateFile();
        signingKeyTCGCertificate = trustagentConfiguration.getSigningKeyTCGCertificateFile(); 
        signingKeyModulus = trustagentConfiguration.getSigningKeyModulusFile();
        signingKeyTCGCertificateSignature = trustagentConfiguration.getSigningKeyTCGCertificateSignatureFile();
        
        log.debug("AIK Cert path is : {}", aikPemCertificate.getAbsolutePath());
        log.debug("TCG Cert path is : {}", signingKeyTCGCertificate.getAbsolutePath());
        log.debug("TCG Cert signature path is : {}", signingKeyTCGCertificateSignature.getAbsolutePath());        
        log.debug("Public key modulus path is : {}", signingKeyModulus.getAbsolutePath());
                
        SigningKeyEndorsementRequest obj = new SigningKeyEndorsementRequest();
        obj.setPublicKeyModulus(FileUtils.readFileToByteArray(signingKeyModulus));
        obj.setTpmCertifyKey(FileUtils.readFileToByteArray(signingKeyTCGCertificate));
        obj.setTpmCertifyKeySignature(FileUtils.readFileToByteArray(signingKeyTCGCertificateSignature)); 
        
        X509Certificate aikCert = X509Util.decodePemCertificate(FileUtils.readFileToString(aikPemCertificate));
        byte[] encodedAikDerCertificate = X509Util.encodeDerCertificate(aikCert);
        obj.setAikDerCertificate(encodedAikDerCertificate);
        
        log.debug("Creating TLS policy");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), 
                trustagentConfiguration.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);
        
        HostTpmKeys client = new HostTpmKeys(clientConfiguration, tlsConnection);
        X509Certificate signingKeyCertificate = client.createSigningKeyCertificate(obj);
        String pemCertificate = X509Util.encodePemCertificate(signingKeyCertificate);
        log.debug("MTW signed PEM certificate is {} ", pemCertificate);
        
        FileUtils.writeStringToFile(signingKeyPem, pemCertificate);
        log.debug("Successfully created the MTW signed X509Certificate for the signing key and stored at {}.", 
                signingKeyPem.getAbsolutePath());
    }    
}
