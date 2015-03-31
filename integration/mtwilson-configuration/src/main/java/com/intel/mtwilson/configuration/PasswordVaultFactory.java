/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.key.password.Password;
import com.intel.mtwilson.Environment;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.util.crypto.keystore.PasswordKeyStore;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author jbuhacoff
 */
public class PasswordVaultFactory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordVaultFactory.class);
    /**
     * The name of the configuration property whose value is the relative or absolute path to the password vault file
     */
    public static final String PASSWORD_VAULT_FILE_PROPERTY = "password.vault.file";
    
    /**
     * The name of the configuration property whose value is the password vault keystore type ("JKS", "JCEKS", ...)
     */
    public static final String PASSWORD_VAULT_TYPE_PROPERTY = "password.vault.type";
    
    /**
     * The name of the configuration property whose value indicates from where we can obtain the key to open the password vault ("environment", "smartcard", ...)
     * It only needs to be set if the configuration itself doesn't have the password.vault.key property
     */
    public static final String PASSWORD_VAULT_KEY_PROVIDER_PROPERTY = "password.vault.key.provider";
    
    /**
     * The name of the configuration property whose value contains the key to the password vault (the master password)
     * If there is a configuration setting for this property, then it is used as the key and the password.vault.key.provider setting is ignored
     */
    public static final String PASSWORD_VAULT_KEY_PROPERTY = "password.vault.key";
    
    public static PasswordKeyStore getPasswordKeyStore() throws KeyStoreException, IOException {
        return getPasswordKeyStore(ConfigurationFactory.getConfiguration());
    }
    
    public static PasswordKeyStore getPasswordKeyStore(Configuration configuration) throws KeyStoreException, IOException {
        PasswordVaultFactory factory = new PasswordVaultFactory(configuration);
        return factory.getPasswordVault();
    }
    
    private String keystorePath;
    private String keystoreType;
    private Password keystorePassword;
    
    public PasswordVaultFactory() throws IOException {
        this(ConfigurationFactory.getConfiguration());
    }
    
    public PasswordVaultFactory(Configuration configuration) {
        keystorePath = configuration.get(PASSWORD_VAULT_FILE_PROPERTY, Folders.configuration() + File.separator + "password-vault.jck");
        keystoreType = configuration.get(PASSWORD_VAULT_TYPE_PROPERTY, "JCEKS");
        keystorePassword = new Password(configuration.get(PASSWORD_VAULT_KEY_PROPERTY, "").toCharArray());
        if( keystorePassword.isEmpty() ) {
            String keyProvider = configuration.get(PASSWORD_VAULT_KEY_PROVIDER_PROPERTY, "environment");
            if( keyProvider.equalsIgnoreCase("environment") ) {
            // gets environment variable MTWILSON_PASSWORD, TRUSTAGENT_PASSWORD, KMS_PASSWORD, etc.
                keystorePassword = new Password(Environment.get("PASSWORD", "").toCharArray());
            }
        }
    }
    
    public PasswordKeyStore getPasswordVault() throws KeyStoreException, IOException {
        try {
            return new PasswordKeyStore(keystoreType, new File(keystorePath), keystorePassword.toCharArray());
        }
        catch(NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        }
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public Password getKeystorePassword() {
        return keystorePassword;
    }
    
    
    
}
