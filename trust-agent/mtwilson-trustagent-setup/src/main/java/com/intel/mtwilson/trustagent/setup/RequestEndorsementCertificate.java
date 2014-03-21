/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.ProvisionTPM;
import gov.niarl.his.privacyca.TpmModule;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 *
 * @author jbuhacoff
 */
public class RequestEndorsementCertificate extends AbstractSetupTask {
    private TrustagentConfiguration config;
    private SimpleKeystore keystore;
    
    @Override
    protected void configure() throws Exception {
        config = new TrustagentConfiguration(getConfiguration());
        keystore = new SimpleKeystore(new FileResource(config.getTrustagentKeystoreFile()), config.getTrustagentKeystorePassword());
    }

    @Override
    protected void validate() throws Exception {
        byte[] ekCert = TpmModule.getCredential(config.getTpmOwnerSecret(), "EC");
        X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
        if( !Arrays.equals(ekCert, endorsementCA.getEncoded())) {
            validation("Known Endorsement CA did not sign TPM EC");
        }
    }

    @Override
    protected void execute() throws Exception {
        ProvisionTPM provisioner = new ProvisionTPM();
        provisioner.run();
    }
    
}
