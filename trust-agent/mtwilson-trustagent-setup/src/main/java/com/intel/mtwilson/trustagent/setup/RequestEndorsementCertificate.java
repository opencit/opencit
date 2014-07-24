/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.ProvisionTPM;
import com.intel.mtwilson.trustagent.niarl.Util;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

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
    private File endorsementAuthoritiesFile;

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
        
        endorsementAuthoritiesFile = config.getEndorsementAuthoritiesFile();
        if( endorsementAuthoritiesFile == null ) {
            configuration("Endorsement authorities file location is not set");
        }
        else if( !endorsementAuthoritiesFile.exists() ) {
            configuration("Endorsement authorities file does not exist");
        }
        else {
            try(InputStream in = new FileInputStream(endorsementAuthoritiesFile)) {
                String pem = IOUtils.toString(in);
                List<X509Certificate> endorsementAuthorities = X509Util.decodePemCertificates(pem);
                log.debug("Found {} endorsement authorities in {}", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
            }
            catch(Exception e) {
                configuration(e, "Cannot read endorsement authorities file");
            }
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
            log.debug("Failed to get EC from TPM using NIARL_TPM_Module");
            // try with tpm tools 
            //   /root/tpm-tools-1.3.8-patched/src/tpm_mgmt/tpm_getpubek
            //    that gets the EK modulus  but we still need the EC:
            // tpm_nvinfo -i 0x1000f000
            // tpm_nvread -i 0x1000f000 -x -t -pOWNER_AUTH -s 834 -f mfr.crt.tpm
            // openssl x509 -in mfr.crt.tpm -inform der -text   (works for Nuvoton, will not work for some others if they wrapped EC in a TCG structure... then have to use dd to remove initial bytes, and sometimes do other corrections for invalid length fields)
            
            
            return;
        }
        log.debug("EC base64: {}", Base64.encodeBase64String(ekCertBytes));
        X509Certificate ekCert = X509Util.decodeDerCertificate(ekCertBytes);
        
        /*
        X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
        try {
            ekCert.verify(endorsementCA.getPublicKey());
        } catch (SignatureException e) {
            validation("Known Endorsement CA did not sign TPM EC", e);
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            validation("Unable to verify TPM EC", e);
        }
        */
        
        try(InputStream in = new FileInputStream(endorsementAuthoritiesFile)) {
            String pem = IOUtils.toString(in);
            List<X509Certificate> endorsementAuthorities = X509Util.decodePemCertificates(pem);
            log.debug("Found {} endorsement authorities in {}", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
            // if we find one certificate authority that can verify our current EC, then we don't need to request a new EC
            boolean found = false;
            for(X509Certificate ca : endorsementAuthorities) {
                try {
                    log.debug("Trying to verify EC with {}", ca.getSubjectX500Principal().getName());
                    ekCert.verify(ca.getPublicKey());
                    found = true;
                } catch (SignatureException e) {
                    log.debug("Endorsement CA '{}' did not sign TPM EC: {}", ca.getSubjectX500Principal().getName(), e.getMessage());
                } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
                    log.debug("Unable to verify TPM EC '{}': {}", ca.getSubjectX500Principal().getName(), e.getMessage());
                }
            }
            if(!found) {
                validation("Unable to verify TPM EC with %d authorities", endorsementAuthorities.size());
            }
        }
        
    }

    @Override
    protected void execute() throws Exception {
        /*
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        */
        
        ProvisionTPM provisioner = new ProvisionTPM();
        provisioner.configure(config.getConfiguration());
        log.debug("skipping provision tpm action");
        provisioner.run();
    }
}
