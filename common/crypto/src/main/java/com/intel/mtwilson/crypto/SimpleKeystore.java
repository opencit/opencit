/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import com.intel.mtwilson.io.FileResource;
import com.intel.mtwilson.io.Resource;
import java.io.*;
import java.net.MalformedURLException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Currently uses Java default keystore type "JKS" but later we may implement
 * an AES-128 keystore encryption provider.
 * 
 * TODO: make the interface uniform for callers by catching all exceptions
 * and re-throwing as KeyManagementException (with original exception attached)
 * 
 * Sun’s default keystore encryption algorithm for the private keys is PBEWithMD5AndTripleDES
 * Reference:
 * http://docs.oracle.com/javase/6/docs/technotes/guides/security/SunProviders.html
 * http://docs.oracle.com/javase/1.4.2/docs/guide/security/CryptoSpec.html#AppA
 * http://docs.oracle.com/javase/1.4.2/docs/guide/security/jce/JCERefGuide.html#AppA
 * 
 * TODO: Support other keystore providers such as PBE with SHA1 or SHA256 and AES-128.
 * 
 * @author jbuhacoff
 */
public class SimpleKeystore {
    public static String CA = "CA";
    public static String SSL = "SSL";
    public static String SAML = "SAML";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final KeyStore keystore;
    private final String keystorePassword;
    private final Resource keystoreResource;
    
