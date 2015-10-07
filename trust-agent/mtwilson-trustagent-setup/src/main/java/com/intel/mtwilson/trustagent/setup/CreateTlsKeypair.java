/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jbuhacoff
 */
public class CreateTlsKeypair extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTlsKeypair.class);

    private TrustagentConfiguration trustagentConfiguration;
    private static final String TLS_ALIAS = "tls";
    
    private String dn;
    private String[] ip;
    private String[] dns;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        dn = trustagentConfiguration.getTrustagentTlsCertDn();
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        // we need to know our own local ip addresses/hostname in order to add them to the ssl cert
        ip = trustagentConfiguration.getTrustagentTlsCertIpArray();
        dns = trustagentConfiguration.getTrustagentTlsCertDnsArray();
        if( dn == null || dn.isEmpty() ) { configuration("DN not configured"); }
        if( keystorePassword == null || keystorePassword.isEmpty() ) { configuration("Keystore password has not been generated"); }
        // NOTE: keystore file itself does not need to be checked, we will create it automatically in execute() if it does not exist
        if( (ip == null ? 0 : ip.length) + (dns == null ? 0 : dns.length) == 0 ) {
            configuration("At least one IP or DNS alternative name must be configured");
        }
    }

    @Override
    protected void validate() throws Exception {
        File keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        if( !keystoreFile.exists() ) {
            validation("Keystore file was not created");
            return;
        }
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        RsaCredentialX509 credential;
        try {
            credential = keystore.getRsaCredentialX509(TLS_ALIAS, keystorePassword);
            if (credential == null || credential.getCertificate() == null 
                    || credential.getCertificate().getSubjectX500Principal() == null 
                    || credential.getCertificate().getSubjectX500Principal().getName() == null) {
                log.debug("Invalid TLS certificate: credential contains null value");
                validation("Certificate must be recreated: credential contains null value");
                return;
            }
            log.debug("Found TLS key {}", credential.getCertificate().getSubjectX500Principal().getName());
        } catch (FileNotFoundException e) {
            log.warn("Keystore does not contain the specified key [{}]", TLS_ALIAS);
            validation("Keystore does not contain the specified key %s", TLS_ALIAS);
            return;
        }
        catch(java.security.UnrecoverableKeyException e) {
            log.debug("Incorrect password for existing key; will create new key: {}", e.getMessage());
            validation("Key must be recreated");
            return;
        }
//        catch(NullPointerException e) {
//            log.debug("Invalid TLS certificate");
//            validation("Certificate must be recreated");
//            return;
//        }
//        log.debug("credential {}", credential);
//        log.debug("credential certificate {}", credential.getCertificate());
//        log.debug("credential certificate encoded {}", credential.getCertificate().getEncoded());
//        log.debug("credential certificate encoded sha1 {}", Sha1Digest.digestOf(credential.getCertificate().getEncoded()));
//        log.debug("Keystore contains TLS keypair: ", Sha1Digest.digestOf(credential.getCertificate().getEncoded()).toHexString());
        /*
         * Issue #2141 thi
        if( !dn.equals(credential.getCertificate().getSubjectX500Principal().getName()) ) {
            log.debug("Certificate DN not the same as configured DN; should recreate certificate");
            validation("Configured DN does not match certificate DN; should recreate certificate");
        }
        */
    }

    @Override
    protected void execute() throws Exception {
        File keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        // create the keypair
        KeyPair keypair = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory()
                .selfSigned(dn, keypair)
                .expires(3650, TimeUnit.DAYS) 
                .keyUsageKeyEncipherment();
        // NOTE:  right now we are creating a self-signed cert but if we have
        //        the mtwilson api url, username, and password, we could submit
        //        a certificate signing request there and have our cert signed
        //        by mtwilson's ca, and then the ssl policy for this host in 
        //        mtwilson could be "signed by trusted ca" instead of
        //        "that specific cert"
        if( ip != null ) {
            for(String san : ip) {
                log.debug("Adding Subject Alternative Name (SAN) with IP address: {}", san);
                builder.ipAlternativeName(san.trim());
            }
        }
        if( dns != null ) {
            for(String san : dns) {
                log.debug("Adding Subject Alternative Name (SAN) with Domain Name: {}", san);
                builder.dnsAlternativeName(san.trim());
            }
        }
        X509Certificate tlscert = builder.build();
        // look for an existing tls keypair and delete it
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        try {
//            String alias = String.format("%s (ssl)", TLS_ALIAS);
            String alias = TLS_ALIAS;
            List<String> aliases = Arrays.asList(keystore.aliases());
            if( aliases.contains(alias) ) {
                keystore.delete(alias);
            }
        }
        catch(KeyStoreException | KeyManagementException e) {
            log.debug("Cannot remove existing tls keypair", e);
        }
        // store it in the keystore
        keystore.addKeyPairX509(keypair.getPrivate(), tlscert, TLS_ALIAS, keystorePassword);
        keystore.save();
        
        // save the settings in configuration
        getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_TLS_CERT_DN, dn);
        if( ip != null ) {
            getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_TLS_CERT_IP, StringUtils.join(ip, ","));
        }
        if( dns != null ) {
            getConfiguration().set(TrustagentConfiguration.TRUSTAGENT_TLS_CERT_DNS, StringUtils.join(dns, ","));
        }
    }
    
}
