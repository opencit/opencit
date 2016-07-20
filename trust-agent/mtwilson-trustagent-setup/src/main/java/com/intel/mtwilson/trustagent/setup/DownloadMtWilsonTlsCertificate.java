/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.impl.AnyProtocolSelector;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Prerequisites:  Trust Agent Keystore must already be created
 * 
 * @author jbuhacoff
 */
public class DownloadMtWilsonTlsCertificate extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DownloadMtWilsonTlsCertificate.class);

    private TrustagentConfiguration trustagentConfiguration;
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;
    private SimpleKeystore keystore;
    private List<String> approved;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        url = trustagentConfiguration.getMtWilsonApiUrl();
        if( url == null || url.isEmpty() ) {
            configuration("Mt Wilson URL is not set: mtwilson.api.url");
        }
        username = trustagentConfiguration.getMtWilsonApiUsername();
        password = trustagentConfiguration.getMtWilsonApiPassword();
        if( username == null || username.isEmpty() ) {
            configuration("Mt Wilson username is not set: mtwilson.api.username");
        }
        if( password == null || password.isEmpty() ) {
            configuration("Mt Wilson password is not set: mtwilson.api.password");
        }
        keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        if( keystoreFile == null || !keystoreFile.exists() ) {
            configuration("Trust Agent keystore does not exist");
        }
        keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        if( keystorePassword == null || keystorePassword.isEmpty() ) {
            configuration("Trust Agent keystore password is not set");
        }
        approved = trustagentConfiguration.getMtWilsonTlsCertificateFingerprints();
        if( approved.isEmpty() ) {
            configuration("One or more approved TLS certificate fingerprints must be set in MTWILSON_TLS_CERT_SHA256; comma-separated values ok");
        }
        keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
    }

    @Override
    protected void validate() throws Exception {
        try {
            X509Certificate[] certificates = keystore.getTrustedCertificates(SimpleKeystore.SSL);
            if( certificates.length == 0 ) {
                validation("Keystore does not contain any Mt Wilson TLS certificates");
            }
            else {
                // check to see if any pre-approved certificates are NOT already in our keystore
                // that's not necessarily an error condition (the approved certs list may be for multiple servers)
                // but it's an indicator that we should execute this task to ensure that we have the most
                // current certs
                ArrayList<String> present = new ArrayList<>();
                for(X509Certificate certificate : certificates) {
                    String fingerprint = Sha256Digest.digestOf(certificate.getEncoded()).toHexString();
                    present.add(fingerprint);
                }
                if( !containsAny(present, approved) ) {
                    validation("Keystore does not contain any approved TLS certificates");
                }
            }
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            validation("Cannot load certificates from keystore");
        }
    }
    
    private boolean containsAny(List<String> list, List<String> any) {
        for(String item : any) {
            if( list.contains(item) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void execute() throws Exception {
        X509Certificate[] certificates = TlsUtil.getServerCertificates(new URL(url), new AnyProtocolSelector());
        ArrayList<X509Certificate> rejected = new ArrayList<>();
        ArrayList<X509Certificate> accepted = new ArrayList<>();
        for(X509Certificate certificate : certificates) {
            String fingerprint = Sha256Digest.digestOf(certificate.getEncoded()).toHexString();
            if( approved.contains(fingerprint)) {
                log.debug("Server certificate {} is approved", fingerprint);
                keystore.addTrustedSslCertificate(certificate, fingerprint);
                accepted.add(certificate);
            }
            else {
                log.debug("Server certificate {} is rejected", fingerprint);
                rejected.add(certificate);
            }
        }
        if( !accepted.isEmpty() ) {
            keystore.save();
        }
        if( accepted.isEmpty() && !rejected.isEmpty() ) {
            log.warn("All server certificates were rejected; check MTWILSON_TLS_CERT_SHA256");
        }
    }
    
}
