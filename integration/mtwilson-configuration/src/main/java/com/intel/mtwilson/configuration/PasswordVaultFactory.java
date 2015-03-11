/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
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
    public static final String PASSWORD_VAULT_FILE_PROPERTY = "password.vault.file";
    public static final String PASSWORD_VAULT_TYPE_PROPERTY = "password.vault.type";
    
    public static PasswordKeyStore getPasswordKeyStore() throws KeyStoreException, IOException {
        return getPasswordKeyStore(ConfigurationFactory.getConfiguration());
    }
    
    public static PasswordKeyStore getPasswordKeyStore(Configuration configuration) throws KeyStoreException, IOException {
        String keystorePath = configuration.get(PASSWORD_VAULT_FILE_PROPERTY, Folders.configuration() + File.separator + "password-vault.jck");
        String keystoreType = configuration.get(PASSWORD_VAULT_TYPE_PROPERTY, "JCEKS");
        char[] keystorePassword = Environment.get("PASSWORD", "").toCharArray();
        try {
            return new PasswordKeyStore(keystoreType, new File(keystorePath), keystorePassword);
        }
        catch(NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        }
    }
}
