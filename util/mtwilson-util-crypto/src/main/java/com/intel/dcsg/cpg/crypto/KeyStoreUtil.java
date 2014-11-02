/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @since 0.1.5
 * @author jbuhacoff
 */
public class KeyStoreUtil {
    public static KeyStore createWithPassword(String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(null, password.toCharArray());
        return keystore;
    }
    public static KeyStore loadWithPassword(InputStream in, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType()); // throws KeyStoreException
        keystore.load(in, password.toCharArray());
        return keystore;
    }
    
    // just a convenience function for extracting trusted certs from a simplekeystore into a java keystore
    public static KeyStore createTrustedSslKeystore(SimpleKeystore keystore) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        String[] aliases = keystore.listTrustedSslCertificates();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (String alias : aliases) {
            ks.setCertificateEntry(alias, keystore.getX509Certificate(alias));
        }
        return ks;
    }

    // just a convenience function for importing an array of certs into a java keystore
    public static KeyStore createTrustedSslKeystore(X509Certificate[] certificates) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (int i=0; i<certificates.length; i++) { 
            X509Certificate cert = certificates[i];
            ks.setCertificateEntry("cert"+i, cert);
        }
        return ks;
    }
    
}
