/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.as.rest.v2.model.BindingKeyEndorsementRequest;
import com.intel.mtwilson.attestation.client.jaxrs.HostTpmKeys;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.util.Properties;

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
        
    }

    @Override
    protected void validate() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        
        // Now check for the existence of the MTW signed PEM file.
        bindingKeyPem = trustagentConfiguration.getBindingKeyX509CertificateFile();
        if (bindingKeyPem == null || !bindingKeyPem.exists()) {
            validation("MTW signed Binding Key certificate does not exist.");
        }        
    }

    @Override
    protected void execute() throws Exception {

        log.info("Calling into MTW to certify the TCG standard binding key");
        String tcgCertPath = trustagentConfiguration.getBindingKeyTCGCertificateFile().getAbsolutePath(); 
        String pubKeyModulus = trustagentConfiguration.getBindingKeyModulusFile().getAbsolutePath();
        
        log.debug("TCG Cert path is : {}", tcgCertPath);
        log.debug("Public key modulus path is : {}", pubKeyModulus);

        BindingKeyEndorsementRequest obj = new BindingKeyEndorsementRequest();
        obj.setPublicKeyModulus(SetupUtils.readblob(pubKeyModulus));
        obj.setTpmCertifyKey(SetupUtils.readblob(tcgCertPath));
        
        log.debug("Creating TLS policy");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), 
                trustagentConfiguration.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);
        
        HostTpmKeys client = new HostTpmKeys(clientConfiguration, tlsConnection);
        String bindingKeyPemCertificate = client.createBindingKeyCertificate(obj);
        log.debug("MTW signed PEM certificate is {} ", bindingKeyPemCertificate);
        
        SetupUtils.writeString(trustagentConfiguration.getBindingKeyX509CertificateFile().getAbsolutePath(), bindingKeyPemCertificate);
        log.info("Successfully created the MTW signed X509Certificate for the binding key and stored at {}.", 
                trustagentConfiguration.getSigningKeyX509CertificateFile().getAbsolutePath());
        
    }
}
