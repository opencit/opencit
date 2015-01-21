/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.configuration;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.PropertiesConfiguration;
import com.intel.mtwilson.Filesystem;
import com.intel.mtwilson.configuration.EncryptedConfigurationProvider;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtection;
import com.intel.dcsg.cpg.crypto.key.password.PasswordProtectionBuilder;
import com.intel.dcsg.cpg.io.FileResource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;
//import org.apache.commons.configuration.Configuration;
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.PropertiesConfiguration;
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
        Filesystem fs = new Filesystem();
        if( fs.getConfigurationFile().exists() ) {
            fs.getConfigurationFile().delete();
        }
        assertFalse(fs.getConfigurationFile().exists());
        EncryptedConfigurationProvider provider = new EncryptedConfigurationProvider("password");
        Configuration conf = provider.load();
        conf.set("foo", "bar");
        provider.save(conf);
        assertTrue(fs.getConfigurationFile().exists());
    }
    
    @Test
    public void testLoadExistingConfiguration() throws IOException {
        testCreateNewConfiguration();
        Filesystem fs = new Filesystem();
        assertTrue(fs.getConfigurationFile().exists());
        EncryptedConfigurationProvider provider = new EncryptedConfigurationProvider("password");
        Configuration conf = provider.load();
        assertEquals("bar", conf.get("foo"));
        // show the entire configuration
        for(String key : conf.keys()) {
            log.debug("Key: {}", key);
        }
    }
}
