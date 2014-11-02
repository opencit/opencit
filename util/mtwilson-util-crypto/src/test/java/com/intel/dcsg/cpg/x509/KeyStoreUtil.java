/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import com.intel.dcsg.cpg.io.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 *
 * @author jbuhacoff
 */
public class KeyStoreUtil {

    public static KeyStore createEmptyKeyStore() throws KeyStoreException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        return keystore;
    }
    
    public static KeyStore loadKeystore(File file, String password) throws KeyStoreException, FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(new FileInputStream(file), password.toCharArray()); // throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException
        return keystore;
    }
    
    public static KeyStore loadKeystore(Resource resource, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(resource.getInputStream(), password.toCharArray()); // throws FileNotFoundException, IOException, NoSuchAlgorithmException, CertificateException
        return keystore;
    }
    
    public static void saveKeystore(KeyStore keystore, File file, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(new FileOutputStream(file), password.toCharArray());
    }

    public static void saveKeystore(KeyStore keystore, Resource resource, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        keystore.store(resource.getOutputStream(), password.toCharArray());
    }
    
    /*
    public KeyStore test() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, UnrecoverableEntryException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.aliases(); // Enumeration<String>   can convert easily with COllections.list( ks.aliases() );
        ks.containsAlias(null); // boolean
        ks.deleteEntry(null); // void
        ks.entryInstanceOf(null, null); // test if entry is of a certain kind
        ks.getCertificate(null); // return the cert  but it's Certificate not X509Certificate  ...  so value add would be t ohave a converstion
        ks.getCertificateAlias(null); // returns alias of first matching cert... could be used to answer isCertificateInKeystore ?? or containsCetificate 
        ks.getCertificateChain(null); // for given alias return the entire cetificate chain... something smiplekeystoer doesn't expose at all
        ks.getCreationDate(null); // not exposed by simpleksytore.,  gets creation date of alias
        ks.getEntry(null, null); // get entry alias with specified protection parameter (password, or some othe rkey )
        ks.getKey(null, null); // like getEntry but for  a key specifically...
        ks.getProvider(); // return provider of keystore
        ks.getType(); // return type of keystore
        ks.isCertificateEntry(null);  // true if alias is for certificate
        ks.isKeyEntry(null); // true if alias is for key
        ks.load(null); // with loadstoreparameter which is a marker so not exposed by us
        ks.load(null, null); // input stream, password
        ks.setCertificateEntry(null, null); // add a cert
        ks.setEntry(null, null, null); // alias, entry , protection parameter (whcih may be null)
        ks.setKeyEntry(null, null, null ); // alias, key, cert chain  ;   key must be EncryptedPrivateKeyInfo  PKCS#8  bytes
        ks.setKeyEntry(null, null, null, null); // alias, key, password, cert chain   
        ks.size(); // number of entries in keystore
        ks.store(null); // with loadstoerparameter wchih we don't expose
        ks.store(null, null); // outputstream, password
        return ks;
    }
    */
}