    /**
     * If you are creating a new keystore, you can pass a FileResource that wraps
     * a non-existent (but writable) File, or you can pass a ByteArrayResource
     * that wraps an empty (not null) byte array.
     * @param resource
     * @param password
     * @throws KeyManagementException
     */
    public SimpleKeystore(Resource resource, String password) throws KeyManagementException {
        keystoreResource = resource;
        keystorePassword = password;
        try {
            keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // KeyStoreException. XXX TODO we need to implement AES-128 keystore encryption provider
            InputStream in = null;
            try {
                in = resource.getInputStream();
                try {
                    keystore.load(in, keystorePassword.toCharArray()); // IOException, NoSuchAlgorithmException, CertificateException
                }
                catch(EOFException e) {
                    // means file was empty or non-existent... even if we "canRead"
                    log.warn("Failed to read keystore", e);
                    keystore.load(null, keystorePassword.toCharArray());            
                }
            }
            catch(FileNotFoundException e) {
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
        catch (KeyStoreException e) {
            throw new KeyManagementException("Cannot create a keystore of type "+KeyStore.getDefaultType(), e);
        }
        catch(IOException e) {
            throw new KeyManagementException("Cannot load keystore", e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new KeyManagementException("Cannot load keystore", e);                    
        }
        catch(CertificateException e) {
            throw new KeyManagementException("Cannot load keystore", e);                    
        }
    }

    /**
     * If there is already a keystore at the provided location, it is opened with the given password.
     * If there isn't, a new keystore is created and will be saved with the given password.
     * 
     * @param file location of the existing or new keystore
     * @param password to open the existing keystore or protect a new keystore
     */
    public SimpleKeystore(File file, String password) throws KeyManagementException {
        this(new FileResource(file), password);
    }

    public void save() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        OutputStream out = keystoreResource.getOutputStream();
        keystore.store(out, keystorePassword.toCharArray());
        out.close();
    }
    
    /**
     * Saves a copy of the keystore. You don't need to call this unless you 
     * want a copy in a different location than where you loaded/created
     * initially. You can still call save() after or before this method to
     * save to the original location.
     * 
     * @param output where to save the keystore
     * @param password to protect the keystore
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException 
     */
    public void save(File output, String password) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(new FileOutputStream(output), password.toCharArray()); // FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException
    }
    
    public String[] aliases() throws KeyStoreException {
        List<String> aliases = Collections.list(keystore.aliases());
        return aliases.toArray(new String[0]);
    }
    
    /**
     * 
     * @param keyAlias
     * @param keyPassword
     * @return
     * @throws FileNotFoundException if the keystore does not contain keyAlias
     * @throws KeyStoreException if the keystore has not been initialized before calling this method
     * @throws NoSuchAlgorithmException if the platform is missing the algorithm used to decrypt the key
     * @throws UnrecoverableEntryException if the keyPassword is incorrect
     * @throws CertificateEncodingException if there is an error in the X509 certificate associated with the key
     */
    public RsaCredentialX509 getRsaCredentialX509(String keyAlias, String keyPassword) throws FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException {
        // load the key pair
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry(keyAlias, new KeyStore.PasswordProtection(keyPassword.toCharArray())); //NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException
        if( pkEntry != null ) {
            PrivateKey myPrivateKey = pkEntry.getPrivateKey();
            Certificate myCertificate = pkEntry.getCertificate();
            if( myCertificate instanceof X509Certificate ) { //if( "X.509".equals(myCertificate.getType()) ) {
                return new RsaCredentialX509(myPrivateKey, (X509Certificate)myCertificate); //CertificateEncodingException, NoSuchAlgorithmException
            }
            throw new IllegalArgumentException("Key has a certificate that is not X509: "+myCertificate.getType());
            //PublicKey myPublicKey = pkEntry.getCertificate().getPublicKey();
            //return new RsaCredential(myPrivateKey, myPublicKey);
        }
        // key pair not found
        throw new FileNotFoundException("Keystore does not contain the specified key");
    }
    
    public X509Certificate getX509Certificate(String certAlias) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, CertificateEncodingException {
        // load the certificate
        KeyStore.TrustedCertificateEntry certEntry = (KeyStore.TrustedCertificateEntry)keystore.getEntry(certAlias, null);
        if( certEntry != null ) {
            Certificate myCertificate = certEntry.getTrustedCertificate();
            if( myCertificate instanceof X509Certificate ) { //if( "X.509".equals(myCertificate.getType()) ) {
                return (X509Certificate)myCertificate;
            }
            throw new IllegalArgumentException("Certificate is not X509: "+myCertificate.getType());
            //PublicKey myPublicKey = pkEntry.getCertificate().getPublicKey();
            //return new RsaCredential(myPrivateKey, myPublicKey);
        }
        throw new KeyStoreException("Cannot load certificate with alias: "+certAlias);
    }

    public X509Certificate getX509Certificate(String certAlias, String purpose) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, CertificateEncodingException {
        String trustedAlias = String.format("%s (%s)", certAlias, purpose);
        return getX509Certificate(trustedAlias);
    }
    
    public X509Certificate[] getTrustedCertificates(String purpose) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException {
        String[] aliases = listTrustedCertificates(purpose);
        ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
        for(String alias : aliases) {
            list.add(getX509Certificate(alias));
        }
        return list.toArray(new X509Certificate[0]);
    }

    /**
     * Returns an array of aliases representing certificates trusted for the
     * given purpose.
     * @return 
     */
    public String[] listTrustedCertificates(String purpose) throws KeyStoreException {
        List<String> aliases = Collections.list(keystore.aliases());
        String tag = String.format("(%s)", purpose.toLowerCase());
        Iterator<String> it = aliases.iterator();
        while(it.hasNext()) {
            String alias = it.next();
            if( !alias.toLowerCase().endsWith(tag) ) {
                it.remove();
            }
        }
        return aliases.toArray(new String[0]);
    }
    
    /**
     * Returns an array of aliases representing server certificates trusted for SSL
     * encryption.
     * @return 
     */
    public String[] listTrustedSslCertificates() throws KeyStoreException {
        return listTrustedCertificates(SSL);
    }

    /**
     * Returns an array of aliases representing server certificates trusted for SAML
     * assertions.
     * @return 
     */
    public String[] listTrustedSamlCertificates() throws KeyStoreException {
        return listTrustedCertificates(SAML);
    }

    /**
     * Returns an array of aliases representing trusted certificate authorities.
     * @return 
     */
    public String[] listTrustedCaCertificates() throws KeyStoreException {
        return listTrustedCertificates(CA);
    }
    
    /**
     * Saves a trusted SSL or SAML certificate into the keystore. In production
     * you need to prompt the user to verify the fingerprint of the certificate
     * ebfore you add it, in order to prevent man-in-the-middle attacks.
     * The trusted purpose (SSL, SAML, etc) is added to the certificate's alias.
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    public void addTrustedCertificate(X509Certificate cert, String alias, String purpose) throws KeyManagementException {
        try {
            List<String> aliases = Collections.list(keystore.aliases());
            String trustedAlias = String.format("%s (%s)", alias, purpose);
            if( aliases.contains(trustedAlias) ) {
                // is it the same certificate? if so, we can ignore this request
                X509Certificate existing = getX509Certificate(trustedAlias);
                if( existing.equals(cert) ) {
                    return; // certificate is already in keystore with same alias
                }
                // a different certificate is already in the keystore with the same alias. we replace it:
                keystore.deleteEntry(trustedAlias);
            }
            keystore.setCertificateEntry(trustedAlias, cert);
        }
        catch(NoSuchAlgorithmException e) {
            throw new KeyManagementException("Cannot add trusted certificate", e);
        }
        catch(KeyStoreException e) {
            throw new KeyManagementException("Cannot add trusted certificate", e);
        }
        catch(CertificateException e) {
            throw new KeyManagementException("Cannot add trusted certificate", e);
        }
        catch(UnrecoverableEntryException e) {
            throw new KeyManagementException("Cannot add trusted certificate", e);
        }
    }

    /**
     * @param privateKey
     * @param cert
     * @param alias 
     * @param keyPassword
     */
    public void addKeyPairX509(PrivateKey privateKey, X509Certificate cert, String alias, String keyPassword) throws KeyManagementException {
        try {
            keystore.setKeyEntry(alias, privateKey, keyPassword.toCharArray(), new X509Certificate[] { cert });        
        }
        catch(KeyStoreException e) {
            throw new KeyManagementException("Cannot add keypair", e);
        }
    }
    
    /**
     * Saves server's SSL certificates into keystore - assumes they are trusted.
     * In production you need to prompt the user to
     * verify the fingerprint of the certificate before you add it, to prevent
     * man-in-the-middle attacks.
     * Since these are SSL certificates they are added to the keystore with the
     * "SSL" trusted purpose tag in their alias, so when you want to load this
     * cert again you need to load it as "alias (ssl)"
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    public void addTrustedSslCertificate(X509Certificate cert, String alias) throws KeyManagementException {
        addTrustedCertificate(cert, alias, SSL);
    }

    /**
     * Saves server's SAML certificates into keystore - assumes they are trusted.
     * In production you need to prompt the user to
     * verify the fingerprint of the certificate before you add it, to prevent
     * man-in-the-middle attacks.
     * Since these are SAML certificates they are added to the keystore with the
     * "SAML" trusted purpose tag in their alias, so when you want to load this
     * cert again you need to load it as "alias (saml)"
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    public void addTrustedSamlCertificate(X509Certificate cert, String alias) throws KeyManagementException {
        addTrustedCertificate(cert, alias, SAML);
    }

    /**
     * Saves CA certificates into keystore - assumes they are trusted.
     * In production you need to prompt the user to
     * verify the fingerprint of the certificate before you add it, to prevent
     * man-in-the-middle attacks.
     * Since these are CA certificates they are added to the keystore with the
     * "CA" trusted purpose tag in their alias, so when you want to load this
     * cert again you need to load it as "alias (ca)"
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    public void addTrustedCaCertificate(X509Certificate cert, String alias) throws KeyManagementException {
        addTrustedCertificate(cert, alias, CA);
    }
    
    public void delete(String alias) throws KeyManagementException {
        try {
            keystore.deleteEntry(alias);
        }
        catch(KeyStoreException e) {
            throw new KeyManagementException("Cannot delete entry: "+alias, e);
        }
    }
}
