/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.setup;

import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.mtwilson.configuration.PasswordVaultFactory;
import com.intel.mtwilson.setup.AbstractSetupTask;
import java.io.File;
import java.security.KeyStoreException;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.io.IOException;

/**
 * Creates a password vault file used to store various passwords needed 
 * internally by the key server.
 * 
 * The password vault is itself password-protected by the master password
 * in the KMS_PASSWORD environment variable.
 * 
 * The password vault file path and keystore type (JCEKS) are written to
 * the application's configuration.
 * 
 * The JCEKS key store provider is able to store password entries (PBE with
 * no salt and no iterations).
 *
 * @author jbuhacoff
 */
public class PasswordVault extends AbstractSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordVault.class);
    private String keystorePath;
    private String keystoreType; // JCEKS
    private File keystoreFile;
    private Password keystorePassword;
    private PasswordVaultFactory passwordVaultFactory;

//    private String storageKeyAlgorithm;
//    private int storageKeyLengthBits;
    @Override
    protected void configure() throws Exception {
        passwordVaultFactory = new PasswordVaultFactory(getConfiguration());
        keystorePath = passwordVaultFactory.getKeystorePath(); //getConfiguration().get(PasswordVaultFactory.PASSWORD_VAULT_FILE_PROPERTY, Folders.configuration() + File.separator + "password-vault.jck");
        keystoreType = passwordVaultFactory.getKeystoreType(); //getConfiguration().get(PasswordVaultFactory.PASSWORD_VAULT_TYPE_PROPERTY, "JCEKS");
        keystorePassword = passwordVaultFactory.getKeystorePassword(); //Environment.get("PASSWORD", "").toCharArray();

        keystoreFile = new File(keystorePath);

        if (keystoreFile.exists()) {
            // we only need to know the password if the file already exists
            // if user lost password, delete the file and we can recreate it
            // with a new random password
            if (keystorePassword.isEmpty() ) {
                configuration("Password vault exists but master password is missing");
            }
        }

    }

    @Override
    protected void validate() throws Exception {
        if (keystorePassword == null || keystorePassword.isEmpty() ) {
            validation("Password vault master password is missing");
            return;
        }
        if (!keystoreFile.exists()) {
            validation("Password vault was not created");
            return;
        }

        try (PasswordKeyStore keystore = new PasswordKeyStore(keystoreType, keystoreFile, keystorePassword.toCharArray())) {
            log.debug("Password vault has {} entries", keystore.aliases().size()); // empty keystore is ok too
        } catch (KeyStoreException | IOException e) {
            validation("Cannot open password vault", e);
        }

    }

    @Override
    protected void execute() throws Exception {
        // the password vault master password must be set by the user by exporting KMS_PASSWORD; we must not generate it here
        if (keystorePassword == null || keystorePassword.isEmpty() ) {
            throw new IllegalStateException("Password vault master password is missing");
        }

        // ensure directories exist
        if (!keystoreFile.getParentFile().exists()) {
            if (keystoreFile.getParentFile().mkdirs()) {
                log.debug("Created directory {}", keystoreFile.getParentFile().getAbsolutePath());
            }
        }

        // create an empty password vault
        try (PasswordKeyStore keystore = new PasswordKeyStore(keystoreType, keystoreFile, keystorePassword.toCharArray())) {
            log.debug("Password vault has {} entries", keystore.aliases().size()); // empty keystore is ok too
            keystore.modified(); // force to write out when we close it
        } catch (KeyStoreException | IOException e) {
            validation("Cannot open password vault", e);
        }
        
        // save the settings in configuration;  DO NOT SAVE MASTER KEY
        getConfiguration().set(PasswordVaultFactory.PASSWORD_VAULT_FILE_PROPERTY, keystorePath);
        getConfiguration().set(PasswordVaultFactory.PASSWORD_VAULT_TYPE_PROPERTY, keystoreType);
    }
}
