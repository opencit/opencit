/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.setup.SetupException;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * Depends on CreateCertificateAuthorityKey to create the cakey first
 * 
 * @author jbuhacoff
 */
public class CreateSamlCertificate extends LocalSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateSamlCertificate.class);

    private String samlDistinguishedName = "CN=mtwilson-saml,OU=mtwilson";
//    private File tlsKeystoreFile = null; // maybe the path would be a configuration item, currently it's hardcoded to be "mtwilson-tls.jks" under MTWILSON_CONF
    private String samlKeystorePassword = null;

    public String getSamlKeystorePassword() {
        return samlKeystorePassword;
    }

    public void setSamlKeystorePassword(String samlKeystorePassword) {
        this.samlKeystorePassword = samlKeystorePassword;
    }

    public String getSamlDistinguishedName() {
        return samlDistinguishedName;
    }

    public void setSamlDistinguishedName(String samlDistinguishedName) {
        this.samlDistinguishedName = samlDistinguishedName;
    }
    
    
    
    @Override
    protected void configure() throws Exception {
        if(samlDistinguishedName == null) {
            configuration("SAML distinguished name is not configured");
        }
        samlKeystorePassword = My.configuration().getSamlKeystorePassword();
        if( samlKeystorePassword == null ) {
            // XXX TODO automatically generate samlKeystorePassword 
            // after configure() the application should  getSamlKeystorePassword()  from us and 
            // save it (for example in mtwilson.properties)
            samlKeystorePassword ="password";
        }
        
        // this section about checkign the ca key availability
        // is in configuration because it must be ready before the
        // setup task can even run
        // it's copied from the validate() method of CreateCertificateAuthorityKe
        // and probably this code needs to be refactored so we don't repeat it;
        // the challenge is whether the exception handling with configuration/validation
        // fault logging can be refactored because the CA setup needs to log them
        // as validation issues while dependent setups such as this SAML setup need to 
        // log them as configuration issues here
            byte[] combinedPrivateKeyAndCertPemBytes;
            FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile()); // throws FileNotFoundException, IOException
            combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn); // throws IOException
            cakeyIn.close();
            try {
                PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
                log.debug("Read cakey {} from {}", cakey.getAlgorithm(), My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
            catch(Exception e) {
                log.debug("Cannot read private key from {}", My.configuration().getCaKeystoreFile().getAbsolutePath(), e);
                configuration("Cannot read private key from: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
            try {
                X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
                log.debug("Read cacert {} from {}", cacert.getSubjectX500Principal().getName(), My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
            catch(Exception e) {
                log.debug("Cannot read certificate from {}", My.configuration().getCaKeystoreFile().getAbsolutePath(), e);
                configuration("Cannot read certificate from: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
            }
        
    }

    @Override
    protected void validate() throws Exception {
        File samlKeystoreFile = My.configuration().getSamlKeystoreFile();
        if( !samlKeystoreFile.exists() ) {
            validation("SAML keystore is missing");
        }
        // keystore exists, look for the private key and cert
        // XXX TODO make sure it has a SAML private key and certificate inside
        if( samlKeystorePassword == null ) {
            configuration("SAML keystore password is not configured");
            return;
        }
        SimpleKeystore keystore = new SimpleKeystore(samlKeystoreFile, samlKeystorePassword);
        for(String alias : keystore.aliases()) {
            log.debug("Keystore alias: {}", alias);
            // make sure it has a SAML private key and certificate inside
            try {
                RsaCredentialX509 credential = keystore.getRsaCredentialX509(alias, samlKeystorePassword);
                log.debug("SAML certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());
            }
            catch(Exception e) {
                log.debug("Cannot read SAML key from keystore", e);
//                validation("Cannot read SAML key from keystore"); // we are assuming the keystore only has one private key entry ... 
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        // load the ca key - same code as in configure() but without exception
        // handling 
            byte[] combinedPrivateKeyAndCertPemBytes;
            FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile()); // throws FileNotFoundException, IOException
            combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn); // throws IOException
            cakeyIn.close();
            PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
            X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
        
        // create a new key pair for SAML
        KeyPair samlkey = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory();
//        builder.selfSigned(samlDistinguishedName, samlkey);
        builder.issuerName(cacert);
        builder.issuerPrivateKey(cakey);
        builder.subjectName(samlDistinguishedName);
        builder.subjectPublicKey(samlkey.getPublic());
        X509Certificate samlcert = builder.build();
        if( cacert == null ) {
//            log.error("Failed to create certificate"); // no need to print this, if the build failed there are guaranteed to be faults to print...
            List<Fault> faults = builder.getFaults();
            for(Fault fault : faults) {
                log.error(String.format("%s%s", fault.toString(), fault.getCause() == null ? "" : ": "+fault.getCause().getMessage()));
                validation(fault); // XXX TODO  should we have an execution() category of faults? 
            }
            throw new SetupException("Cannot generate SAML certificate");
            
        }
        
        File samlKeystoreFile = My.configuration().getSamlKeystoreFile();
        SimpleKeystore keystore = new SimpleKeystore(samlKeystoreFile, samlKeystorePassword);
//        keystore.addTrustedCaCertificate(cacert, cacert.getIssuerX500Principal().getName());
        keystore.addKeyPairX509(samlkey.getPrivate(), samlcert, samlDistinguishedName, samlKeystorePassword, cacert); // we have to provide the issuer chain since it's not self-signed,  otherwise we'll get an exception from the KeyStore provider
        keystore.save();
    }
    
}
