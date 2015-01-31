/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.MyConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class KmsConfigurationProviderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KmsConfigurationProviderTest.class);
    private FileResource testResource = new FileResource(new File("target"+File.separator+"test.properties"));
    private String testPassword = "password";
    
    /**
     * This test is a concise setup of the password encrypted file
     * Essentially the same thing is done by EncryptedConfigurationProvider
     * 
     * @throws ConfigurationException
     * @throws IOException 
     */
    @Test
    public void testWritencryptedFile() throws ConfigurationException, IOException {
        // sample data
        PropertiesConfiguration p = new PropertiesConfiguration();
        p.set("foo", "bar");
        // setup password protected file
        PasswordProtection protection = PasswordProtectionBuilder.factory().aes(256).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        if( !protection.isAvailable() ) {
//                log.warn("Protection algorithm {} key length {} mode {} padding {} not available", protection.getAlgorithm(), protection.getKeyLengthBits(), protection.getMode(), protection.getPadding());
            protection = PasswordProtectionBuilder.factory().aes(128).block().sha256().pbkdf2WithHmacSha1().saltBytes(8).iterations(1000).build();
        }
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(testResource, testPassword, protection);
        StringWriter writer = new StringWriter();
        p.getProperties().store(writer, "test");
        encryptedFile.saveString(writer.toString());
    }
    
    /**
     * This test is a concise setup of the password encrypted file
     * Essentially the same thing is done by EncryptedConfigurationProvider
     * 
     * @throws ConfigurationException
     * @throws IOException 
     */    
    @Test
    public void testReadEncryptedFile() throws IOException, ConfigurationException {
        PasswordEncryptedFile encryptedFile = new PasswordEncryptedFile(testResource, testPassword);
        String content = encryptedFile.loadString();
        StringReader reader = new StringReader(content);
        PropertiesConfiguration p = new PropertiesConfiguration();
        p.getProperties().load(reader);
        // show the entire configuration
        for(String key : p.keys()) {
            log.debug("Key: {}", key);
        }
    }
    
    @Test
    public void testCreateNewConfiguration() throws IOException {
        MyConfiguration configuration = new MyConfiguration();
        if( configuration.getConfigurationFile().exists() ) {
            configuration.getConfigurationFile().delete();
        }
        assertFalse(configuration.getConfigurationFile().exists());
        EncryptedConfigurationProvider provider = new EncryptedConfigurationProvider(configuration.getConfigurationFile(), "password");
        Configuration conf = provider.load();
        conf.set("foo", "bar");
        provider.save(conf);
        assertTrue(configuration.getConfigurationFile().exists());
    }
    
    @Test
    public void testLoadExistingConfiguration() throws IOException {
        testCreateNewConfiguration();
        MyConfiguration configuration = new MyConfiguration();
        assertTrue(configuration.getConfigurationFile().exists());
        EncryptedConfigurationProvider provider = new EncryptedConfigurationProvider(configuration.getConfigurationFile(), "password");
        Configuration conf = provider.load();
        assertEquals("bar", conf.get("foo"));
        // show the entire configuration
        for(String key : conf.keys()) {
            log.debug("Key: {}", key);
        }
    }
}
