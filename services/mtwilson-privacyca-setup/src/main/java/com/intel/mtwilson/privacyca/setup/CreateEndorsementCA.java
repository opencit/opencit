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
import java.security.cert.X509Certificate;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author jbuhacoff
 */
public class CreateEndorsementCA extends LocalSetupTask {
    private File endorsementPemFile;
    private String endorsementPassword;
    private String endorsementIssuer;
    private File endorsementP12;
    private int endorsementCertificateValidityDays;
    
    @Override
    protected void configure() throws Exception {
        endorsementPemFile = My.configuration().getPrivacyCaEndorsementCacertsFile();
        endorsementIssuer = My.configuration().getPrivacyCaEndorsementIssuer();
        endorsementP12 = My.configuration().getPrivacyCaEndorsementP12();
        endorsementPassword = My.configuration().getPrivacyCaEndorsementPassword();
        endorsementCertificateValidityDays = My.configuration().getPrivacyCaEndorsementValidityDays();
        
        if( endorsementPassword == null || endorsementPassword.isEmpty() ) {
            endorsementPassword = RandomUtil.randomBase64String(16); 
            getConfiguration().set("mtwilson.privacyca.ek.p12.password", endorsementPassword);
        }
    }

    @Override
    protected void validate() throws Exception {
        if( !endorsementPemFile.exists() ) {
            validation("Endorsement CA certs file does not exist");
        }
        if( !endorsementP12.exists() ) {
            validation("Endorsement P12 file does not exist");
        }
        if( !endorsementP12.exists() ) {
            validation("Privacy CA p12 file does not exist");
        }
    }

    @Override
    protected void execute() throws Exception {
        TpmUtils.createCaP12(2048, endorsementIssuer, endorsementPassword, endorsementP12.getAbsolutePath(), endorsementCertificateValidityDays);
        X509Certificate pcaCert = TpmUtils.certFromP12(endorsementP12.getAbsolutePath(), endorsementPassword);
        String self = X509Util.encodePemCertificate(pcaCert);
        String existingEndorsementAuthorities = "";
        if( endorsementPemFile.exists() ) {
            existingEndorsementAuthorities = FileUtils.readFileToString(endorsementPemFile);
        }
        FileUtils.writeStringToFile(endorsementPemFile, String.format("%s\n%s", existingEndorsementAuthorities,self)); 
    }
    
}
