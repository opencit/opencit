/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.ReadonlyConfiguration;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.pem.Pem;
import com.intel.mtwilson.Environment;
import com.intel.mtwilson.Folders;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

/**
 * The ConfigurationFactory is used to locate and read the application's
 * configuration file, which may be encrypted.
 *
 * @author jbuhacoff
 */
public class ConfigurationFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationFactory.class);

    private static final String PASSWORD = "PASSWORD"; // transforms into MTWILSON_PASSWORD, KMS_PASSWORD, etc. environment variables

    private static ConfigurationProvider provider;
    private static Configuration configuration;

    public static File getConfigurationFile() {
        String path = Folders.configuration();
        String filename = System.getProperty("mtwilson.configuration.file", "mtwilson.properties");  // kms overrides with "kms.conf" for example
        File file = new File(path + File.separator + filename);
        return file;
    }

    /**
     * Get a read-only view of the current configuration. If you need to 
     * edit the configuration, use {@code getConfigurationProvider()} instead.
     * @return
     * @throws IOException 
     */
    public static Configuration getConfiguration() throws IOException {
        if (configuration == null) {
            // because extensions cache may not be loaded at this time,
            // any plugins that implement configuration providers must
            // register themselves via the Java Service Provider Interface

            // find the appropriate provider and store a read-only configuration
            configuration = new ReadonlyConfiguration(getConfigurationProvider().load());
        }
        return configuration;
    }
    
    /**
     * Get the active configuration provider, from which you can
     * load and reload the configuration, and store any changes to the
     * configuration.
     * @return
     * @throws IOException 
     */
    public static ConfigurationProvider getConfigurationProvider() throws IOException {
        if( provider == null ) {
            // locate the configuration file
            File file = getConfigurationFile();
            if( file.exists() ) {
                // load existing configuration
                try (FileInputStream in = new FileInputStream(file)) {
                    String content = IOUtils.toString(in);
                    // 2. detect if it's encrypted: it would start with something like -----BEGIN ENCRYPTED DATA----- and ends with -----END ENCRYPTED DATA-----

                    if (Pem.isPem(content)) {
                        // 3. read encrypted configuration
                // password environment variable name like MTWILSON_PASSWORD, TRUSTAGENT_PASSWORD, KMS_PASSWORD, etc.
                        String password = Environment.get(PASSWORD);
    //                    ByteArrayResource resource = new ByteArrayResource(content.getBytes(Charset.forName("UTF-8")));
                        provider = new EncryptedConfigurationProvider(new FileResource(file), password);
                    } else {
                        // 3. read plain configuration
    //                    ByteArrayResource resource = new ByteArrayResource(content.getBytes(Charset.forName("UTF-8")));
                        provider = new ResourceConfigurationProvider(new FileResource(file));
                    }
                }
            }
            else {
                // create new configuration; if a password variable is set, assume the configuration should be encrypted
                // password environment variable name like MTWILSON_PASSWORD, TRUSTAGENT_PASSWORD, KMS_PASSWORD, etc.
                String password = Environment.get(PASSWORD);
                if( password == null ) {
                    provider = new EmptyConfigurationProvider(new ResourceConfigurationProvider(new FileResource(file)));
                }
                else {
                    provider = new EmptyConfigurationProvider(new EncryptedConfigurationProvider(new FileResource(file), password));
                }
            }
        }
        return provider;
    }
}