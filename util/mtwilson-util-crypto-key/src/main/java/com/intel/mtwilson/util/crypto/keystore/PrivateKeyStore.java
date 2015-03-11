/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.dcsg.cpg.io.Resource;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

/**
 * A wrapper around java.security.KeyStore that specializes in PrivateKey entries
 * and simplifies use of the keystore.
 * 
 * @author jbuhacoff
 */
public class PrivateKeyStore extends AbstractKeyStore implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PrivateKeyStore.class);
//    private char[] keystorePassword;
    private KeyStore keystore;
    private KeyProtectionDelegate keyProtectionDelegate;

    /**
     * 
     * @param keystoreType should be JKS or PKCS12 or other keystore type which is capable of storing PrivateKey entries with their associated Certificate[] chains
     * @param keystoreFile
     * @param keystorePassword
     * @throws KeyStoreException
     * @throws IOException 
     */
    public PrivateKeyStore(String keystoreType, File keystoreFile, char[] keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreFile, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }
    public PrivateKeyStore(String keystoreType, Resource keystoreResource, char[] keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreResource, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }
    public PrivateKeyStore(String keystoreType, Resource keystoreResource, Password keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreResource, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }

    /**
     * Precondition: keystore file exists (or throws FileNotFoundException)
     *
     * @return
     * @throws KeyStoreException
     * @throws FileNotFoundException
     * @throws IOException if keystore cannot be loaded
     * @throws NoSuchAlgorithmException is keystore cannot be loaded
     * @throws UnrecoverableKeyException if key cannot be loaded from keystore
     * @throws
     */
    public PrivateKey getPrivateKey(String alias) throws KeyStoreException {
        try {
            PrivateKey key = (PrivateKey)keystore.getKey(alias, keyProtectionDelegate.getPassword(alias));
            return key;
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException("Cannot load private key", e);
        }
    }
    
    public Certificate[] getCertificates(String alias) throws KeyStoreException {
        return keystore.getCertificateChain(alias);
    }

    public void set(String alias, PrivateKey key, Certificate[] certificates) throws KeyStoreException {
        if (keystore.containsAlias(alias)) {
            log.warn("Replacing private key {}", alias);
        }
        keystore.setKeyEntry(alias, key, keyProtectionDelegate.getPassword(alias), certificates);
        modified();
    }
    
}
