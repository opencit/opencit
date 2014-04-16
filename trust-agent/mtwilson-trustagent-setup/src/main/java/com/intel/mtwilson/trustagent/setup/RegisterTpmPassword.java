/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.configuration.CompositeConfiguration;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.configuration.EnvironmentConfiguration;
import com.intel.dcsg.cpg.configuration.KeyTransformerConfiguration;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.PropertiesUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.util.AllCapsNamingStrategy;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.client.jaxrs.HostTpmPassword;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.CreateIdentity;
import com.intel.mtwilson.trustagent.niarl.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

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
        // TODO:  check if tpm password has been updated in mtwilson - 
        //        by saving the ETag we get from Mt Wilson and then
        //        looking for the same ETag from here.
        //        until that is done, user should always run this setup task
        //        with --force
        
        // TODO:  this should be consolidated in the v2 client abstract class  with use of TlsPolicyManager ; see also RequestEndorsementCertificat e and RequestAikCertificate
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        
        // check if mt wilson already knows the tpm owner secret
        HostTpmPassword client = new HostTpmPassword(config.getConfiguration());
        TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
        criteria.id = hostHardwareId;
        TpmPassword tpmPassword = client.retrieveTpmPassword(criteria);
        if( tpmPassword == null ) {
            validation("TPM Owner Secret not yet registered with Mt Wilson");
        }
        else {
            // mt wilson has a value for this, check if it's the same as ours
            if( etagCache.containsKey(TrustagentConfiguration.TPM_OWNER_SECRET) ) {
                String previousEtag = etagCache.getProperty(TrustagentConfiguration.TPM_OWNER_SECRET); 
                log.debug("The previous tag is {}", previousEtag);
                String currentEtag = tpmPassword.getEtag();
                if( currentEtag != null && !currentEtag.equalsIgnoreCase(previousEtag) ) {
                    validation("TPM Owner Secret was updated and should be re-registered in Mt Wilson");
                }
            }
        }
    }

    @Override
    protected void execute() throws Exception {
        // TODO:  this should be consolidated in the v2 client abstract class  with use of TlsPolicyManager ; see also RequestEndorsementCertificat e and RequestAikCertificate
        System.setProperty("javax.net.ssl.trustStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", config.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", config.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getTrustagentKeystorePassword());
        
        HostTpmPassword client = new HostTpmPassword(config.getConfiguration());
        String etag = client.storeTpmPassword(hostHardwareId, tpmOwnerSecretHex);
        if( etag != null && !etag.isEmpty() ) {
            etagCache.setProperty(TrustagentConfiguration.TPM_OWNER_SECRET, etag);
            try(FileOutputStream out = new FileOutputStream(etagCacheFile)) {
                etagCache.store(out, "automatically generated by setup");
            }
        }
    }
    
}
