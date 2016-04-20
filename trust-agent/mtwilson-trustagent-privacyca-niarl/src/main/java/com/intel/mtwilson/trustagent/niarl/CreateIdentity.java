/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.niarl;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.tls.policy.TlsPolicyBuilder;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.client.jaxrs.PrivacyCA;
import com.intel.mtwilson.configuration.Configurable;
import com.intel.mtwilson.privacyca.v2.model.IdentityBlob;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallenge;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeRequest;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeResponse;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.IdentityOS;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmPubKey;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

/**
 * Request AIK Certificate from Mt Wilson Privacy CA
 * 
 * Pre-requisites:
 * Owner, SRK, and AIK secrets must already be generated and present in the
 * configuration;  EC must have already been provisioned by ProvisionTPM
 * 
 * @author jbuhacoff
 */
public class CreateIdentity implements Configurable, Runnable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateIdentity.class);
    
    private Configuration configuration = null;
    
    @Override
    public void configure(Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public void run() {
        try {
            // load the PCA certificate
            TrustagentConfiguration config = new TrustagentConfiguration(configuration);
            SimpleKeystore keystore = new SimpleKeystore(new FileResource(config.getTrustagentKeystoreFile()), config.getTrustagentKeystorePassword());
            X509Certificate privacy = keystore.getX509Certificate("privacy", SimpleKeystore.CA);
            
            // encrypt the EC using the PCA's public key
            byte[] ekCert = TpmModule.getCredential(config.getTpmOwnerSecret(), "EC");
            TpmIdentityRequest encryptedEkCert = new TpmIdentityRequest(ekCert, (RSAPublicKey) privacy.getPublicKey(), false);
            
            // create the identity request
            boolean shortcut = true;
            String HisIdentityLabel = "HIS Identity Key"; 
            TpmIdentity newId = TpmModule.collateIdentityRequest(config.getTpmOwnerSecret(), config.getAikSecret(), HisIdentityLabel, new TpmPubKey((RSAPublicKey) privacy.getPublicKey(), 3, 1).toByteArray(), config.getAikIndex(), (X509Certificate) null, !shortcut);
//             TpmKey aik = new TpmKey(newId.getAikBlob());
            
//            HttpsURLConnection.setDefaultHostnameVerifier((new InsecureTlsPolicy()).getHostnameVerifier()); 

            TlsPolicy tlsPolicy = TlsPolicyBuilder.factory().strictWithKeystore(config.getTrustagentKeystoreFile(), config.getTrustagentKeystorePassword()).build();
            TlsConnection tlsConnection = new TlsConnection(new URL(config.getMtWilsonApiUrl()), tlsPolicy);

            Properties clientConfiguration = new Properties();
            clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_USERNAME, config.getMtWilsonApiUsername());
            clientConfiguration.setProperty(TrustagentConfiguration.MTWILSON_API_PASSWORD, config.getMtWilsonApiPassword());
            
            // send the identity request to the privacy ca to get a challenge
            PrivacyCA client = new PrivacyCA(clientConfiguration, tlsConnection);
            
            IdentityChallengeRequest request = new IdentityChallengeRequest();
            request.setEndorsementCertificate(encryptedEkCert.toByteArray());
            request.setIdentityRequest(newId.getIdentityRequest());
            
            IdentityChallenge identityChallenge = client.identityChallengeRequest(request);
            byte[] challenge = identityChallenge.getIdentityChallenge();
            
            // answer the challenge
            int os = IdentityOS.osType();//return os type. win:0; linux:1; other:-1
            byte[] asym1 = new byte[256];
            System.arraycopy(challenge, 0, asym1, 0, asym1.length);
            byte[] sym1 = new byte[challenge.length - 256];
            System.arraycopy(challenge, 256, sym1, 0, sym1.length);
            byte[] decrypted1;
            if (os == 1) {//linux
                //decrypted1 = TpmModule.activateIdentity(ownerAuthRaw, keyAuthRaw, asym1, sym1, HisIdentityIndex);
                HashMap<String, byte[]> results = TpmModule.activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asym1, sym1, config.getAikIndex());

                decrypted1 = results.get("aikcert");
            } else //decrypted1 = TpmModuleJava.ActivateIdentity(asym1, sym1, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw); //Comments  temporarily due to TSSCoreService.jar compiling issue 
            {
                decrypted1 = TpmModule.activateIdentity(config.getTpmOwnerSecret(), config.getAikSecret(), asym1, sym1, config.getAikIndex());
            }
            
            // send the answer and receive the AIK certificate
            TpmIdentityRequest encryptedChallenge = new TpmIdentityRequest(decrypted1, (RSAPublicKey) privacy.getPublicKey(), false);
            System.err.println("Create Identity... Calling into HisPriv second time, size of msg = " + encryptedChallenge.toByteArray().length);
            
            IdentityChallengeResponse identityChallengeResponse = new IdentityChallengeResponse();
            identityChallengeResponse.setChallengeResponse(encryptedChallenge.toByteArray());
            IdentityBlob identityBlob = client.identityChallengeResponse(identityChallengeResponse);
            byte[] encrypted2 = identityBlob.getIdentityBlob();

            // decode and store the aik certificate
            byte[] asym2 = new byte[256];
            System.arraycopy(encrypted2, 0, asym2, 0, asym2.length);
            byte[] sym2 = new byte[encrypted2.length - 256];
            System.arraycopy(encrypted2, 256, sym2, 0, sym2.length);
            byte[] decrypted2;
            byte[] aikblob;

            String aikcertfilepath = config.getAikCertificateFile().getAbsolutePath();
            String aikblobfilepath = config.getAikBlobFile().getAbsolutePath(); 
            if (os == 1) {//linux
                HashMap<String, byte[]> results = TpmModule.activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asym2, sym2, config.getAikIndex());
                System.out.println(results);

                decrypted2 = results.get("aikcert");
                aikblob = results.get("aikblob");

                writecert(aikcertfilepath, decrypted2);
                writeblob(aikblobfilepath, aikblob);
                Runtime.getRuntime().exec("chmod 600 " + aikblobfilepath);

            } else {
                //decrypted1 = TpmModuleJava.ActivateIdentity(asym1, sym1, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw); 
                //decrypted2 = TpmModuleJava.ActivateIdentity(asym2, sym2, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw);//Comments  temporarily due to TSSCoreService.jar compiling issue 
                decrypted2 = TpmModule.activateIdentity(config.getTpmOwnerSecret(), config.getAikSecret(), asym2, sym2, config.getAikIndex());
                writecert(aikcertfilepath, decrypted2);
            }
            
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // issue #878
    private static void writecert(String absoluteFilePath, byte[] certificateBytes) throws FileNotFoundException, java.security.cert.CertificateException, IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        X509Certificate certificate = X509Util.decodeDerCertificate(certificateBytes); // throws CertificateException
        String certificatePem = X509Util.encodePemCertificate(certificate);
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(certificatePem, out); // throws IOException
        }
    }

    // issue #878
    // given a File, ensures that its parent directory exists, creating it if necessary, and throwing PrivacyCAException 
    // if there is a failure
    private static void mkdir(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                log.warn("Failed to create client installation path!");
                throw new IOException("Failed to create client installation path!");
            }
        }
    }

    // issue #878
    private static void writeblob(String absoluteFilePath, byte[] encryptedBytes) throws IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(encryptedBytes, out); // throws IOException
        }
    }    
}
