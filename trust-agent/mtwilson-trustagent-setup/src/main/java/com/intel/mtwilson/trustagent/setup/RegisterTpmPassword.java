/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.io.PropertiesUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.mtwilson.attestation.client.jaxrs.HostTpmPassword;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

/**
 *
 * @author jbuhacoff
 */
public class RegisterTpmPassword extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegisterTpmPassword.class);
    private TrustagentConfiguration config;
    private String tpmOwnerSecretHex;
    private UUID hostHardwareId;
    private File etagCacheFile;
    private Properties etagCache;
    private String url;
    private String username;
    private String password;
    
    @Override
    protected void configure() throws Exception {
        config = new TrustagentConfiguration(getConfiguration());
        
        url = config.getMtWilsonApiUrl();
        username = config.getMtWilsonApiUsername();
        password = config.getMtWilsonApiPassword();
        if (url == null || url.isEmpty()) {
            configuration("Mt Wilson URL [mtwilson.api.url] must be set");
        }
        if (username == null || username.isEmpty()) {
            configuration("Mt Wilson username [mtwilson.api.username] must be set");
        }
        if (password == null || password.isEmpty()) {
            configuration("Mt Wilson password [mtwilson.api.password] must be set");
        }
        
        tpmOwnerSecretHex = config.getTpmOwnerSecretHex();
        if( tpmOwnerSecretHex == null || tpmOwnerSecretHex.isEmpty()) {
            configuration("TPM Owner Secret [tpm.owner.secret] must be set");
        }

        etagCacheFile = config.getTrustagentEtagCacheFile();
        if( etagCacheFile.exists() ) {
            etagCache = PropertiesUtil.loadExisting(etagCacheFile);
        }
        else {
            etagCache = new Properties();
        }
        String hostHardwareIdHex = config.getHardwareUuid();
        if( hostHardwareIdHex == null || hostHardwareIdHex.isEmpty() || !UUID.isValid(hostHardwareIdHex) ) {
            configuration("Host hardware UUID [hardware.uuid] must be set");
        }
        else {
            hostHardwareId = UUID.valueOf(hostHardwareIdHex);
        }
        
        // these properties are used in validate() and execute() and must be defined
        if( config.getTrustagentKeystoreFile() == null ) {
            configuration("Keystore file is not set");
        }
        if( config.getTrustagentKeystorePassword() == null ) {
            configuration("Keystore password is not set");
        }
    }

    @Override
    protected void validate() throws Exception {
        //        by saving the ETag we get from Mt Wilson and then
        //        looking for the same ETag from here.
        //        until that is done, user should always run this setup task
        //        with --force
        
        /*
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        */
        log.debug("RegisterTpmPassword.validate creating strict TLS Policy using keystore");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(config.getTrustagentKeystoreFile(), config.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);

        // check if mt wilson already knows the tpm owner secret
        HostTpmPassword client = new HostTpmPassword(clientConfiguration, tlsConnection);
//        TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
//        criteria.id = hostHardwareId;
//        TpmPassword tpmPassword = client.retrieveTpmPassword(criteria);
        TpmPassword tpmPassword;
        try {
            tpmPassword = client.retrieveTpmPassword(hostHardwareId);
            if(tpmPassword == null){
                validation("TPM Owner Secret is not registered with Mt Wilson");
                return;
            }
        } catch (Exception e) {
//            log.debug("Error while retrieving tpm password for {}: {}", hostHardwareId, e.getMessage());
            validation(e, "Cannot determine if TPM Owner Secret is registered with Mt Wilson");
            return;
        }
        // mt wilson has a value for this, check if it's the same as ours
        if (etagCache.containsKey(TrustagentConfiguration.TPM_OWNER_SECRET)) {
            String previousEtag = etagCache.getProperty(TrustagentConfiguration.TPM_OWNER_SECRET);
            log.debug("The previous tag is {}", previousEtag);
            String currentEtag = tpmPassword.getEtag();
            if (currentEtag != null && !currentEtag.equalsIgnoreCase(previousEtag)) {
                validation("TPM Owner Secret was updated and should be re-registered in Mt Wilson");
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        /*
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        */
        
        log.debug("RegisterTpmPassword.execute creating strict TLS policy using keystore");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(config.getTrustagentKeystoreFile(), config.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);
        
        HostTpmPassword client = new HostTpmPassword(clientConfiguration, tlsConnection);
        String etag = client.storeTpmPassword(hostHardwareId, tpmOwnerSecretHex);
        if( etag != null && !etag.isEmpty() ) {
            etagCache.setProperty(TrustagentConfiguration.TPM_OWNER_SECRET, etag);
            try(FileOutputStream out = new FileOutputStream(etagCacheFile)) {
                etagCache.store(out, "automatically generated by setup");
            }
        }
    }
    
}
