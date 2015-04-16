/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.Resource;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * A wrapper around java.security.KeyStore that specializes in X509Certificate entries
 * to simplify use of the keystore.
 * 
 * @author jbuhacoff
 */
public class PublicKeyX509CertificateStore extends AbstractKeyStore implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicKeyX509CertificateStore.class);
//    private char[] keystorePassword;
    private KeyStore keystore;
//    private KeyProtectionDelegate keyProtectionDelegate;

    /**
     * 
     * @param keystoreType should be JKS or PKCS12 or other keystore type which is capable of storing PrivateKey entries with their associated Certificate[] chains
     * @param keystoreFile
     * @param keystorePassword
     * @throws KeyStoreException
     * @throws IOException 
     */
    public PublicKeyX509CertificateStore(String keystoreType, File keystoreFile, char[] keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreFile, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
//        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }
    public PublicKeyX509CertificateStore(String keystoreType, Resource keystoreResource, char[] keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreResource, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
//        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }
    public PublicKeyX509CertificateStore(String keystoreType, Resource keystoreResource, Password keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreResource, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
//        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }

    /**
     * Precondition: keystore file exists (or throws FileNotFoundException)
     *
     * @return
     * @throws KeyStoreException
     * @throws
     */
    public X509Certificate get(String alias) throws KeyStoreException {
        Certificate certificate = keystore.getCertificate(alias);
        if( certificate instanceof X509Certificate ) {
            return (X509Certificate)certificate;
        }
        log.error("Alias {} is not an X509Certificate: {}", alias, certificate.getClass().getName());
        throw new KeyStoreException("Not an X509Certificate");
    }
    
    public void set(String alias, X509Certificate certificate) throws KeyStoreException {
        if (keystore.containsAlias(alias)) {
            log.warn("Replacing certificate {}", alias);
        }
        keystore.setCertificateEntry(alias, certificate);
        modified();
    }
    
}
