/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.saml;

import com.intel.dcsg.cpg.configuration.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Loads the private key, certificate, and other settings required for the SAML
 * issuer.
 *
 * @author jbuhacoff
 */
public class IssuerConfiguration {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssuerConfiguration.class);
    private final PrivateKey privateKey;
    private final Certificate certificate;
    private final String issuerName; // for example, http://1.2.3.4/AttestationService
    private final String issuerServiceName; // for example "AttestationService"
    private final String jsr105provider;
    private final Integer validitySeconds;

    public IssuerConfiguration(PrivateKey privateKey, Certificate certificate, Configuration configuration) {
        this.privateKey = privateKey;
        this.certificate = certificate;
        SamlConfiguration saml = new SamlConfiguration(configuration);
        issuerName = configuration.get(SamlConfiguration.SAML_ISSUER, "AttestationService"); // saml.getSamlIssuer();
        issuerServiceName = "Cloud Integrity Technology";
        jsr105provider = saml.getJsr105Provider(); // conf.getString(JSR105_PROVIDER, "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        validitySeconds = saml.getSamlValiditySeconds();
    }

    public IssuerConfiguration(Configuration configuration) throws FileNotFoundException, IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        SamlConfiguration saml = new SamlConfiguration(configuration);

        log.debug("SAML keystore file: {}", saml.getSamlKeystoreFile());
        File keystoreFile = saml.getSamlKeystoreFile(); // replaces My.configuration().getSamlKeystoreFile();
        try (FileInputStream keystoreInputStream = new FileInputStream(keystoreFile)) {
            KeyStore keyStore = getKeyStore(keystoreInputStream, saml.getSamlKeystorePassword()); /*configuration.getString("saml.keystore.password"*//*,System.getenv("SAMLPASSWORD")*/
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(saml.getSamlKeyAlias(), //  /*configuration.getString("saml.key.alias"),*/
                    new KeyStore.PasswordProtection(saml.getSamlKeyPassword().toCharArray()));    //configuration.getString("saml.key.password"/*, System.getenv("SAMLPASSWORD")*/).toCharArray()));
            privateKey = entry.getPrivateKey();
            certificate = entry.getCertificate();
        }

        issuerName = configuration.get(SamlConfiguration.SAML_ISSUER, "AttestationService"); // saml.getSamlIssuer();
        issuerServiceName = "Cloud Integrity Technology";
        jsr105provider = saml.getJsr105Provider(); // conf.getString(JSR105_PROVIDER, "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        validitySeconds = saml.getSamlValiditySeconds();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public String getIssuerServiceName() {
        return issuerServiceName;
    }

    public String getJsr105Provider() {
        return jsr105provider;
    }

    public Integer getValiditySeconds() {
        return validitySeconds;
    }

    /**
     * Get a KeyStore object given the keystore filename and password.
     */
    private KeyStore getKeyStore(InputStream in, String password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore result = KeyStore.getInstance(KeyStore.getDefaultType());
        result.load(in, password.toCharArray());
        return result;
    }
}
