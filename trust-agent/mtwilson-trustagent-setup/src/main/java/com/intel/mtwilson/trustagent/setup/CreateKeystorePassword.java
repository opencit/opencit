/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 *
 * @author jbuhacoff
 */
public class CreateKeystorePassword extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateKeystorePassword.class);
    private TrustagentConfiguration trustagentConfiguration;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
    }

    @Override
    protected void validate() throws Exception {
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        if( keystorePassword == null || keystorePassword.isEmpty() ) {
            validation("Keystore password is not set");
        }
    }

    @Override
    protected void execute() throws Exception {
        String keystorePassword = RandomUtil.randomBase64String(8).replace("=","_");
        log.info("Generated random keystore password"); 
        File keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        if( keystoreFile.exists() ) {
            // load it and if we already have a password set then change it, otherwise we create a new keystore
            String existingKeystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
            try {
                SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), existingKeystorePassword);
                String[] aliases = keystore.aliases();
                log.debug("Keystore exists, changing password", aliases.length);
                keystore.save(keystoreFile, keystorePassword);
            }
            catch(KeyManagementException | KeyStoreException e) {
                log.debug("Cannot open keystore, deleting it", e);
                keystoreFile.delete();
            }
        }
        // store the new password
        getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_KEYSTORE_PASSWORD, keystorePassword);
    }
    
}
