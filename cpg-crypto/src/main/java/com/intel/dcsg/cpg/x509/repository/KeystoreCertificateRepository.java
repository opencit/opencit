/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.crypto.Md5Digest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A writable (depending on file permissions) repository of X509Certificates that is saved in a JKS-format file.
 * The repository must already exist.
 * 
 * @author jbuhacoff
 */
public class KeystoreCertificateRepository implements MutableCertificateRepository {

    private Logger log = LoggerFactory.getLogger(getClass());
    private final String keystorePath;
    private final char[] password;
    private final KeyStore keystore;

    public KeystoreCertificateRepository(String keystorePath, String keystorePassword) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
        this.keystorePath = keystorePath;
        password = keystorePassword.toCharArray();
        keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            File keystoreFile = new File(keystorePath);
            if( keystoreFile.exists() ) {
                FileInputStream in = new FileInputStream(keystoreFile);
                keystore.load(in, password);
            }
            else {
                keystore.load(null, password);            
            }
        }
        catch(Exception e) {
            log.error("Cannot open keystore: ", e);
            keystore.load(null, password);            
        }
    }
    
    public KeyStore getKeystore() {
        return keystore;
    }

    /**
     *
     * @return a list of certificates in the repository; modifying the list does not modify the repository
     */
    @Override
    public List<X509Certificate> getCertificates() {
        ArrayList<X509Certificate> allCerts = new ArrayList<X509Certificate>();
        try {
            List<String> aliases = Collections.list(keystore.aliases());
            for(String alias : aliases) {
                log.debug("Keystore entry alias: {}", alias);
                if( Md5Digest.isValidHex(alias) ) {
                    Md5Digest fingerprint = new Md5Digest(alias);
                    X509Certificate cert = getCertificate(fingerprint);
                    if( cert != null ) {
                        allCerts.add(cert);
                    }
                }
            }
        }
        catch(Exception e) {
            log.error("Cannot list certificates in keystore", e);
        }
        return allCerts;
    }

    /**
     * Based on the underlying HashSet hashCode
     * @return
     */
    @Override
    public int hashCode() {
        return keystore.hashCode()+1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        KeystoreCertificateRepository rhs = (KeystoreCertificateRepository) other;
        return this.keystore.equals(rhs.keystore);
    }

    /**
     *
     * @param certificate
     * @throws KeyManagementException
     */
    @Override
    public void addCertificate(X509Certificate certificate) throws KeyManagementException {
        try {
            Md5Digest fingerprint = Md5Digest.digestOf(certificate.getEncoded());
            X509Certificate trustedCert = getCertificate(fingerprint);
            if( trustedCert != null ) {
                log.debug("Certificate is already in keystore: {}", certificate.getSubjectX500Principal().getName());
                return;
            }
            String alias = fingerprint.toString();
            log.debug("Adding certificate to repository: {}", alias);
            keystore.setCertificateEntry(alias, certificate);
            
            // save the keystore!
            FileOutputStream out = new FileOutputStream(new File(keystorePath));
            keystore.store(out, password);
            IOUtils.closeQuietly(out);
        }
        catch(Exception e) {
            throw new KeyManagementException("Cannot add certificate", e);
        }
    }
    
    
    
    /**
     * Md5Digest happens to be the alias we use when storing so we can do a direct lookup
     * @param fingerprint md5 digest of the certificate you want to retrieve
     * @return the X509 Certificate if found, or null if it was not found
     * @throws KeyManagementException  if there were errors opening, searching, or loading certificates from the keystore
     */
    public X509Certificate getCertificate(Md5Digest fingerprint) throws KeyManagementException {
        String alias = fingerprint.toString(); // hex md5 fingerprint
        try {
            if( keystore.containsAlias(alias) ) {
                if( keystore.isCertificateEntry(alias) ) {
                    KeyStore.TrustedCertificateEntry entry = (KeyStore.TrustedCertificateEntry)keystore.getEntry(alias, null); // using null instead of password because trusted certificate entries are not password protected (only the keystore itself is protected to provide integrity over the contents);    new KeyStore.PasswordProtection(password)
                    Certificate cert = entry.getTrustedCertificate();
                    if( cert instanceof X509Certificate ) {
                        return (X509Certificate)cert;
                    }
                }
            }
            return null;
        }
        catch(Exception e) {
            throw new KeyManagementException(e);
        }
    }
        
}
