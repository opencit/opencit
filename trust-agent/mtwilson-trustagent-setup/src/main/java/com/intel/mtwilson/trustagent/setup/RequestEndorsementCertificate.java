/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.CompositeConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.ProvisionTPM;
import com.intel.mtwilson.trustagent.niarl.Util;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
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

/**
 *
 * @author jbuhacoff
 */
public class RequestEndorsementCertificate extends AbstractSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestEndorsementCertificate.class);
    private TrustagentConfiguration config;
    private File keystoreFile;
    private SimpleKeystore keystore;
    private String tpmOwnerSecretHex;
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

        tpmOwnerSecretHex = config.getTpmOwnerSecretHex(); // we check it here because ProvisionTPM calls getOwnerSecret() which relies on this
        if (tpmOwnerSecretHex == null) {
            configuration("TPM Owner Secret is not configured: %s", TrustagentConfiguration.TPM_OWNER_SECRET); // this constant is the name of the property, literally "tpm.owner.secret"
        }
        if (!Util.isOwner(config.getTpmOwnerSecret())) {
            configuration("Trust Agent is not the TPM owner");
        }
        keystoreFile = config.getTrustagentKeystoreFile();
        if (keystoreFile.exists()) {
            keystore = new SimpleKeystore(new FileResource(keystoreFile), config.getTrustagentKeystorePassword());
            try {
                X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
                log.debug("Endorsement CA {}", Sha1Digest.digestOf(endorsementCA.getEncoded()).toHexString());
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
                configuration("Endorsement CA certificate cannot be loaded");
            }
        } else {
            configuration("Keystore file is missing");
        }
    }

    @Override
    protected void validate() throws Exception {
        byte[] ekCertBytes;
        try {
            ekCertBytes = TpmModule.getCredential(config.getTpmOwnerSecret(), "EC");
        } catch (TpmModuleException e) {
            if (e.getErrorCode() != null) {
                switch (e.getErrorCode()) {
                    case 1:
                        validation("Incorrect TPM owner password");
                        break;
                    case 2:
                        validation("Endorsement certificate needs to be requested");
                        break;
                    default:
                        validation("Error code %d while validating EC", e.getErrorCode());
                }
            }
            return;
        }
        X509Certificate ekCert = X509Util.decodeDerCertificate(ekCertBytes);
        X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
        try {
            ekCert.verify(endorsementCA.getPublicKey());
        } catch (SignatureException e) {
            validation("Known Endorsement CA did not sign TPM EC", e);
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            validation("Unable to verify TPM EC", e);
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
        
        ProvisionTPM provisioner = new ProvisionTPM();
        provisioner.setConfiguration(config.getConfiguration());
        provisioner.run();
    }
}
