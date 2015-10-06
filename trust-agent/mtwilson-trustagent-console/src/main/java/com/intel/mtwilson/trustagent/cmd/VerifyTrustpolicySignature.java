/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import static com.intel.mtwilson.util.xml.dsig.XmlDsigVerify.isValid;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author rksavino
 */
public class VerifyTrustpolicySignature implements Command {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VerifyTrustpolicySignature.class);
    private static final String SAML_CERTIFICATE_ALIAS = "saml (ca)";
    private TrustagentConfiguration configuration;
    private Configuration options;
    
    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }

    @Override
    public void execute(String[] args) throws Exception {
        configuration = TrustagentConfiguration.loadConfiguration();
        
        if ( args == null || args.length <= 0 ) {
            throw new IllegalArgumentException("Path to trust policy XML file not specified");
        } else if ( args.length > 1 ) {
            throw new IllegalArgumentException("Illegal number of arguments");
        }
        File trustPolicyXmlFile = new File(args[0]);
        if( !trustPolicyXmlFile.exists() ) {
            throw new FileNotFoundException("Trust policy XML file does not exist");
        }
        File trustagentKeystoreFile = configuration.getTrustagentKeystoreFile();
        if( !trustagentKeystoreFile.exists() ) {
            throw new FileNotFoundException("Trustagent keystore file does not exist");
        }
        String trustagentKeystorePassword = configuration.getTrustagentKeystorePassword();
        if( trustagentKeystorePassword == null || trustagentKeystorePassword.isEmpty()) {
            throw new NullPointerException("Trustagent keystore password is not configured");
        }
        
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(trustagentKeystoreFile), trustagentKeystorePassword);
        X509Certificate samlCert;
        
        try {
            samlCert = keystore.getX509Certificate(SAML_CERTIFICATE_ALIAS);
            if (samlCert == null || samlCert.getSubjectX500Principal() == null 
                    || samlCert.getSubjectX500Principal().getName() == null) {
                log.error("Invalid SAML certificate: credential contains null value");
                throw new NullPointerException("Invalid SAML certificate: credential contains null value");
            }
            log.debug("Found key {}", samlCert.getSubjectX500Principal().getName());
        } catch(java.security.UnrecoverableKeyException e) {
            log.error("Incorrect password for existing key: {}", e.getMessage());
            throw e;
        }
//        } catch(NullPointerException e) {
//            log.error("Invalid certificate");
//            throw e;
//        }
        
        String trustPolicyXml;
        File fileDir = new File(trustPolicyXmlFile.getAbsolutePath());
        try(FileInputStream in = new FileInputStream(fileDir)) {
            trustPolicyXml = IOUtils.toString(in, Charset.forName("UTF-8"));
        } catch (Exception e) {
            log.error("Error reading trust policy XML file");
            throw e;
        }
        if ( trustPolicyXml.isEmpty() ) {
            throw new IllegalArgumentException("Trust policy XML file is empty");
        }
        if ( !isValid(trustPolicyXml, samlCert) ) {
            throw new IllegalArgumentException("Trust policy is not signed by MtWilson SAML");
        }
    }
}
