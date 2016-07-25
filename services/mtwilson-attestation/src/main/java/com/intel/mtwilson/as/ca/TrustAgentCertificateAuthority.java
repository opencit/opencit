/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.ca;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.as.controller.MwKeystoreJpaController;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwKeystore;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.x500.DN;
import com.intel.mtwilson.My;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.apache.commons.configuration.Configuration;
import com.intel.dcsg.cpg.x509.X509Util;
import java.security.cert.CertificateException;
/**
 * The Trust Agent Certificate Authority is an RSA key pair that signs SSL
 * certificates during Trust Agent installs.
 * 
 * The required components are:
 * 1. RSA key pair (self-signed or higher CA signed)
 * 2. Password for the RSA private key
 * 
 * The RSA key pair is stored in a Java keystore file in the Mt Wilson database.
 * The password is provided through the Mt Wilson configuration.
 * 
 * Call the setup() method to ensure that a keystore and CA certificate exist
 * in the database before trying to use the Trust Agent CA.
 * 
 * @author jbuhacoff
 */
public class TrustAgentCertificateAuthority {
    public static final String KEYSTORE_NAME = "Mt Wilson CA";
    public static final String KEYSTORE_PROVIDER = "JKS";
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustAgentCertificateAuthority.class);
    private Configuration config = ASConfig.getConfiguration();
    private MwKeystoreJpaController keystoreJpa;  //new MwKeystoreJpaController(getEntityManagerFactory());
    private MwKeystore mwKeystore = null;
    private ByteArrayResource mwKeystoreResource = null;
    private SimpleKeystore keystore = null;
    private X509Certificate cacert = null;
    private String keystorePassword = null;
    private String keyAlias = null;
    private String keyPassword = null;
    
    public TrustAgentCertificateAuthority() throws IOException {
        keystoreJpa = My.jpa().mwKeystore();
    }
    
    public TrustAgentCertificateAuthority(Configuration config) {
        this.config = config;
    }
    
    /**
     * Preconditions:
     * Configuration contains the following keys:
     * mtwilson.ca.keystore.password
     * mtwilson.ca.key.alias
     * mtwilson.ca.key.password
     * 
     * Behavior:
     * If the keystore exists in the database and contains a CA key, nothing happens.
     * If the keystore does not exist in the database, it is created.
     * If the keystore exists but does not have a CA key, it is created.
     * If the keystore exists but cannot be opened because the password is incorrect -- will throw a CryptographyException.
     * If the keystore exists but the CA key cannot be accessed because the password is incorrect -- will throw a CryptographyException
     * 
     * The general contract of setup() is that it returns if everything is ok, and
     * throws an Exception if there is any error.  See preconditions for 
     * required inputs.
     * 
     * @throws CryptographyException
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws NonexistentEntityException
     * @throws Exception 
     */
    public void setup() throws CryptographyException, GeneralSecurityException, IOException, NonexistentEntityException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ASDataException {
        setupConfiguration();
        setupKeystore();
        setupCACert();        
    }
    
    /**
     * @deprecated see code in setup-console 
     * 
     * For simplicity, the CSR format is simply a self-signed certificate with
     * the details that the requestor would like to use in the CA-signed certificate.
     * 
     * Currently the only self-signed details that are honored are the Common Name
     * and the IP Address Alternative Name.
     * The rest of the output details are determined by the nature of this method,
     * which is to sign Trust Agent SSL certificates.
     * 
     * The output certificate should be valid for SSL but not for other functions.
     * 
     * Preconditions:
     * Configuration contains the following keys:
     * mtwilson.ca.keystore.password
     * mtwilson.ca.key.alias
     * 
     * @param csr should be a self-signed X509Certificate with Subject Name and Alternative Name (IP Address)
     * @param authorizationPassword required in order to sign the certificate.  (mtwilson.ca.key.password)
     * @return 
     */
    public X509Certificate signSslCertificate(X509Certificate csr, String authorizationPassword) throws CryptographyException, FileNotFoundException {
        RsaCredentialX509 ca = getCA(authorizationPassword);
        String subjectName = csr.getSubjectX500Principal().getName(); // this is a string like CN=abc, O=xyz, C=US
        DN dn = new DN(subjectName);
        String subjectCommonName = dn.getCommonName() != null ? dn.getCommonName() : subjectName;
        String alternativeName = X509Util.ipAddressAlternativeName(csr);
        if( alternativeName != null ) {
            alternativeName = "ip:" + alternativeName;
        }
        int days = 3650;
        try {
            X509Certificate cert = RsaUtil.createX509CertificateWithIssuer(csr.getPublicKey(), subjectCommonName, alternativeName, days, ca.getPrivateKey(), ca.getCertificate());
            return cert;
        }
        catch(IOException e) {
            throw new CryptographyException("Cannot create X509 certificate:", e);
        }
    }
    
    private void setupConfiguration() {
        keystorePassword = config.getString("mtwilson.ca.keystore.password");
        keyAlias = config.getString("mtwilson.ca.key.alias");
        keyPassword = config.getString("mtwilson.ca.key.password");
        
        if( keystorePassword == null || keyAlias == null || keyPassword == null ) {
            throw new IllegalStateException("One or more required configuration settings missing: mtwilson.ca.keystore.password, mtwilson.ca.key.alias, mtwilson.ca.key.password");
        }
        
    }

    /**
     * Precondition: setupConfiguration()
     * 
     * @throws CryptographyException 
     */
    private void setupKeystore() throws CryptographyException {
        if( !isKeystoreCreated() ) {
            createKeystoreWithPassword();
        }
    }
    
    /**
     * Precondition: setupKeystore()
     * 
     * @throws CryptographyException
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws NonexistentEntityException
     * @throws Exception 
     */
    private void setupCACert() throws CryptographyException, GeneralSecurityException, IOException, NonexistentEntityException, KeyStoreException, 
            NoSuchAlgorithmException, CertificateException, ASDataException {
        if( !isCACertCreated() ) {
            createCA();
            saveKeystore();
        }        
    }
    
    /**
     * Precondition: setupConfiguration()
     * 
     * If this function returns true, then the keystore is non-null
     * 
     * @return true if the CA keystore is available
     */
    private boolean isKeystoreCreated() {
        if( mwKeystore != null && keystore != null ) {
            return true;
        }
        if( mwKeystore == null ) {
            mwKeystore = keystoreJpa.findMwKeystoreByName(KEYSTORE_NAME);
        }
        if( mwKeystore != null && mwKeystore.getKeystore() != null ) {
            try {
                openKeystoreWithPassword();
            }
            catch(CryptographyException e) {
                log.error("Cannot open keystore", e);
                keystore = null;
            }
        }
        return mwKeystore != null && keystore != null;
    }
    
    /**
     * Precondition: isKeystoreCreated() == true
     * 
     * If this function returns true, then the cacert is non-null
     * 
     * @return true if the CA certificate is available
     */
    private boolean isCACertCreated() {
        if( cacert != null ) {
            return true;
        }
        try {
            try {
                cacert = getCACert();
                if( cacert != null ) {
                    return true;
                }
            }
            catch(FileNotFoundException e) {
                return false;
            }
            return false;
        }
        catch(CryptographyException e) {
            return false;
        }
    }
    
    /**
     * Precondition: setupConfiguration()
     * 
     * @param password
     * @throws CryptographyException 
     */
    private void createKeystoreWithPassword() throws CryptographyException {
        try {
            mwKeystoreResource = new ByteArrayResource();
            keystore = new SimpleKeystore(mwKeystoreResource, keystorePassword);
        }
        catch(KeyManagementException e) {
            throw new CryptographyException("Cannot create new keystore", e);
        }
    }
    
    /**
     * Precondition: setupConfiguration()
     * 
     * @throws CryptographyException if cannot open the keystore
     */
    private void openKeystoreWithPassword() throws CryptographyException {        
        try {
            mwKeystoreResource = new ByteArrayResource(mwKeystore.getKeystore());
            keystore = new SimpleKeystore(mwKeystoreResource, keystorePassword);
        }
        catch(KeyManagementException e) {
            throw new CryptographyException("Cannot open existing keystore", e);
        }
    }
    
    private void saveKeystore() throws NonexistentEntityException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,NonexistentEntityException, ASDataException {
        if( keystore == null ) { throw new IllegalStateException("Keystore is null"); }
        keystore.save(); // makes the keystore available in mwKeystoreResource.toByteArray()
        if( mwKeystore == null ) {
            mwKeystore = new MwKeystore();
            mwKeystore.setName(KEYSTORE_NAME);
            mwKeystore.setComment(String.format("Automatically created on %s", new Date().toString()));
            mwKeystore.setProvider(KEYSTORE_PROVIDER);
            mwKeystore.setKeystore(mwKeystoreResource.toByteArray());
            keystoreJpa.create(mwKeystore);
        }
        else {
            mwKeystore.setKeystore(mwKeystoreResource.toByteArray());
            keystoreJpa.edit(mwKeystore);
        }
    }
    
    private RsaCredentialX509 getCA(String password) throws CryptographyException, FileNotFoundException {
        try {
            
            if (keystore == null)
                throw new KeyStoreException();
            
            RsaCredentialX509 credential = keystore.getRsaCredentialX509(keyAlias, password);
            return credential;
        }
        catch(NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | CertificateEncodingException e) {
            throw new CryptographyException("Cannot load CA Cert from keystore", e);
        }
    }
    
    private RsaCredentialX509 getCA() throws CryptographyException, FileNotFoundException {
        return getCA(keyPassword);
    }
    
    private X509Certificate getCACert() throws CryptographyException, FileNotFoundException {
        return getCA().getCertificate();
    }
    
    private void createCA() throws CryptographyException, GeneralSecurityException, IOException {
        try {
            KeyPair keypair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
            cacert = RsaUtil.generateX509Certificate("CN=Mt Wilson CA", keypair, 3650);
            keystore.addKeyPairX509(keypair.getPrivate(), cacert, keyAlias, keyPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException("Cannot create new CA keypair", e);
        }
    }
}
