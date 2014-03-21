/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.x509.X509Builder;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author jbuhacoff
 */
public class CreateTlsKeypair extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTlsKeypair.class);

    private TrustagentConfiguration trustagentConfiguration;
    private static final String TLS_ALIAS = "tls";
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        String dn = trustagentConfiguration.getTrustagentTlsCertDn();
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        // we need to know our own local ip addresses/hostname in order to add them to the ssl cert
        String[] ip = trustagentConfiguration.getTrustagentTlsCertIp();
        String[] dns = trustagentConfiguration.getTrustagentTlsCertDns();
        if( dn == null || dn.isEmpty() ) { configuration("DN not configured"); }
        if( keystorePassword == null || keystorePassword.isEmpty() ) { configuration("Keystore password has not been generated"); }
        // NOTE: keystore file itself does not need to be checked, we will create it automatically in execute() if it does not exist
        if( (ip == null ? 0 : ip.length) + (dns == null ? 0 : dns.length) == 0 ) {
            configuration("At least one IP or DNS alternative name must be configured");
        }
    }

    @Override
    protected void validate() throws Exception {
        String dn = trustagentConfiguration.getTrustagentTlsCertDn();
        File keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        RsaCredentialX509 credential = keystore.getRsaCredentialX509(TLS_ALIAS, keystorePassword);
//        log.debug("credential {}", credential);
//        log.debug("credential certificate {}", credential.getCertificate());
//        log.debug("credential certificate encoded {}", credential.getCertificate().getEncoded());
//        log.debug("credential certificate encoded sha1 {}", Sha1Digest.digestOf(credential.getCertificate().getEncoded()));
//        log.debug("Keystore contains TLS keypair: ", Sha1Digest.digestOf(credential.getCertificate().getEncoded()).toHexString());
        if( !dn.equals(credential.getCertificate().getSubjectX500Principal().getName()) ) {
            log.debug("Certificate DN not the same as configured DN; should recreate certificate");
            validation("Configured DN does not match certificate DN; should recreate certificate");
        }
        // TODO: check alternative names ip and dns of cert against configured
    }

    @Override
    protected void execute() throws Exception {
        String dn = trustagentConfiguration.getTrustagentTlsCertDn();
        File keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        String keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        // create the keypair
        KeyPair keypair = RsaUtil.generateRsaKeyPair(2048);
        X509Builder builder = X509Builder.factory()
                .selfSigned(dn, keypair)
                .expires(3650, TimeUnit.DAYS) // 10 years default ; TODO: make it configurable 
                .keyUsageKeyEncipherment();
        String[] ip = trustagentConfiguration.getTrustagentTlsCertIp();
        String[] dns = trustagentConfiguration.getTrustagentTlsCertDns();
        for(String san : ip) {
            builder.ipAlternativeName(san.trim());
        }
        for(String san : dns) {
            builder.dnsAlternativeName(san.trim());
        }
        X509Certificate tlscert = builder.build();
        // store in the keystore
        SimpleKeystore keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
        keystore.addKeyPairX509(keypair.getPrivate(), tlscert, TLS_ALIAS, keystorePassword);
        keystore.save();
    }
    
}
