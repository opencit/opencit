/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.FileDAO;
import com.intel.mtwilson.tag.model.File;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.datatypes.ApiClientUpdateRequest;
import com.intel.mtwilson.ms.business.ApiClientBO;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.v2.client.MwClientUtil;
import java.net.URL;
import java.util.Locale;
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
            mtwilsonUrl = My.configuration().getAssetTagServerString(); //My.configuration().getMtWilsonURL().toString(); // Global.configuration().getMtWilsonURL();
        }
        if( mtwilsonClientKeystoreUsername == null || mtwilsonClientKeystoreUsername.isEmpty() ) {
            mtwilsonClientKeystoreUsername = My.configuration().getTagKeystoreUsername(); // Global.configuration().getMtWilsonClientKeystoreUsername();
        }
        if( mtwilsonClientKeystorePassword == null || mtwilsonClientKeystorePassword.isEmpty() ) {
            mtwilsonClientKeystorePassword = My.configuration().getTagKeystorePassword(); //Global.configuration().getMtWilsonClientKeystorePassword();
        }
        
        // defaults:  username=asset-tag-prov-svc and password=random-16-chars 
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

        
        ByteArrayResource keystoreResource = new ByteArrayResource();
        
        try (FileDAO fileDao = TagJdbi.fileDao()) {
            File keystoreFile = fileDao.findByName(KEYSTORE_FILE);
            if (keystoreFile != null) {
                System.out.println(String.format("%s keystore and user login certificate already exist", mtwilsonClientKeystoreUsername));
                return;
            }
        }

        //set properties
        Properties properties = My.configuration().getClientProperties();
        properties.setProperty("mtwilson.api.url", mtwilsonUrl);

        //create keystore and users
        Extensions.register(TlsPolicyCreator.class, com.intel.mtwilson.tls.policy.creator.impl.CertificateDigestTlsPolicyCreator.class);
        SimpleKeystore keystore = MwClientUtil.createUserInResourceV2(keystoreResource, mtwilsonClientKeystoreUsername, mtwilsonClientKeystorePassword,
                url, properties, "tagservice user for asset tag functionality", Locale.ENGLISH, "TLSv1.2");
        keystore.save();

        //approve users
        RsaCredentialX509 rsaCredentialX509 = keystore.getRsaCredentialX509(mtwilsonClientKeystoreUsername, mtwilsonClientKeystorePassword);
        String[] roles = new String[]{"Attestation", "Report", "Whitelist", "AssetTagManagement"};
        try {
            ApiClientUpdateRequest updateRequest = new ApiClientUpdateRequest();
            updateRequest.enabled = true;
            updateRequest.fingerprint = rsaCredentialX509.identity();
            updateRequest.roles = roles;
            updateRequest.status = "APPROVED";
            ApiClientBO apiClientBO = new ApiClientBO();
            apiClientBO.updateV2(updateRequest);
            System.out.println(String.format("Approved %s [fingerprint %s]", mtwilsonClientKeystoreUsername, Hex.encodeHexString(rsaCredentialX509.identity())));   
        } catch (Exception e) {
            System.err.println(String.format("Failed to approve %s [fingerprint %s]: %s", mtwilsonClientKeystoreUsername, Hex.encodeHexString(rsaCredentialX509.identity()), e.getMessage()));
            throw new IllegalStateException(String.format("Failed to approve %s [fingerprint %s]: %s", mtwilsonClientKeystoreUsername, Hex.encodeHexString(rsaCredentialX509.identity()), e.getMessage()));
        }
        
        try (FileDAO fileDao = TagJdbi.fileDao()) {
            fileDao.insert(new UUID(), KEYSTORE_FILE, "application/x-java-keystore", keystoreResource.toByteArray());
            System.out.println(String.format("Saved %s keystore to database", mtwilsonClientKeystoreUsername));
        }
    }
    
    /***** UNUSED
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
        catch(IOException | IllegalStateException | IllegalOrphanException | NonexistentEntityException | MSDataException e) {
            throw new SetupException("Cannot update API Client record: "+e.getMessage(), e);
        }
    }
        
    private void approveUserLoginCertificate(LoginDAO loginDAO, String username) {
        UserLoginCertificate userLoginCertificate = loginDAO.findUserLoginCertificateByUsername(username);
        loginDAO.updateUserLoginCertificateById(userLoginCertificate.getId(), true, Status.APPROVED, "");        
    }*/
            
    
    
    public static void main(String args[]) throws Exception {
        TagCreateMtWilsonClient cmd = new TagCreateMtWilsonClient();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[] { });
        
    }    
    
}
