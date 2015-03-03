/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.as.rest.v2.model.BindingKeyEndorsementRequest;
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
public class CertifyBindingKey extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertifyBindingKey.class);
    
    private TrustagentConfiguration trustagentConfiguration;
    private File bindingKeyPem;    
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;
    private File bindingKeyModulus;
    private File bindingKeyTCGCertificate;
    private File bindingKeyTCGCertificateSignature;
    private File aikPemCertificate;
    
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
        
        bindingKeyPem = trustagentConfiguration.getBindingKeyX509CertificateFile();
    }

    @Override
    protected void validate() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        
        // Now check for the existence of the MTW signed PEM file.
        if (bindingKeyPem == null || !bindingKeyPem.exists()) {
            validation("MTW signed Binding Key certificate does not exist.");
        }        
    }

    @Override
    protected void execute() throws Exception {

        log.info("Calling into MTW to certify the TCG standard binding key");
        bindingKeyTCGCertificate = trustagentConfiguration.getBindingKeyTCGCertificateFile(); 
        bindingKeyModulus = trustagentConfiguration.getBindingKeyModulusFile();
        bindingKeyTCGCertificateSignature = trustagentConfiguration.getBindingKeyTCGCertificateSignatureFile();
        aikPemCertificate = trustagentConfiguration.getAikCertificateFile();
                
        log.debug("TCG Cert path is : {}", bindingKeyTCGCertificate.getAbsolutePath());
        log.debug("Public key modulus path is : {}", bindingKeyModulus.getAbsolutePath());
        log.debug("TCG Cert signature path is : {}", bindingKeyTCGCertificateSignature.getAbsolutePath());
        log.debug("AIK Certificate path is : {}", aikPemCertificate.getAbsolutePath());

        BindingKeyEndorsementRequest obj = new BindingKeyEndorsementRequest();
        obj.setPublicKeyModulus(FileUtils.readFileToByteArray(bindingKeyModulus));
        obj.setTpmCertifyKey(FileUtils.readFileToByteArray(bindingKeyTCGCertificate));
        obj.setTpmCertifyKeySignature(FileUtils.readFileToByteArray(bindingKeyTCGCertificateSignature));
        
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
        X509Certificate bindingKeyCertificate = client.createBindingKeyCertificate(obj);
        String pemCertificate = X509Util.encodePemCertificate(bindingKeyCertificate);
        log.debug("MTW signed PEM certificate is {} ", pemCertificate);
        
        FileUtils.writeStringToFile(bindingKeyPem, pemCertificate);
        log.debug("Successfully created the MTW signed X509Certificate for the binding key and stored at {}.", 
                bindingKeyPem.getAbsolutePath());
        
    }
}
