/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.CreateIdentity;
import java.security.cert.X509Certificate;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jbuhacoff
 */
public class RequestAikCertificate extends AbstractSetupTask {
    private TrustagentConfiguration config;
    private SimpleKeystore keystore;
    
    @Override
    protected void configure() throws Exception {
        config = new TrustagentConfiguration(getConfiguration());
        keystore = new SimpleKeystore(new FileResource(config.getTrustagentKeystoreFile()), config.getTrustagentKeystorePassword());
    }

    @Override
    protected void validate() throws Exception {
        X509Certificate aikCertificate = X509Util.decodePemCertificate(FileUtils.readFileToString(config.getAikCertificateFile()));
        X509Certificate privacyCA = keystore.getX509Certificate("privacy", SimpleKeystore.CA);
        if(aikCertificate.getIssuerX500Principal().getName().equals(privacyCA.getIssuerX500Principal().getName())) {
            validation("Known Privacy CA did not sign AIK Certificate");
        }
    }

    @Override
    protected void execute() throws Exception {
        CreateIdentity provisioner = new CreateIdentity();
        provisioner.run();
    }
    
}
