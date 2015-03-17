/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedResource;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import java.io.File;
import java.io.IOException;
//import org.apache.commons.configuration.Configuration;
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Requires an environment variable "KMS_PASSWORD" to be set.
 * Uses this password directly to decrypt the configuration file.
 * 
 * A later version may use the password to connect to a local
 * decryption service instead and obtain the decrypted configuration
 * from there.
 * 
 * @author jbuhacoff
 */
public class EncryptedConfigurationProvider implements ConfigurationProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EncryptedConfigurationProvider.class);
    private File file;
    private PasswordProtection protection;
    private ResourceConfigurationProvider delegate;

    /**
     * Configures the provider using the provided password. The provider
     * does not attempt to decrypt the configuration until you call {@code load()}
     * @param file containing the encrypted configuration
     * @param password to decrypt the file
     */
    public EncryptedConfigurationProvider(File file, String password) {
        this(new FileResource(file), password);
    }
    
    /**
     * Configures the provider using the provided password. The provider
     * does not attempt to decrypt the configuration until you call {@code load()}
     * @param resource containing the encrypted configuration
     * @param password to decrypt the file
     */
    public EncryptedConfigurationProvider(Resource resource, String password) {
        if( password == null || password.isEmpty() ) {
            throw new IllegalArgumentException("Password is required");
        }
        PasswordEncryptedResource encrypted = new PasswordEncryptedResource(resource, password, getProtection());
        delegate = new ResourceConfigurationProvider(encrypted);
    }
    
    private PasswordProtection getProtection() {
        if( protection == null ) {
            protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            // aes-256 is only available if administrator has enabled java's 
            // strong cryptography suite, so check it and downgrade to aes-128 if necessary
            if( !protection.isAvailable() ) {
    //                log.warn("Protection algorithm {} key length {} mode {} padding {} not available", protection.getAlgorithm(), protection.getKeyLengthBits(), protection.getMode(), protection.getPadding());
                protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
            }
        }
        return protection;
    }
    
    
    /**
     * Loads an existing encrypted configuration file; fails if there is not
     * already an encrypted file present
     * 
     * @return
     * @throws ConfigurationException
     * @throws IOException 
     * @throws 
     */
    @Override
    public Configuration load() throws IOException {
        return delegate.load();
    }

    /**
     * Saves the given configuration to an encrypted configuration file
     * @param configuration
     * @throws ConfigurationException
     * @throws IOException 
     */
    @Override
    public void save(Configuration configuration) throws IOException {
        delegate.save(configuration);
    }
    
}
