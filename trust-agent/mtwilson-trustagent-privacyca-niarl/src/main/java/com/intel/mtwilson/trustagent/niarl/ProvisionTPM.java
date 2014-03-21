/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.niarl;

import com.intel.mtwilson.My;
import com.intel.mtwilson.client.jaxrs.PrivacyCA;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmUtils;
import java.security.cert.X509Certificate;

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
public class ProvisionTPM implements Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProvisionTPM.class);
    
    @Override
    public void run() {
        try {
            TrustagentConfiguration config = new TrustagentConfiguration(My.configuration().getConfiguration());
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
		// Take Ownership
		byte [] nonce1 = TpmUtils.createRandomBytes(20);
		try {
			TpmModule.takeOwnership(config.getTpmOwnerSecret(), nonce1);
		} catch (TpmModule.TpmModuleException e){
			if(e.toString().contains(".takeOwnership returned nonzero error: 4")){
				log.info("Ownership is already taken");
                                if( !System.getProperty("forceCreateEk", "false").equals("true") ) { // feature to help with bug #554 and allow admin to force creating an ek (in case it failed the first time due to a non-tpm error such as java missing classes exception
                                    return;
                                }
			}
			else
				throw e;
		}
		// Create Endorsement Certificate
		byte[] nonce2 = TpmUtils.createRandomBytes(20);
		try {
			byte [] pubEkMod = TpmModule.getEndorsementKeyModulus(config.getTpmOwnerSecret(), nonce2);
            
            // send the public endorsement key modulus to the privacy ca and receive the endorsement certificate
            PrivacyCA pcaClient = new PrivacyCA(config.getConfiguration());
            X509Certificate ekCert = pcaClient.endorseTpm(pubEkMod);
            
            // write the EC to the TPM NVRAM
			TpmModule.setCredential(config.getTpmOwnerSecret(), "EC", ekCert.getEncoded());
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
