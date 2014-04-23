/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.CompositeConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.CreateIdentity;
import com.intel.mtwilson.trustagent.niarl.Util;
import gov.niarl.his.privacyca.TpmModule;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jbuhacoff
 */
public class RequestAikCertificate extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestAikCertificate.class);
    private TrustagentConfiguration config;
    private SimpleKeystore keystore;
    private X509Certificate privacyCA;
    private String url;
    private String username;
    private String password;
    
    @Override
    protected void configure() throws Exception {
        config = new TrustagentConfiguration(getConfiguration());
        
        url = config.getMtWilsonApiUrl();
        username = config.getMtWilsonApiUsername();
        password = config.getMtWilsonApiPassword();
        if (url == null || url.isEmpty()) {
            configuration("Mt Wilson URL [mtwilson.api.url] must be set");
        }
        if (username == null || username.isEmpty()) {
            configuration("Mt Wilson username [mtwilson.api.username] must be set");
        }
        if (password == null || password.isEmpty()) {
            configuration("Mt Wilson password [mtwilson.api.password] must be set");
        }
        
        if( config.getTrustagentKeystoreFile().exists() ) {
        keystore = new SimpleKeystore(new FileResource(config.getTrustagentKeystoreFile()), config.getTrustagentKeystorePassword());
        // TODO:  the SimpleKeystore needs an api for checking if a certificate exists and returning null if it doesn't
        //        exist, instead of an exception , like findCertificate,  so that we don't need to use a try/catch block
        //        if we want to check its existence before using it, as opposed to just trying to use it and throwing
        //        an exception if it's missing. 
        try {
            privacyCA = keystore.getX509Certificate("privacy", SimpleKeystore.CA);
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            log.debug("Cannot load Privacy CA certificate", e);
            configuration("Privacy CA certificate is missing");
        }
        }
        else {
            configuration("Keystore file is missing");
        }
        if (!Util.isOwner(config.getTpmOwnerSecret())) {
            configuration("Trust Agent is not the TPM owner");
            return;
        }
        
        // we need an EC in order to request an AIK, so make sure we have it
        try {
            byte[] ekCert = TpmModule.getCredential(config.getTpmOwnerSecret(), "EC");
        }
        catch(TpmModule.TpmModuleException e) {
            if( e.getErrorCode() == 2 ) {
                configuration("Endorsement Certificate is missing");
            }
            else {
                configuration("Cannot determine presence of Endorsement Certificate: %s", e.getMessage());
            }
        }
    }

    @Override
    protected void validate() throws Exception {
        File aikCertificateFile = config.getAikCertificateFile();
        if( !aikCertificateFile.exists() ) {
            validation("AIK has not been created");
            return;
        }
        
        X509Certificate aikCertificate = X509Util.decodePemCertificate(FileUtils.readFileToString(aikCertificateFile));
        try {
            aikCertificate.verify(privacyCA.getPublicKey());
        }
        catch(SignatureException e) {
            validation("Known Privacy CA did not sign AIK", e);
        }
        catch(CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            validation("Unable to verify AIK", e);
        }
    }

    @Override
    protected void execute() throws Exception {
        /*
        // TODO:  this should be consolidated in the v2 client abstract class  with use of TlsPolicyManager ; see also RequestEndorsementCertificat e and RequestAikCertificate
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        */
        CreateIdentity provisioner = new CreateIdentity();
        provisioner.setConfiguration(config.getConfiguration());
        provisioner.run();
    }
    
}
