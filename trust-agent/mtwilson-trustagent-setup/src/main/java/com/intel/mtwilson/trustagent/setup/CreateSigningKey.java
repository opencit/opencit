/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.TpmCertifyKey;
import gov.niarl.his.privacyca.TpmModule;
import java.io.File;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ssbangal
 */
public class CreateSigningKey extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateSigningKey.class);
    private TrustagentConfiguration trustagentConfiguration;
    private File signingKeyBlob;
    private File signingKeyModulus;
    private File signingKeyTCGCertificate;
    private File signingKeyTCGCertificateSignature;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());        
    }

    @Override
    protected void validate() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String signingKeySecretHex = trustagentConfiguration.getSigningKeySecretHex();
        if( signingKeySecretHex == null || signingKeySecretHex.isEmpty() ) {
            validation("Signing key secret is not set");
        }
        
        // Now check for the existence of the signing private/public key and the tcg standard signing certificate from the 
        // certifyKey output.
        signingKeyBlob = trustagentConfiguration.getSigningKeyBlobFile();
        if (signingKeyBlob == null || !signingKeyBlob.exists()) {
            validation("Private component of signing key does not exist.");
        }

        signingKeyTCGCertificate = trustagentConfiguration.getSigningKeyTCGCertificateFile();
        if (signingKeyTCGCertificate == null || !signingKeyTCGCertificate.exists()) {
            validation("TCG standard certificate for the signing key does not exist.");
        }

        signingKeyTCGCertificateSignature = trustagentConfiguration.getSigningKeyTCGCertificateSignatureFile();
        if (signingKeyTCGCertificateSignature == null || !signingKeyTCGCertificateSignature.exists()) {
            validation("TCG standard certificate for the signing key does not exist.");
        }

        signingKeyModulus = trustagentConfiguration.getSigningKeyModulusFile();
        if (signingKeyModulus == null || !signingKeyModulus.exists()) {
            validation("Public component of signing key does not exist.");
        }
        
    }

    @Override
    protected void execute() throws Exception {
        
        log.info("Starting the process to create the TCG standard signing key certificate");
        
        String signingKeySecretHex = RandomUtil.randomHexString(20);
        log.debug("Generated random Signing key secret"); 
        
        getConfiguration().set(TrustagentConfiguration.SIGNING_KEY_SECRET, signingKeySecretHex);
        
        // Call into the TpmModule certifyKey function to create the signing key and certify the same using the AIK so that we have the chain of trust.
        HashMap<String, byte[]> certifyKey = TpmModule.certifyKey(TrustagentConfiguration.SIGNING_KEY_NAME, trustagentConfiguration.getSigningKeySecret(), 
                trustagentConfiguration.getSigningKeyIndex(), trustagentConfiguration.getAikSecret(), trustagentConfiguration.getAikIndex());
        
        // Store the public key modulus, tcg standard certificate (output of certifyKey) & the private key blob.
        signingKeyBlob = trustagentConfiguration.getSigningKeyBlobFile();
        signingKeyTCGCertificate = trustagentConfiguration.getSigningKeyTCGCertificateFile(); 
        signingKeyModulus = trustagentConfiguration.getSigningKeyModulusFile();
        signingKeyTCGCertificateSignature = trustagentConfiguration.getSigningKeyTCGCertificateSignatureFile();
        
        log.debug("Blob path is : {}", signingKeyBlob.getAbsolutePath());
        log.debug("TCG Cert path is : {}", signingKeyTCGCertificate.getAbsolutePath());
        log.debug("TCG Cert signature path is : {}", signingKeyTCGCertificateSignature.getAbsolutePath());        
        log.debug("Public key modulus path is : {}", signingKeyModulus.getAbsolutePath());
        
        FileUtils.writeByteArrayToFile(signingKeyModulus, certifyKey.get("keymod"));
        FileUtils.writeByteArrayToFile(signingKeyBlob, certifyKey.get("keyblob"));
        FileUtils.writeByteArrayToFile(signingKeyTCGCertificate, certifyKey.get("keydata"));
        FileUtils.writeByteArrayToFile(signingKeyTCGCertificateSignature, certifyKey.get("keysig"));
        
        TpmCertifyKey tpmCertifyKey = new TpmCertifyKey(certifyKey.get("keydata"));
        log.debug("TCG Signing Key contents: {} - {}", tpmCertifyKey.getKeyParms().getAlgorithmId(), tpmCertifyKey.getKeyParms().getTrouSerSmode());
        
        log.info("Successfully created the signing key TCG certificate and the same has been stored at {}.", signingKeyTCGCertificate.getAbsolutePath());
    }    
}
