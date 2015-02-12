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
    private File bindingKeyTCGCertificateSignature;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());        
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

        bindingKeyTCGCertificateSignature = trustagentConfiguration.getBindingKeyTCGCertificateSignatureFile();
        if (bindingKeyTCGCertificateSignature == null || !bindingKeyTCGCertificateSignature.exists()) {
            validation("Signature file of the TCG standard certificate for the binding key does not exist.");
        }

        bindingKeyModulus = trustagentConfiguration.getBindingKeyModulusFile();
        if (bindingKeyModulus == null || !bindingKeyModulus.exists()) {
            validation("Public component of binding key does not exist.");
        }
        
    }

    @Override
    protected void execute() throws Exception {
        
        log.info("Starting the process to create the TCG standard binding key certificate");
        
        String bindingKeySecretHex = RandomUtil.randomHexString(20);
        log.info("Generated random Binding key secret"); 
        
        getConfiguration().setString(TrustagentConfiguration.BINDING_KEY_SECRET, bindingKeySecretHex);
        
        // Call into the TpmModule certifyKey function to create the binding key and certify the same using AIK to build the chain of trust.
        HashMap<String, byte[]> certifyKey = TpmModule.certifyKey(TrustagentConfiguration.BINDING_KEY_NAME, trustagentConfiguration.getBindingKeySecret(), 
                trustagentConfiguration.getBindingKeyIndex(), trustagentConfiguration.getAikSecret(), trustagentConfiguration.getAikIndex());
        
        String blobPath = trustagentConfiguration.getBindingKeyBlobFile().getAbsolutePath();
        String tcgCertPath = trustagentConfiguration.getBindingKeyTCGCertificateFile().getAbsolutePath(); 
        String pubKeyModulus = trustagentConfiguration.getBindingKeyModulusFile().getAbsolutePath();
        String tcgCertSignaturePath = trustagentConfiguration.getBindingKeyTCGCertificateSignatureFile().getAbsolutePath();
        
        log.debug("Blob path is : {}", blobPath);
        log.debug("TCG Cert path is : {}", tcgCertPath);
        log.debug("TCG Cert signature path is : {}", tcgCertSignaturePath);
        log.debug("Public key modulus path is : {}", pubKeyModulus);
        
        SetupUtils.writeblob(pubKeyModulus, certifyKey.get("keymod"));
        SetupUtils.writeblob(blobPath, certifyKey.get("keyblob"));
        SetupUtils.writeblob(tcgCertPath, certifyKey.get("keydata"));
        SetupUtils.writeblob(tcgCertSignaturePath, certifyKey.get("keysig"));
        
        TpmCertifyKey tpmCertifyKey = new TpmCertifyKey(certifyKey.get("keydata"));
        log.debug("TCG Binding Key contents: {} - {}", tpmCertifyKey.getKeyParms().getAlgorithmId(), tpmCertifyKey.getKeyParms().getTrouSerSmode());

        log.info("Successfully created the Binding key TCG certificate and the same has been stored at {}.", tcgCertPath);
                
    }    
}
