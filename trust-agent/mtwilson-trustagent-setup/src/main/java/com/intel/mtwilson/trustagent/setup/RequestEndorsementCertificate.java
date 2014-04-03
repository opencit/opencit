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
import java.util.Arrays;

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
    
    @Override
    protected void configure() throws Exception {
        config = new TrustagentConfiguration(getConfiguration());
        tpmOwnerSecretHex = config.getTpmOwnerSecretHex(); // we check it here because ProvisionTPM calls getOwnerSecret() which relies on this
        if( tpmOwnerSecretHex == null ) {
            configuration("TPM Owner Secret is not configured: "+TrustagentConfiguration.TPM_OWNER_SECRET); // this constant is the name of the property, literally "tpm.owner.secret"
        }
        keystoreFile = config.getTrustagentKeystoreFile();
        if( !keystoreFile.exists() ) {
            configuration("Keystore file is missing");
            return;
        }
        keystore = new SimpleKeystore(new FileResource(keystoreFile), config.getTrustagentKeystorePassword());
        try {
            X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
            log.debug("Endorsement CA {}", Sha1Digest.digestOf(endorsementCA.getEncoded()).toHexString());
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            configuration("Endorsement CA certificate cannot be loaded");
        }
    }

    @Override
    protected void validate() throws Exception {
        byte[] ekCertBytes = TpmModule.getCredential(config.getTpmOwnerSecret(), "EC");
        X509Certificate ekCert = X509Util.decodeDerCertificate(ekCertBytes);
        X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
        try {
            ekCert.verify(endorsementCA.getPublicKey());
        }
        catch(SignatureException e) {
            validation("Known Endorsement CA did not sign TPM EC", e);
        }
        catch(CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            validation("Unable to verify TPM EC", e);
        }
    }

    @Override
    protected void execute() throws Exception {
        // TODO:  this should be consolidated in the v2 client abstract class  with use of TlsPolicyManager ; see also RequestEndorsementCertificat e and RequestAikCertificate
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        
        ProvisionTPM provisioner = new ProvisionTPM();
        provisioner.setConfiguration(getConfiguration());
        provisioner.run();
    }
    
}
