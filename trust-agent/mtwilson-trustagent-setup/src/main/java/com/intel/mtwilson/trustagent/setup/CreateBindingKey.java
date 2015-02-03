/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.as.rest.v2.model.BindingKeyEndorsementRequest;
import com.intel.mtwilson.attestation.client.jaxrs.HostTpmKeys;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.TpmCertifyKey;
import gov.niarl.his.privacyca.TpmModule;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ssbangal
 */
public class CreateBindingKey extends AbstractSetupTask {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateBindingKey.class);
    private TrustagentConfiguration trustagentConfiguration;
    private File bindingKeyBlob;
    private File bindingKeyModulus;
    private File bindingKeyTCGCertificate;
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
//        if( username == null || username.isEmpty() ) {
//            configuration("Mt Wilson username is not set");
//        }
//        if( password == null || password.isEmpty() ) {
//            configuration("Mt Wilson password is not set");
//        }
        
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
        String bindingKeySecretHex = trustagentConfiguration.getBindingKeySecretHex();
        if( bindingKeySecretHex == null || bindingKeySecretHex.isEmpty() ) {
            validation("Binding key secret is not set");
        }
        
        // Now check for the existence of the binding private/public key and the tcg standard binding certificate from the 
        // certifyKey output.
        bindingKeyBlob = trustagentConfiguration.getBindingKeyBlobFile();
        if (bindingKeyBlob == null || !bindingKeyBlob.exists()) {
            validation("Private component of binding key does not exist.");
        }

        bindingKeyTCGCertificate = trustagentConfiguration.getBindingKeyTCGCertificateFile();
        if (bindingKeyTCGCertificate == null || !bindingKeyTCGCertificate.exists()) {
            validation("TCG standard certificate for the binding key does not exist.");
        }

        bindingKeyModulus = trustagentConfiguration.getBindingKeyModulusFile();
        if (bindingKeyModulus == null || !bindingKeyModulus.exists()) {
            validation("Public component of binding key does not exist.");
        }
        
    }

    @Override
    protected void execute() throws Exception {
        String bindingKeySecretHex = RandomUtil.randomHexString(20);
        log.info("Generated random Binding key secret"); 
        
        getConfiguration().setString(TrustagentConfiguration.BINDING_KEY_SECRET, bindingKeySecretHex);
        
        // Call into the TpmModule certifyKey function to create the binding key and certify the same.
        HashMap<String, byte[]> certifyKey = TpmModule.certifyKey("bind", trustagentConfiguration.getBindingKeySecret(), 3, 
                trustagentConfiguration.getAikSecret(), trustagentConfiguration.getAikIndex());
        
        String blobPath = trustagentConfiguration.getBindingKeyBlobFile().getAbsolutePath();
        String tcgCertPath = trustagentConfiguration.getBindingKeyTCGCertificateFile().getAbsolutePath(); 
        String pubKeyModulus = trustagentConfiguration.getBindingKeyModulusFile().getAbsolutePath();
        
        log.debug("Blob path is : {}", blobPath);
        log.debug("TCG Cert path is : {}", tcgCertPath);
        log.debug("Public key modulus path is : {}", pubKeyModulus);
        
        writeblob(pubKeyModulus, certifyKey.get("keymod"));
        writeblob(blobPath, certifyKey.get("keyblob"));
        writeblob(tcgCertPath, certifyKey.get("keydata"));
        
        TpmCertifyKey tpmCertifyKey = new TpmCertifyKey(certifyKey.get("keydata"));
        log.debug("Key contents: {} - {}", tpmCertifyKey.getKeyParms().getAlgorithmId(), tpmCertifyKey.getKeyParms().getTrouSerSmode());

        BindingKeyEndorsementRequest obj = new BindingKeyEndorsementRequest();
        obj.setPublicKeyModulus(certifyKey.get("keymod"));
        obj.setTpmCertifyKey(certifyKey.get("keydata"));
        
        log.debug("Creating TLS policy");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), 
                trustagentConfiguration.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, "admin");
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, "password");
        
        HostTpmKeys client = new HostTpmKeys(clientConfiguration, tlsConnection);
        String bindingKeyPemCertificate = client.createBindingKeyCertificate(obj);
        log.debug("MTW signed PEM certificate is {} ", bindingKeyPemCertificate);
        
        writeString(trustagentConfiguration.getBindingKeyX509CertificateFile().getAbsolutePath(), bindingKeyPemCertificate);
        
    }
    
    // given a File, ensures that its parent directory exists, creating it if necessary, and throwing PrivacyCAException 
    // if there is a failure
    private static void mkdir(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                log.warn("Failed to create client installation path!");
                throw new IOException("Failed to create client installation path!");
            }
        }
    }

    private static void writeblob(String absoluteFilePath, byte[] encryptedBytes) throws IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(encryptedBytes, out); // throws IOException
        }
    }    

    private static void writeString(String absoluteFilePath, String data) throws IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(data, out); // throws IOException
        }
    }    
    
}
