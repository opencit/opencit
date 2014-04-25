/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.ApiClientFactory;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.FileDAO;
import com.intel.mtwilson.tag.model.File;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.setup.SetupException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command exports a file from the database to the filesystem
 * @author jbuhacoff
 */
public class TagCreateMtWilsonClient extends TagCommand {
    private static Logger log = LoggerFactory.getLogger(TagCreateMtWilsonClient.class);
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
            mtwilsonClientKeystoreUsername = My.configuration().getTagKeystoreUsername(); // Global.configuration().getMtWilsonClientKeystoreUsername();
        }
        if( mtwilsonClientKeystorePassword == null || mtwilsonClientKeystorePassword.isEmpty() ) {
            mtwilsonClientKeystorePassword = My.configuration().getTagKeystorePassword(); //Global.configuration().getMtWilsonClientKeystorePassword();
        }
        
        // defaults:  username=asset-tag-prov-svc and password=random-16-chars ... XXX TODO maybe prompt for the username & url?  password can still be generated.
        if( mtwilsonUrl == null || mtwilsonUrl.isEmpty() ) {
            mtwilsonUrl = "https://127.0.0.1:8181";
        }
        if( mtwilsonClientKeystoreUsername == null || mtwilsonClientKeystoreUsername.isEmpty() ) {
            mtwilsonClientKeystoreUsername = "tagservice";
        }
        if( mtwilsonClientKeystorePassword == null || mtwilsonClientKeystorePassword.isEmpty() ) {
            mtwilsonClientKeystorePassword = RandomStringUtils.randomAlphanumeric(16);
        }

        URL url = new URL(mtwilsonUrl);
        String[] roles = new String[] { "Attestation", "Report","Whitelist", "AssetTagManagement" };
                
        ByteArrayResource keystoreResource = new ByteArrayResource();
//        SimpleKeystore keystore = new SimpleKeystore(keystoreResource, mtwilsonClientKeystorePassword);
//        TrustFirstCertificateTlsPolicy policy = new TrustFirstCertificateTlsPolicy(new KeystoreCertificateRepository(keystore));
        ApiClientFactory factory = new ApiClientFactory();
        // XXX TODO use the tls keystore and policy configured for the mt wilson server ... 
        SimpleKeystore keystore = factory.createUserInResource(keystoreResource, mtwilsonClientKeystoreUsername, mtwilsonClientKeystorePassword, url, new InsecureTlsPolicy(), roles);
        keystore.save();
        
        // save the keystore to database
        FileDAO fileDao = TagJdbi.fileDao();
        File keystoreFile = fileDao.findByName(KEYSTORE_FILE);
        if( keystoreFile == null ) {
            fileDao.insert(new UUID(), KEYSTORE_FILE, "application/x-java-keystore", keystoreResource.toByteArray());
        }
        else {
            fileDao.update(keystoreFile.getId(), keystoreFile.getName(), keystoreFile.getContentType(), keystoreResource.toByteArray());
        }
        fileDao.close();
        
        // display configuration so user can copy it to mtwilson.properties 
        // TODO:  when converting to a setup task this would just be set in the in-memory configuration 
        // and the application setup manager would write all the properties out to mtwilson.properties
        // file at the end of setup
        Properties p = new Properties();
        p.setProperty("mtwilson.api.url", mtwilsonUrl);
        p.setProperty("mtwilson.api.username", mtwilsonClientKeystoreUsername);
        p.setProperty("mtwilson.api.password", mtwilsonClientKeystorePassword);
        //p.store(System.out, "mtwilson.properties"); // user is responsible for copying this into mtwilson.properties (and it might be encrypted etc)

        RsaCredentialX509 rsaCredentialX509 = keystore.getRsaCredentialX509(mtwilsonClientKeystoreUsername, mtwilsonClientKeystorePassword);
        
        try {
            approveMtWilsonClient(rsaCredentialX509.identity());
            System.out.println(String.format("Approved %s [fingerprint %s]", mtwilsonClientKeystoreUsername, Hex.encodeHexString(rsaCredentialX509.identity())));        
         }
         catch(Exception e) {
             System.err.println(String.format("Failed to approve %s [fingerprint %s]: %s", mtwilsonClientKeystoreUsername, Hex.encodeHexString(rsaCredentialX509.identity()), e.getMessage()));
         }
        
        
    }
    
    private void approveMtWilsonClient(byte[] fingerprint) {
        try {
            System.out.println(String.format("Searching for client by fingerprint: %s", Hex.encodeHexString(fingerprint)));
            ApiClientX509JpaController x509jpaController = My.jpa().mwApiClientX509();
            ApiClientX509 client = x509jpaController.findApiClientX509ByFingerprint(fingerprint);
            if( client == null ) {
                log.error("Cannot find client record with fingerprint {}", Hex.encodeHexString(fingerprint));
                throw new IllegalStateException("Cannot find client record with fingerprint "+Hex.encodeHexString(fingerprint));
            }
            client.setStatus("Approved");
            client.setEnabled(true);
            x509jpaController.edit(client);
        }
        catch(Exception e) {
            throw new SetupException("Cannot update API Client record: "+e.getMessage(), e);
        }
    }
            
    
    
    public static void main(String args[]) throws Exception {
        TagCreateMtWilsonClient cmd = new TagCreateMtWilsonClient();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { });
        
    }    
    
}
