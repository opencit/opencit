/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.tasks;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.setup.LocalSetupTask;
import com.intel.mtwilson.setup.SetupException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
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
    public static final String SAML_CERTIFICATE_DN = "saml.certificate.dn";
    public static final String SAML_KEYSTORE_FILE = "saml.keystore.file";
    public static final String SAML_KEYSTORE_PASSWORD = "saml.keystore.password";
    public static final String SAML_KEY_ALIAS = "saml.key.alias";
    public static final String SAML_KEY_PASSWORD = "saml.key.password";

    public String getSamlKeystoreFile() {
        return getConfiguration().get(SAML_KEYSTORE_FILE, My.configuration().getDirectoryPath() + File.separator + "mtwilson-saml.jks");
    }

    public void setSamlKeystoreFile(String samlKeystoreFile) {
        getConfiguration().set(SAML_KEYSTORE_FILE, samlKeystoreFile);
    }

    public String getSamlKeystorePassword() {
        return getConfiguration().get(SAML_KEYSTORE_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    public void setSamlKeystorePassword(String samlKeystorePassword) {
        getConfiguration().set(SAML_KEYSTORE_PASSWORD, samlKeystorePassword);
    }

    public String getSamlKeyAlias() {
        return getConfiguration().get(SAML_KEY_ALIAS, "mtwilson-saml");
    }

    public void setSamlKeyAlias(String samlKeyAlias) {
        getConfiguration().set(SAML_KEY_ALIAS, samlKeyAlias);
    }

    public String getSamlKeyPassword() {
        return getConfiguration().get(SAML_KEY_PASSWORD, null); // no default here, will return null if not configured: only the configure() method will generate a new random password if necessary
    }

    public void setSamlKeyPassword(String samlKeyPassword) {
        getConfiguration().set(SAML_KEY_PASSWORD, samlKeyPassword);
    }

    public String getSamlCertificateDistinguishedName() {
        return getConfiguration().get(SAML_CERTIFICATE_DN, "CN=mtwilson-saml,OU=mtwilson");
    }

    public void setSamlCertificateDistinguishedName(String samlDistinguishedName) {
        getConfiguration().set(SAML_CERTIFICATE_DN, samlDistinguishedName);
    }

    @Override
    protected void configure() throws Exception {
//        File samlKeystoreFile = new File(getSamlKeystoreFile());

        String samlKeystorePassword = getSamlKeystorePassword();
        if (samlKeystorePassword == null || samlKeystorePassword.isEmpty()) {
            setSamlKeystorePassword(RandomUtil.randomBase64String(16));
            setSamlKeyPassword(getSamlKeystorePassword());
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
        try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) { //  // throws FileNotFoundException, IOException
            combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn); // throws IOException
        } catch (IOException e) {
            log.debug("Cannot read saml cakey from {}", My.configuration().getCaKeystoreFile());
            configuration("Cannot read saml ca key from: %s", My.configuration().getCaKeystoreFile());
            return;
        }
        try {
            PrivateKey cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
            log.debug("Read cakey {} from {}", cakey.getAlgorithm(), My.configuration().getCaKeystoreFile().getAbsolutePath());
        } catch (CryptographyException e) {
            log.debug("Cannot read private key from {}", My.configuration().getCaKeystoreFile().getAbsolutePath(), e);
            configuration("Cannot read private key from: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
        }
        try {
            X509Certificate cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
            log.debug("Read cacert {} from {}", cacert.getSubjectX500Principal().getName(), My.configuration().getCaKeystoreFile().getAbsolutePath());
        } catch (CertificateException e) {
            log.debug("Cannot read certificate from {}", My.configuration().getCaKeystoreFile().getAbsolutePath(), e);
            configuration("Cannot read certificate from: %s", My.configuration().getCaKeystoreFile().getAbsolutePath());
        }

    }

    @Override
    protected void validate() throws Exception {
        if (getSamlKeystorePassword() == null) {
            configuration("SAML keystore password is not configured");
        }
        if (getSamlKeyPassword() == null) {
            configuration("SAML key password is not configured");
        }
        if (!getConfigurationFaults().isEmpty()) {
            return;
        }

//        File samlKeystoreFile = My.configuration().getSamlKeystoreFile();
        File samlKeystoreFile = new File(getSamlKeystoreFile());
        if (!samlKeystoreFile.exists()) {
            validation("SAML keystore file is missing");
        }
        // keystore exists, look for the private key and cert
        SimpleKeystore keystore = new SimpleKeystore(samlKeystoreFile, getSamlKeystorePassword());
        for (String alias : keystore.aliases()) {
            log.debug("Keystore alias: {}", alias);
            // make sure it has a SAML private key and certificate inside
            try {
                RsaCredentialX509 credential = keystore.getRsaCredentialX509(alias, getSamlKeystorePassword());
                log.debug("SAML certificate: {}", credential.getCertificate().getSubjectX500Principal().getName());
            } catch (FileNotFoundException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException | CryptographyException e) {
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
        PrivateKey cakey;
        X509Certificate cacert;
        try (FileInputStream cakeyIn = new FileInputStream(My.configuration().getCaKeystoreFile())) { // ; // throws FileNotFoundException, IOException
            combinedPrivateKeyAndCertPemBytes = IOUtils.toByteArray(cakeyIn); // throws IOException
            cakey = RsaUtil.decodePemPrivateKey(new String(combinedPrivateKeyAndCertPemBytes));
            cacert = X509Util.decodePemCertificate(new String(combinedPrivateKeyAndCertPemBytes));
        }

        // create a new key pair for SAML
        KeyPair samlkey = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory();
//        builder.selfSigned(samlDistinguishedName, samlkey);
        builder.issuerName(cacert);
        builder.issuerPrivateKey(cakey);
        builder.subjectName(getSamlCertificateDistinguishedName());
        builder.subjectPublicKey(samlkey.getPublic());
        X509Certificate samlcert = builder.build();
        if (cacert == null) {
//            log.error("Failed to create certificate"); // no need to print this, if the build failed there are guaranteed to be faults to print...
            List<Fault> faults = builder.getFaults();
            for (Fault fault : faults) {
                log.error(String.format("%s: %s", fault.getClass().getName(), fault.toString()));
                validation(fault); 
            }
            throw new SetupException("Cannot generate SAML certificate");

        }

        File samlKeystoreFile = new File(getSamlKeystoreFile());
        SimpleKeystore keystore = new SimpleKeystore(samlKeystoreFile, getSamlKeystorePassword());
//        keystore.addTrustedCaCertificate(cacert, cacert.getIssuerX500Principal().getName());
        keystore.addKeyPairX509(samlkey.getPrivate(), samlcert, getSamlCertificateDistinguishedName(), getSamlKeystorePassword(), cacert); // we have to provide the issuer chain since it's not self-signed,  otherwise we'll get an exception from the KeyStore provider
        keystore.save();

        Pem samlCert = new Pem("CERTIFICATE", samlcert.getEncoded());
        Pem samlCaCert = new Pem("CERTIFICATE", cacert.getEncoded());
        String certificateChainPem = String.format("%s\n%s", samlCert.toString(), samlCaCert.toString());
        File certificateChainPemFile = new File(My.configuration().getDirectoryPath() + File.separator + "saml.crt.pem");
        try (FileOutputStream certificateChainPemFileOut = new FileOutputStream(certificateChainPemFile)) {
            IOUtils.write(certificateChainPem, certificateChainPemFileOut);
        } catch (IOException e) {
            validation(e, "Cannot write saml.crt.pem");
        }

//        getConfiguration().setString("saml.keystore.file", samlKeystoreFile.getAbsolutePath());
//        getConfiguration().setString("saml.keystore.password", samlKeystorePassword);
//        getConfiguration().setString("saml.key.alias", samlDistinguishedName);
//        getConfiguration().setString("saml.key.password", samlKeystorePassword);
    }
}
