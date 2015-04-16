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
import java.security.UnrecoverableKeyException;
import javax.crypto.SecretKey;

/**
 * A wrapper around java.security.KeyStore that specializes in SecretKey entries
 * and simplifies use of the keystore.
 * 
 * @author jbuhacoff
 */
public class SecretKeyStore extends AbstractKeyStore implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecretKeyStore.class);
//    private char[] keystorePassword;
    private KeyStore keystore;
    private KeyProtectionDelegate keyProtectionDelegate;

    /**
     * 
     * @param keystoreType should be JCEKS or other keystore type which is capable of storing SecretKey entries
     * @param keystoreFile
     * @param keystorePassword
     * @throws KeyStoreException
     * @throws IOException 
     */
    public SecretKeyStore(String keystoreType, File keystoreFile, char[] keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreFile, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }
    public SecretKeyStore(String keystoreType, Resource keystoreResource, char[] keystorePassword) throws KeyStoreException, IOException {
        super(keystoreType, keystoreResource, keystorePassword);
//        super.setKeyProtectionDelegate(new SinglePasswordKeyProtectionDelegate(keystorePassword));
        this.keystore = super.keystore();
//        this.keystorePassword = keystorePassword;
        this.keyProtectionDelegate = new SinglePasswordKeyProtectionDelegate(keystorePassword);
    }
    public SecretKeyStore(String keystoreType, Resource keystoreResource, Password keystorePassword) throws KeyStoreException, IOException {
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
    public SecretKey get(String alias) throws KeyStoreException {
        try {
            SecretKey key = (SecretKey) keystore.getKey(alias, keyProtectionDelegate.getPassword(alias)); // throws UnrecoverableKeyException
            return key;
        } catch (GeneralSecurityException e) {
            throw new KeyStoreException("Cannot load key", e);
        }
    }

    public void set(String alias, SecretKey key) throws KeyStoreException {
        if (keystore.containsAlias(alias)) {
            log.warn("Replacing key {}", alias);
        }
        keystore.setEntry(alias, new KeyStore.SecretKeyEntry(key), new KeyStore.PasswordProtection(keyProtectionDelegate.getPassword(alias)));
        modified();
    }
    
    
}
