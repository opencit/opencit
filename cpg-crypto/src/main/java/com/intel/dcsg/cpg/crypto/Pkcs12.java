/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import com.intel.dcsg.cpg.io.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for reading and writing keys in PKCS#12 format
 * @since 0.1
 * @author jbuhacoff
 */
public class Pkcs12 {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final KeyStore keystore;
    private final String keystorePassword;
    private final Resource keystoreResource;
    
    /**
     * 
     * @param resource
     * @param password
     * @throws IOException if there was an error reading the keystore from the resource
     * @throws KeyStoreException if the PKCS12 keystore type is not available, or if the integrity check algorithm is not available, or if any certificates in the keystore could not be loaded
     */
    public Pkcs12(Resource resource, String password) throws IOException, KeyStoreException {
        keystoreResource = resource;
        keystorePassword = password;
        keystore = KeyStore.getInstance("PKCS12"); // throws KeyStoreException if this keystore type is not available
        try {
            InputStream in = keystoreResource.getInputStream();
            try {
                if( in == null ) {
                    keystore.load(null, keystorePassword.toCharArray());     // before a keystore may be accessed, it MUST be loaded;  passing null creates a new keystore.  see http://docs.oracle.com/javase/6/docs/api/java/io/InputStream.html                        
                }
                else {
                    keystore.load(in, keystorePassword.toCharArray()); // IOException, NoSuchAlgorithmException, CertificateException
                }
            }
            catch(Exception e) {
                log.warn("Cannot load keystore: {}", e.toString());
                log.warn("Creating new keystore");
                keystore.load(null, keystorePassword.toCharArray());
            }
            finally {
                try {
                    if( in != null ) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    log.warn("Failed to close keystore after reading", e);
                }
            }
        }
        catch(Exception e) {
            throw new KeyStoreException(e);
        }
    }
    
    /**
     * 
     * @return the resource that was passed in to the constructor
     */
    public Resource getResource() { return keystoreResource; }

    /**
     * Saves the keystore to the resource passed in to the constructor.
     * 
     * @throws IOException if there was an error writing the keystore to the resource
     * @throws KeyStoreException if the keystore has not been initialized, or if the integrity check algorithm is not available, or if any certificates in the keystore could not be loaded
     */
    public void save() throws IOException, KeyStoreException {
        try {
            OutputStream out = keystoreResource.getOutputStream();
            keystore.store(out, keystorePassword.toCharArray()); //, 
            out.close();
        }
        catch(NoSuchAlgorithmException e) {// if the algorithm used to check the integrity of the keystore cannot be found
            throw new KeyStoreException(e); 
        }
        catch(CertificateException e) {// if any certificates in the keystore could not be loaded
            throw new KeyStoreException(e); 
        }
    }
    
    /**
     * 
     * @return
     * @throws KeyStoreException if the keystore has not been initialized (loaded).
     */
    public List<String> aliases() throws KeyStoreException {
        return Collections.list(keystore.aliases());
    }
    
    public RsaCredentialX509 getRsaCredentialX509(String keyAlias, String keyPassword) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, FileNotFoundException, CertificateEncodingException, CryptographyException {
        // load the key pair
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry(keyAlias, new KeyStore.PasswordProtection(keyPassword.toCharArray())); //NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException
        if( pkEntry != null ) {
            PrivateKey myPrivateKey = pkEntry.getPrivateKey();
            Certificate myCertificate = pkEntry.getCertificate();
            if( myCertificate instanceof X509Certificate ) { //if( "X.509".equals(myCertificate.getType()) ) {
                return new RsaCredentialX509(myPrivateKey, (X509Certificate)myCertificate); //CryptographyException
            }
            throw new IllegalArgumentException("Key has a certificate that is not X509: "+myCertificate.getType());
            //PublicKey myPublicKey = pkEntry.getCertificate().getPublicKey();
            //return new RsaCredential(myPrivateKey, myPublicKey);
        }
        // key pair not found
        throw new FileNotFoundException("Keystore does not contain the specified key");        
    }
    
    /**
     * Replaces an existing keypair with the same alias or adds a new keypair
     * if one did not already exist.
     * 
     * The chain is optional and if provided it must be the certificates that
     * signed the credential's public key, in order, with the Root CA being LAST.
     * 
     * @param key
     * @param chain
     * @param alias 
     * @param keyPassword
     */
    public void setRsaCredentialX509(RsaCredentialX509 key, X509Certificate[] chain, String alias, String keyPassword) throws KeyManagementException {
        try {
            List<String> aliases = Collections.list(keystore.aliases());
            if( aliases.contains(alias) ) {
                keystore.deleteEntry(alias);
            }
            X509Certificate[] chain1;
            if( chain != null ) {
                chain1 = new X509Certificate[chain.length+1];
                chain1[0] = key.getCertificate();
                System.arraycopy(chain, 0, chain1, 1, chain.length);
            }
            else {
                chain1 = new X509Certificate[] { key.getCertificate() };
            }
            keystore.setKeyEntry(alias, key.getPrivateKey(), keyPassword.toCharArray(), chain1);        
        }
        catch(KeyStoreException e) {
            throw new KeyManagementException("Cannot add credential", e);
        }
    }
    
}
