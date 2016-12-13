/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.niarl;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.client.jaxrs.PrivacyCA;
import com.intel.dcsg.cpg.configuration.Configurable;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.tpmmodules.Tpm;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmUtils;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * Take ownership
 * Request EC from Mt Wilson Privacy CA
 * 
 * Pre-requisites:
 * TPM Owner Secret and TPM SRK Secret must already be configured
 * (whether newly generated for taking ownership or previously generated for
 * existing TPM owner)
 * 
 * NOTE:
 * TSS_TPMSTATUS_CEKP_USED   can be used to determine if the EK is the original
 * manufacturer EK or a locally generated one using Tspi_TPM_CreateEndorsementKey
 * 
 * 
 * @author jbuhacoff
 */
public class ProvisionTPM implements Configurable, Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProvisionTPM.class);
    
    private Configuration configuration = null;
    
    @Override
    public void configure(Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public void run() {
        try {
            TrustagentConfiguration config = new TrustagentConfiguration(configuration);
            /*
             * The following actions must be performed during the TPM Provisioning process:
             * 1. Take ownership of the TPM
             * 		- owner auth
             * 2. Create an Endorsement Certificate (EC)
             * 		- public EK
             * 			- owner auth (should already have from above)
             * 		- private key and cert for CA to create new cert
             * 		- validity period of EC cert
             * 3. Store the newly created EC in the TPM's NV-RAM
             */
            // Take Ownership - we expect either the tpm to be cleared so we can
            // take ownership usng the configured tpm owner secret, or for
            // the tpm to already be owned (we'll get error code #4) with the
            // secret we know so we can continue
            byte [] nonce1 = TpmUtils.createRandomBytes(20);
            try {
                //TpmModule.takeOwnership(config.getTpmOwnerSecret(), nonce1);
                Tpm.getModule().takeOwnership(config.getTpmOwnerSecret(), nonce1);
            } catch (TpmModule.TpmModuleException e){
                if( e.getErrorCode() != null && e.getErrorCode() == 4 ) {
                    log.debug("Ownership is already taken"); 
                    /*
                    if( !System.getProperty("forceCreateEk", "false").equals("true") ) { // feature to help with bug #554 and allow admin to force creating an ek (in case it failed the first time due to a non-tpm error such as java missing classes exception
                        return;
                    }
                    */
                }
                else {
                    throw e;
                }
            }
            // Create Endorsement Certificate
            byte[] nonce2 = TpmUtils.createRandomBytes(20);
            log.debug("Nonce: {}", TpmUtils.byteArrayToHexString(nonce2));
            try {
                //byte [] pubEkMod = TpmModule.getEndorsementKeyModulus(config.getTpmOwnerSecret(), nonce2);
                byte [] pubEkMod = Tpm.getModule().getEndorsementKeyModulus(config.getTpmOwnerSecret(), nonce2);
                log.debug("Public EK Modulus: {}", TpmUtils.byteArrayToHexString(pubEkMod));
                log.debug("Requesting TPM endorsement from Privacy CA");
                // send the public endorsement key modulus to the privacy ca and receive the endorsement certificate
            
                TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(config.getTrustagentKeystoreFile(), config.getTrustagentKeystorePassword()).build();
                TlsConnection tlsConnection = new TlsConnection(new URL(config.getMtWilsonApiUrl()), tlsPolicy);

                Properties clientConfiguration = new Properties();
                clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, config.getMtWilsonApiUsername());
                clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, config.getMtWilsonApiPassword());
            
                PrivacyCA pcaClient = new PrivacyCA(clientConfiguration, tlsConnection);

                X509Certificate ekCert = pcaClient.endorseTpm(pubEkMod);
                log.debug("Received EC {}", Sha1Digest.digestOf(ekCert.getEncoded()).toHexString());
                // write the EC to the TPM NVRAM
                //TpmModule.setCredential(config.getTpmOwnerSecret(), "EC", ekCert.getEncoded());
                Tpm.getModule().setCredential(config.getTpmOwnerSecret(), "EC", ekCert.getEncoded());
            } catch (TpmModule.TpmModuleException e){
                log.error("Error getting EC: {}", e.getMessage());
                throw e;
            }
        }
        catch(Exception e) {
            log.error("Error provisioning TPM: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }    
}
