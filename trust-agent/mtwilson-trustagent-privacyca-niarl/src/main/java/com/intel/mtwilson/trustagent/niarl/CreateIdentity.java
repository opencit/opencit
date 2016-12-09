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
import com.intel.dcsg.cpg.configuration.Configurable;
import com.intel.mtwilson.privacyca.v2.model.IdentityBlob;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallenge;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeRequest;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeResponse;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import static com.intel.mtwilson.trustagent.niarl.Util.fixMakeCredentialBlobForWindows;
import com.intel.mtwilson.trustagent.tpmmodules.Tpm;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Request AIK Certificate from Mt Wilson Privacy CA
 *
 * Pre-requisites: Owner, SRK, and AIK secrets must already be generated and present in the configuration; EC must have already been provisioned by ProvisionTPM
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
            byte[] ekCert = Tpm.getModule().getCredential(config.getTpmOwnerSecret(), "EC");
            /*
            if (IdentityOS.isWindows()) { 
                // Call Windows API to get the TPM EK certificate and assign it to "ekCert"
                //#5815: Call to static method 'com.intel.mtwilson.trustagent.tpmmodules.Tpm.getTpm' via instance reference.
               //Tpm tpm = new Tpm();
                ekCert = Tpm.getModule().getCredential(config.getTpmOwnerSecret(), "EC");
            } else
                ekCert = Tpm.getModule().getCredential(config.getTpmOwnerSecret(), "EC");
             */
            TpmIdentityRequest encryptedEkCert = new TpmIdentityRequest(ekCert, (RSAPublicKey) privacy.getPublicKey(), false);

            // create the identity request
            //#5831: Test expression is always true.
            boolean shortcut = false;
            String HisIdentityLabel = "HIS_Identity_Key";

            TpmIdentity newId;
            if (IdentityOS.isWindows()) {
                /* Call Windows API to get the TPM EK certificate and assign it to "ekCert" */
                newId = Tpm.getModule().collateIdentityRequest(config.getTpmOwnerSecret(), config.getAikSecret(), HisIdentityLabel, new TpmPubKey((RSAPublicKey) privacy.getPublicKey(), 3, 1).toByteArray(), config.getAikIndex(), X509Util.decodeDerCertificate(ekCert), shortcut);

                // write the AikOpaque to file
                String aikopaquefilepath = config.getAikOpaqueFile().getAbsolutePath();
                writeblob(aikopaquefilepath, newId.getAikOpaque());

            } else {
                newId = Tpm.getModule().collateIdentityRequest(config.getTpmOwnerSecret(), config.getAikSecret(), HisIdentityLabel, new TpmPubKey((RSAPublicKey) privacy.getPublicKey(), 3, 1).toByteArray(), config.getAikIndex(), (X509Certificate) null, shortcut);
            }
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
            request.setAikName(newId.getAikNameBytes());
            request.setTpmVersion(Tpm.getTpmVersion());

            IdentityChallenge identityChallenge = client.identityChallengeRequest(request);
            byte[] challenge = identityChallenge.getIdentityChallenge();
            byte[] decrypted1;
            byte[] asym1 = new byte[identityChallenge.getAsymSize()];
            byte[] sym1 = new byte[identityChallenge.getSymSize()];
            System.arraycopy(challenge, 0, asym1, 0, asym1.length);
            System.arraycopy(challenge, asym1.length, sym1, 0, sym1.length);
            log.debug("challengelength: " + challenge.length);
            log.debug("asymlength in phase1: " + identityChallenge.getAsymSize());
            log.debug("symlength in phase 1: " + identityChallenge.getSymSize());
            int os = IdentityOS.osType();

            if (os == 1) { // linux
                HashMap<String, byte[]> results = Tpm.getModule().activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asym1, sym1, config.getAikIndex());

                decrypted1 = results.get("aikcert");
            } else {
                // Windows
                if (Tpm.getTpmVersion().equals("1.2")) {
                    byte[] asymEKblob = new byte[256];
                    // Struct is [asym1 || sym1 || {ekBlob}] where ekBlob is optional
                    int index = asym1.length + sym1.length;
                    System.arraycopy(challenge, index, asymEKblob, 0, challenge.length - (index));
                    //Tpm tpm = new Tpm();
                    HashMap<String, byte[]> results = Tpm.getModule().activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asymEKblob, sym1, config.getAikIndex());
                    decrypted1 = results.get("aikcert");
                }
                else {
                    /* 
                        Intel TSS2 output from TPM2_MakeCredential differs from Microsofts. 
                        Intel's has inconsistent usage of endianess and has padding for structures
                        We need to convert and fix it to be Microsoft compatible.
                    */
                    asym1 = fixMakeCredentialBlobForWindows(asym1);                        
                        
                    HashMap<String, byte[]> results = Tpm.getModule().activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asym1, sym1, config.getAikIndex());
                    decrypted1 = results.get("aikcert"); 
                }
            }

            // send the answer and receive the AIK certificate
            TpmIdentityRequest encryptedChallenge = new TpmIdentityRequest(decrypted1, (RSAPublicKey) privacy.getPublicKey(), false);
            System.err.println("Create Identity... Calling into HisPriv second time, size of msg = " + encryptedChallenge.toByteArray().length);

            IdentityChallengeResponse identityChallengeResponse = new IdentityChallengeResponse();
            identityChallengeResponse.setChallengeResponse(encryptedChallenge.toByteArray());
            identityChallengeResponse.setTpmVersion(Tpm.getTpmVersion());
            identityChallengeResponse.setAikName(newId.getAikNameBytes());
            IdentityBlob identityBlob = client.identityChallengeResponse(identityChallengeResponse);
            byte[] encrypted2 = identityBlob.getIdentityBlob();

            log.debug("encrypted2 length: " + encrypted2.length);
            log.debug("asymlength in phase2: " + identityBlob.getAsymSize());
            log.debug("symlength in phase2: " + identityBlob.getSymSize());
            
            // decode and store the aik certificate
            byte[] asym2 = new byte[identityBlob.getAsymSize()];
            byte[] sym2 = new byte[identityBlob.getSymSize()];

            System.arraycopy(encrypted2, 0, asym2, 0, asym2.length);

            // try to calculate the length of symblob due to the addition of EK_BLOB for Windows

            System.arraycopy(encrypted2, asym2.length, sym2, 0, sym2.length);
            byte[] decrypted2;
            byte[] aikblob;

            String aikcertfilepath = config.getAikCertificateFile().getAbsolutePath();
            String aikblobfilepath = config.getAikBlobFile().getAbsolutePath();
            if (os == 1) {//linux
                HashMap<String, byte[]> results = Tpm.getModule().activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asym2, sym2, config.getAikIndex());
                //System.out.println(results);

                decrypted2 = results.get("aikcert");
                aikblob = results.get("aikblob");

                writecert(aikcertfilepath, decrypted2);
                writeblob(aikblobfilepath, aikblob);
                Runtime.getRuntime().exec("chmod 600 " + aikblobfilepath);

            } else {
                // Windows
                if("2.0".equals(Tpm.getTpmVersion())) {
                    asym2 = fixMakeCredentialBlobForWindows(asym2);
                    
                    HashMap<String, byte[]> results = Tpm.getModule().activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asym2, sym2, config.getAikIndex());
                    //System.out.println(results);

                    decrypted2 = results.get("aikcert");
                    aikblob = results.get("aikblob");

                    writecert(aikcertfilepath, decrypted2);
                    writeblob(aikblobfilepath, aikblob);
                } else {

                    //decrypted1 = TpmModuleJava.ActivateIdentity(asym1, sym1, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw); 
                    //decrypted2 = TpmModuleJava.ActivateIdentity(asym2, sym2, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw);//Comments  temporarily due to TSSCoreService.jar compiling issue 
                    //decrypted2 = TpmModule.activateIdentity(config.getTpmOwnerSecret(), config.getAikSecret(), asym2, sym2, config.getAikIndex());
                    //writecert(aikcertfilepath, decrypted2);
                    int index = asym2.length + sym2.length;
                    byte[] asymEKblob = new byte[encrypted2.length - (index) ];
                    System.arraycopy(encrypted2, index, asymEKblob, 0, asymEKblob.length);

                    //Tpm tpm = new Tpm();
                    HashMap<String, byte[]> results = Tpm.getModule().activateIdentity2(config.getTpmOwnerSecret(), config.getAikSecret(), asymEKblob, sym2, config.getAikIndex());
                    System.out.println(results);

                    decrypted2 = results.get("aikcert");
                    aikblob = results.get("aikblob");

                    writecert(aikcertfilepath, decrypted2);
                    writeblob(aikblobfilepath, aikblob);
                    //Runtime.getRuntime().exec("chmod 600 " + aikblobfilepath);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // issue #878
    private static void writecert(String absoluteFilePath, byte[] certificateBytes) throws FileNotFoundException, java.security.cert.CertificateException, IOException {
        File file = new File(absoluteFilePath);
        mkdir(file); // ensure the parent directory exists
        X509Certificate certificate = X509Util.decodeDerCertificate(certificateBytes); // throws CertificateException
        String certificatePem = X509Util.encodePemCertificate(certificate);
        try (FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
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
        try (FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
            IOUtils.write(encryptedBytes, out); // throws IOException
        }
    }
    
}

