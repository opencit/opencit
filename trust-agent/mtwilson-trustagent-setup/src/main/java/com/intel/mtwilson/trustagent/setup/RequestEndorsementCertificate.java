/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.trustagent.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.as.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.attestation.client.jaxrs.CaCertificates;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.tpm.endorsement.client.jaxrs.TpmEndorsements;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementCollection;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsementFilterCriteria;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.niarl.ProvisionTPM;
import com.intel.mtwilson.trustagent.niarl.Util;
import com.intel.mtwilson.trustagent.tpm.tasks.ReadEndorsementCertificate;
import com.intel.mtwilson.trustagent.tpmmodules.Tpm;
import gov.niarl.his.privacyca.IdentityOS;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
public class RequestEndorsementCertificate extends AbstractSetupTask {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestEndorsementCertificate.class);
    private TrustagentConfiguration config;
    private File keystoreFile;
    private SimpleKeystore keystore;
    private String tpmOwnerSecretHex;
    private String url;
    private String username;
    private String password;
    private X509Certificate ekCert;
    private CaCertificates caCertificatesClient;
    private TpmEndorsements tpmEndorsementsClient;
    private File endorsementAuthoritiesFile;
    private List<X509Certificate> endorsementAuthorities;
    private UUID hostHardwareId;

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

        tpmOwnerSecretHex = config.getTpmOwnerSecretHex(); // we check it here because ProvisionTPM calls getOwnerSecret() which relies on this
        if (tpmOwnerSecretHex == null) {
            configuration("TPM Owner Secret is not configured: %s", TrustagentConfiguration.TPM_OWNER_SECRET); // this constant is the name of the property, literally "tpm.owner.secret"
        }
        if (!Util.isOwner(config.getTpmOwnerSecret())) {
            configuration("Trust Agent is not the TPM owner");
        }
        
        keystoreFile = config.getTrustagentKeystoreFile();
        /*
        if (keystoreFile.exists()) {
            keystore = new SimpleKeystore(new FileResource(keystoreFile), config.getTrustagentKeystorePassword());
            try {
                X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
                log.debug("Endorsement CA {}", Sha1Digest.digestOf(endorsementCA.getEncoded()).toHexString());
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
                configuration("Endorsement CA certificate cannot be loaded");
            }
        } else {
            configuration("Keystore file is missing");
        }
        */
        
        endorsementAuthoritiesFile = config.getEndorsementAuthoritiesFile();
        if( endorsementAuthoritiesFile == null ) {
            configuration("Endorsement authorities file location is not set");
        }
        
        try {
            tpmEndorsementsClient = new TpmEndorsements(config.getMtWilsonClientProperties());
        }
        catch(Exception e) {
            log.error("Cannot configure TPM Endorsements API client", e);
            configuration(e, "Cannot configure TPM Endorsements API client");
        }
        
        try {
            caCertificatesClient = new CaCertificates(config.getMtWilsonClientProperties());
        }
        catch(Exception e) {
            configuration(e, "Cannot configure CA Certificates API client");
        }
        
        String hostHardwareIdHex = config.getHardwareUuid();
        if( hostHardwareIdHex == null || hostHardwareIdHex.isEmpty() || !UUID.isValid(hostHardwareIdHex) ) {
            configuration("Host hardware UUID [hardware.uuid] must be set");
        }
        else {
            hostHardwareId = UUID.valueOf(hostHardwareIdHex);
        }
        
    }

    @Override
    protected void validate() throws Exception {
        try {
            readEndorsementCertificate();
        }
        catch(Exception e) {
            validation(e, "Cannot read endorsement certificate");
        }
        
        /*
        X509Certificate endorsementCA = keystore.getX509Certificate("endorsement", SimpleKeystore.CA);
        try {
            ekCert.verify(endorsementCA.getPublicKey());
        } catch (SignatureException e) {
            validation("Known Endorsement CA did not sign TPM EC", e);
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            validation("Unable to verify TPM EC", e);
        }
        */
        
        if( !endorsementAuthoritiesFile.exists()) {
            validation("Endorsement authorities file is missing");
        }
        if( endorsementAuthorities == null || endorsementAuthorities.isEmpty() ) {
            validation("No endorsement authorities");
        }
        else {
            String errorMessage;
            log.debug("Found {} endorsement authorities in {}", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
            // if we find one certificate authority that can verify our current EC, then we don't need to request a new EC
            if(!isEkSignedByEndorsementAuthority()) {
                errorMessage = String.format("Unable to verify TPM EC with %d authorities from %s.", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
                
                // check if we have registered with MTW
                if (!isEkRegisteredWithMtWilson()) {
                    errorMessage += "EC is also not registered with Mt.Wilson";
                    validation(errorMessage);
                }

//                validation("Unable to verify TPM EC with %d authorities from %s", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
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
        
        // try to read the local EC from the TPM ; will set tpmEndorsementCertificate if successful, or leave it null if unsuccessful
        readEndorsementCertificate();
        
        // first check if we have an EC and if it can be validated against known manufacturer CA certs
        log.debug("RequestEndorsementCertificate checking if EC is issued by known manufacturer");
        downloadEndorsementAuthorities();
        if( isEkSignedByEndorsementAuthority() ) {
            log.debug("EC is already issued by endorsement authority; no need to request new EC");
            return;
        }
        
        // second check if we have an EC and if it's already registered with Mt Wilson
        log.debug("RequestEndorsementCertificate checking if EC is registered with Mt Wilson");
        if( ekCert != null && isEkRegisteredWithMtWilson() ) {
            log.debug("EK is already registered with Mt Wilson; no need to request an EC");
            return;
        }
        
        // now if we have an EC register it with Mt Wilson
        if( ekCert != null ) {
            log.debug("RequestEndorsementCertificate registering EC with Mt Wilson");
            registerEkWithMtWilson();
        }
        else {
            // otherwise if we don't have an EC try to get our EK endorsed by Mt Wilson and install the received EC in TPM NNRAM
            log.debug("RequestEndorsementCertificate endorsing EC with Mt Wilson");
            endorseTpmWithMtWilson();
        }
    }
    
    private void readEndorsementCertificate() throws Exception {
        byte[] ekCertBytes;
        
        /* add the case to read EC from TPM on Windows */
        if (IdentityOS.isWindows()) { 
            /* Call Windows API to get the TPM EK certificate and assign it to "ekCert" */
            try {
                ekCertBytes = Tpm.getModule().getCredential(config.getTpmOwnerSecret(), "EC");
                log.debug("EC base64: {}", Base64.encodeBase64String(ekCertBytes));
                ekCert = X509Util.decodeDerCertificate(ekCertBytes);
            } catch (TpmModuleException e) {
                ekCert = null;
                if (e.getErrorCode() != null) {
                    switch (e.getErrorCode()) {
                        case 1:
                            throw new IllegalArgumentException("Incorrect TPM owner password");
                        case 2:
                            //throw new IllegalArgumentException("Endorsement certificate needs to be requested");
                            return;
                        default:
                            throw new IllegalArgumentException(String.format("Error code %d while validating EC", e.getErrorCode()));
                    }
                }
                log.debug("Failed to get EC");
                // try with tpm tools 
                //   /root/tpm-tools-1.3.8-patched/src/tpm_mgmt/tpm_getpubek
                //    that gets the EK modulus  but we still need the EC:
                // tpm_nvinfo -i 0x1000f000
                // tpm_nvread -i 0x1000f000 -x -t -pOWNER_AUTH -s 834 -f mfr.crt.tpm
                // openssl x509 -in mfr.crt.tpm -inform der -text   (works for Nuvoton, will not work for some others if they wrapped EC in a TCG structure... then have to use dd to remove initial bytes, and sometimes do other corrections for invalid length fields)
                throw new RuntimeException("Failed to get EC");
            }
        }
        else {  /* Linux -- Also need to distinguish between TPM 1.2 and TPM 2.0 */
            try {   
                /*
                String tpmVersion = TrustagentConfiguration.getTpmVersion();
                if (tpmVersion.equals("2.0")) {
                    File ecCertificateFile = config.getEcCertificateFile();
                    if( !ecCertificateFile.exists() )
                        ekCert = null;
                    else
                        ekCert = X509Util.decodePemCertificate(FileUtils.readFileToString(ecCertificateFile));
                } 
                */
                //else {
                    //ekCertBytes = TpmModule.getCredential(config.getTpmOwnerSecret(), "EC"); //tpm1.2           
                    ekCertBytes = Tpm.getModule().getCredential(config.getTpmOwnerSecret(), "EC");
                    log.debug("EC base64: {}", Base64.encodeBase64String(ekCertBytes));
                    ekCert = X509Util.decodeDerCertificate(ekCertBytes);
                //}
            } catch (TpmModuleException e) {
                ekCert = null;
                if (e.getErrorCode() != null) {
                    switch (e.getErrorCode()) {
                        case 1:
                            throw new IllegalArgumentException("Incorrect TPM owner password");
                        case 2:
                            //throw new IllegalArgumentException("Endorsement certificate needs to be requested");
                            return;
                        default:
                            throw new IllegalArgumentException(String.format("Error code %d while validating EC", e.getErrorCode()));
                    }
                }
                log.debug("Failed to get EC");
                // try with tpm tools 
                //   /root/tpm-tools-1.3.8-patched/src/tpm_mgmt/tpm_getpubek
                //    that gets the EK modulus  but we still need the EC:
                // tpm_nvinfo -i 0x1000f000
                // tpm_nvread -i 0x1000f000 -x -t -pOWNER_AUTH -s 834 -f mfr.crt.tpm
                // openssl x509 -in mfr.crt.tpm -inform der -text   (works for Nuvoton, will not work for some others if they wrapped EC in a TCG structure... then have to use dd to remove initial bytes, and sometimes do other corrections for invalid length fields)
                throw new RuntimeException("Failed to get EC");
            }
        }
    }
    
    private void downloadEndorsementAuthorities() throws Exception {
        // we create or replace our endorsement.pem file with what mtwilson provides
        // because it's mtwilson that will be evaluating it anyway in order to 
        // issue AIK certiicates later,  and because it's handy for the admin to
        // see locally the list of certs for troubleshooting -- so this could be
        // converted to getting the array of X509Certificate objects directly 
        // from the client without saving anything to disk. 
        
        CaCertificateFilterCriteria criteria = new CaCertificateFilterCriteria();
        criteria.domain = "ek"; // or "endorsement"
        String endorsementAuthoritiesPem = caCertificatesClient.searchCaCertificatesPem(criteria);
        try(OutputStream out = new FileOutputStream(endorsementAuthoritiesFile)) {
            IOUtils.write(endorsementAuthoritiesPem, out);
        }        
        try(InputStream in = new FileInputStream(endorsementAuthoritiesFile)) {
            String pem = IOUtils.toString(in);
            endorsementAuthorities = X509Util.decodePemCertificates(pem);
            log.debug("Found {} endorsement authorities in {}", endorsementAuthorities.size(), endorsementAuthoritiesFile.getAbsolutePath());
        }
    }
    
    private boolean isEkSignedByEndorsementAuthority() {
        for(X509Certificate ca : endorsementAuthorities) {
            try {
                log.debug("Trying to verify EC with {}", ca.getSubjectX500Principal().getName());
                if (ekCert != null) {
                    ekCert.verify(ca.getPublicKey());
                    log.debug("Verified EC with {}", ca.getSubjectX500Principal().getName());
                    return true;
                }
            } catch (SignatureException e) {
                log.debug("Endorsement CA '{}' did not sign TPM EC: {}", ca.getSubjectX500Principal().getName(), e.getMessage());
            } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | NullPointerException e) {
                log.debug("Unable to verify TPM EC '{}': {}", ca.getSubjectX500Principal().getName(), e.getMessage());
            }
        }
        return false;
    }
    
    private boolean isEkRegisteredWithMtWilson() throws Exception {
        TpmEndorsementFilterCriteria criteria = new TpmEndorsementFilterCriteria();
        criteria.hardwareUuidEqualTo = hostHardwareId.toString();
        TpmEndorsementCollection collection = tpmEndorsementsClient.searchTpmEndorsements(criteria);
        if( collection.getTpmEndorsements().isEmpty() ) {
            ObjectMapper mapper = new ObjectMapper();
            log.debug("Did not find EC with search criteria {}", mapper.writeValueAsString(criteria));
            return false;
        }
        log.debug("Found EC by hardware uuid");
        return true;
    }
    
    private void registerEkWithMtWilson() throws Exception {
        TpmEndorsement tpmEndorsement = new TpmEndorsement();
        tpmEndorsement.setId(new UUID());
        tpmEndorsement.setCertificate(ekCert.getEncoded());
        tpmEndorsement.setComment("registered by trust agent");
        tpmEndorsement.setHardwareUuid(hostHardwareId.toString());
        tpmEndorsement.setIssuer(ekCert.getIssuerDN().getName().replaceAll("\\x00", "")); // should be automatically set by server upon receiving the cert
        tpmEndorsement.setRevoked(false); // should default to false on server
        tpmEndorsementsClient.createTpmEndorsement(tpmEndorsement);
    }
    
    private void endorseTpmWithMtWilson() throws Exception {
        ProvisionTPM provisioner = new ProvisionTPM();
        provisioner.configure(getConfiguration());
        provisioner.run();
    }
}
