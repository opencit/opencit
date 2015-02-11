/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.setup;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.dcsg.cpg.tls.policy.impl.AnyProtocolSelector;
import com.intel.mtwilson.attestation.client.jaxrs.CaCertificates;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author boskisha
 */
public class DownloadMtWilsonSamlCertificate extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DownloadMtWilsonSamlCertificate.class);

    private TrustagentConfiguration trustagentConfiguration;
    private String url;
    private String username;
    private String password;
    private File keystoreFile;
    private String keystorePassword;
    private SimpleKeystore keystore;
    
    @Override
    protected void configure() throws Exception {
        trustagentConfiguration = new TrustagentConfiguration(getConfiguration());
        url = trustagentConfiguration.getMtWilsonApiUrl();
        if( url == null || url.isEmpty() ) {
            configuration("Mt Wilson URL is not set");
        }
        username = trustagentConfiguration.getMtWilsonApiUsername();
        password = trustagentConfiguration.getMtWilsonApiPassword();
        if( username == null || username.isEmpty() ) {
            configuration("Mt Wilson username is not set");
        }
        if( password == null || password.isEmpty() ) {
            configuration("Mt Wilson password is not set");
        }
        keystoreFile = trustagentConfiguration.getTrustagentKeystoreFile();
        if( keystoreFile == null || !keystoreFile.exists() ) {
            configuration("Trust Agent keystore does not exist");
        }
        keystorePassword = trustagentConfiguration.getTrustagentKeystorePassword();
        if( keystorePassword == null || keystorePassword.isEmpty() ) {
            configuration("Trust Agent keystore password is not set");
        }
        keystore = new SimpleKeystore(new FileResource(keystoreFile), keystorePassword);
    }

    @Override
    protected void validate() throws Exception {
        try {
            X509Certificate samlCerttificate = keystore.getX509Certificate("saml", SimpleKeystore.CA);
            if(samlCerttificate == null){
                validation("Missing Saml Certificate");
            }
            if(samlCerttificate != null){
                log.debug("Found saml Certificate {}", Sha1Digest.digestOf(samlCerttificate.getEncoded()).toHexString());
            }
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            log.debug("Cannot load Saml certificate", e);
            validation("Cannot load Saml certificate", e);
        }
    }

    @Override
    protected void execute() throws Exception {
        /*
        // TODO:  this should be consolidated in the v2 client abstract class  with use of TlsPolicyManager ; see also RequestEndorsementCertificat e and RequestAikCertificate
        System.setProperty("javax.net.ssl.trustStore", trustagentConfiguration.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", trustagentConfiguration.getTrustagentKeystorePassword());
        System.setProperty("javax.net.ssl.keyStore", trustagentConfiguration.getTrustagentKeystoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", trustagentConfiguration.getTrustagentKeystorePassword());
        */
        log.debug("Downloading SAML certificate and adding it to the keystore");
        TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(trustagentConfiguration.getTrustagentKeystoreFile(), trustagentConfiguration.getTrustagentKeystorePassword()).build();
        TlsConnection tlsConnection = new TlsConnection(new URL(url), tlsPolicy);
        
        Properties clientConfiguration = new Properties();
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, username);
        clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, password);
        
        CaCertificates client = new CaCertificates(clientConfiguration, tlsConnection);
        X509Certificate certificate = client.retrieveCaCertificate("saml");
        keystore.addTrustedCaCertificate(certificate, "saml");
//        X509Certificate endorsementCertificate = client.retrieveCaCertificate("endorsement");
//        keystore.addTrustedCaCertificate(endorsementCertificate, "endorsement");
        keystore.save();
    }    
}

