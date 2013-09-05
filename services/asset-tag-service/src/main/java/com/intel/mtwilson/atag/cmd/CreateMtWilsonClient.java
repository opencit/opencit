/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.cmd;

import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.file.PasswordEncryptedFile;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.ApiClientFactory;
import com.intel.mtwilson.My;
import com.intel.mtwilson.atag.AtagCommand;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.Global;
import com.intel.mtwilson.atag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.atag.dao.jdbi.FileDAO;
import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.model.File;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.io.ByteArrayResource;
import com.intel.mtwilson.io.Resource;
import com.intel.mtwilson.tls.InsecureTlsPolicy;
import com.intel.mtwilson.tls.KeystoreCertificateRepository;
import com.intel.mtwilson.tls.TlsPolicy;
import com.intel.mtwilson.tls.TrustFirstCertificateTlsPolicy;
import java.io.FileNotFoundException;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class CreateMtWilsonClient extends AtagCommand {
    private static Logger log = LoggerFactory.getLogger(CreateMtWilsonClient.class);
    public static final String KEYSTORE_FILE = "mtwilson-client-keystore";
    
    @Override
    public void execute(String[] args) throws Exception {
        // file name, and either outfile or stdout
//        if( args.length < 1 ) { throw new IllegalArgumentException("Usage: create-mtwilson-client [--username=username] [--password=password] [--url=https://mtwilson.com]"); } // on Windows especially do not use single quotes around the argument because it will be a part of it

        // check if username/password is provided on command line or already configured ...
        String mtwilsonUrl = null, mtwilsonClientKeystoreUsername = null, mtwilsonClientKeystorePassword = null;
        if( getOptions().containsKey("url") ) {
            mtwilsonUrl = getOptions().getString("url", "");
        }
        if( getOptions().containsKey("username") ) {
            mtwilsonClientKeystoreUsername = getOptions().getString("username", "");
        }
        if( getOptions().containsKey("password") ) {
            mtwilsonClientKeystorePassword = getOptions().getString("password", "");
        }
        
        // already configured
        if( mtwilsonUrl == null || mtwilsonUrl.isEmpty() ) {
            mtwilsonUrl = My.configuration().getMtWilsonURL().toString(); // Global.configuration().getMtWilsonURL();
        }
        if( mtwilsonClientKeystoreUsername == null || mtwilsonClientKeystoreUsername.isEmpty() ) {
            mtwilsonClientKeystoreUsername = My.configuration().getKeystoreUsername(); // Global.configuration().getMtWilsonClientKeystoreUsername();
        }
        if( mtwilsonClientKeystorePassword == null || mtwilsonClientKeystorePassword.isEmpty() ) {
            mtwilsonClientKeystorePassword = My.configuration().getKeystorePassword(); //Global.configuration().getMtWilsonClientKeystorePassword();
        }
        
        // defaults:  username=asset-tag-prov-svc and password=random-16-chars ... XXX TODO maybe prompt for the username & url?  password can still be generated.
        if( mtwilsonUrl == null || mtwilsonUrl.isEmpty() ) {
            mtwilsonUrl = "https://127.0.0.1:8181";
        }
        if( mtwilsonClientKeystoreUsername == null || mtwilsonClientKeystoreUsername.isEmpty() ) {
            mtwilsonClientKeystoreUsername = "asset-tag-prov-svc";
        }
        if( mtwilsonClientKeystorePassword == null || mtwilsonClientKeystorePassword.isEmpty() ) {
            mtwilsonClientKeystorePassword = RandomStringUtils.randomAlphanumeric(16);
        }

        URL url = new URL(mtwilsonUrl);
        String[] roles = new String[] { "AssetTagManagement" };
        ByteArrayResource keystoreResource = new ByteArrayResource();
//        SimpleKeystore keystore = new SimpleKeystore(keystoreResource, mtwilsonClientKeystorePassword);
//        TrustFirstCertificateTlsPolicy policy = new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(keystore));
        ApiClientFactory factory = new ApiClientFactory();
        SimpleKeystore keystore = factory.createUserInResource(keystoreResource, mtwilsonClientKeystoreUsername, mtwilsonClientKeystorePassword, url, new InsecureTlsPolicy(), roles);
        keystore.save();
        
        // save the keystore to database
        FileDAO fileDao = Derby.fileDao();
        File keystoreFile = fileDao.findByName(KEYSTORE_FILE);
        if( keystoreFile == null ) {
            fileDao.insert(new UUID(), KEYSTORE_FILE, "application/x-java-keystore", keystoreResource.toByteArray());
        }
        else {
            fileDao.update(keystoreFile.getId(), keystoreFile.getName(), keystoreFile.getContentType(), keystoreResource.toByteArray());
        }
        fileDao.close();
        
        // save configuration 
        Properties p = new Properties();
        p.setProperty("mtwilson.api.url", mtwilsonUrl);
        p.setProperty("mtwilson.api.username", mtwilsonClientKeystoreUsername);
        p.setProperty("mtwilson.api.password", mtwilsonClientKeystorePassword);
        p.store(System.out, "mtwilson.properties"); // user is responsible for copying this into mtwilson.properties (and it might be encrypted etc)

        Derby.stopDatabase();
    }
    
    
    public static void main(String args[]) throws Exception {
        CreateMtWilsonClient cmd = new CreateMtWilsonClient();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { });
        
    }    
    
}
