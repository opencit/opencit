/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.setup;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.security.cert.X509Certificate;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jbuhacoff
 */
public class CreatePrivacyCA extends LocalSetupTask {
    private File identityPemFile;
    private String identityPassword;
    private String identityIssuer;
    private File identityP12;
    private int identityCertificateValidityDays;
    
    @Override
    protected void configure() throws Exception {
        identityPemFile = My.configuration().getPrivacyCaIdentityCacertsFile();
        identityIssuer = My.configuration().getPrivacyCaIdentityIssuer();
        identityP12 = My.configuration().getPrivacyCaIdentityP12();
        identityPassword = My.configuration().getPrivacyCaIdentityPassword();
        identityCertificateValidityDays = My.configuration().getPrivacyCaIdentityValidityDays();
        
        if( identityPassword == null || identityPassword.isEmpty() ) {
            identityPassword = RandomUtil.randomBase64String(16); 
            getConfiguration().set("mtwilson.privacyca.aik.p12.password", identityPassword);
        }
    }

    @Override
    protected void validate() throws Exception {
        if( !identityPemFile.exists() ) {
            validation("Privacy CA certs file does not exist");
        }
        if( !identityP12.exists() ) {
            validation("Privacy CA P12 file does not exist");
        }
    }

    @Override
    protected void execute() throws Exception {
        TpmUtils.createCaP12(2048, identityIssuer, identityPassword, identityP12.getAbsolutePath(), identityCertificateValidityDays);
        X509Certificate pcaCert = TpmUtils.certFromP12(identityP12.getAbsolutePath(), identityPassword);
        String self = X509Util.encodePemCertificate(pcaCert);
        String existingPrivacyAuthorities = "";
        if( identityPemFile.exists() ) {
            existingPrivacyAuthorities = FileUtils.readFileToString(identityPemFile);
        }
        FileUtils.writeStringToFile(identityPemFile, String.format("%s\n%s", existingPrivacyAuthorities,self)); 
    }
    
}
