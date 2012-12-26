/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 * @since 0.5.4
 */
public class SslUtil {
    private static final Logger log = LoggerFactory.getLogger(SslUtil.class);
    
    public static KeyStore createTrustedSslKeystore(SimpleKeystore keystore) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        String[] aliases = keystore.listTrustedCertificates(SimpleKeystore.SSL);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (String alias : aliases) {
            ks.setCertificateEntry(alias, keystore.getX509Certificate(alias));
        }
        return ks;
    }

    /**
     * Used by registerUserWithKeystore to automatically add a server's ssl certificates to the keystore.
     * It's important for the user to later review the keystore and validate those certificate fingerprints!!
     * @param keystore
     * @param baseURL
     * @throws Exception
     */
    public static void addSslCertificatesToKeystore(SimpleKeystore keystore, URL server) throws CryptographyException, IOException {
        try {
            X509Certificate[] certs = RsaUtil.getServerCertificates(server);
            String aliasBasename = server.getHost();
            if (certs != null) {
                int certificateNumber = 0;
                for (X509Certificate cert : certs) {
                    certificateNumber++;
                    String alias = String.format("%s-%d", aliasBasename, certificateNumber);
                    keystore.addTrustedSslCertificate(cert, alias);
                    log.info("Added SSL certificate with alias {}, subject {}, fingerprint {}, from server {}", new String[]{alias, cert.getSubjectX500Principal().getName(), DigestUtils.shaHex(cert.getEncoded()), aliasBasename});
                }
                keystore.save();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException("Cannot download SSL Certificate for " + server.toString(), e);
        } catch (KeyManagementException e) {
            throw new CryptographyException("Cannot download SSL Certificate for " + server.toString(), e);
        } catch (FileNotFoundException e) {
            throw new CryptographyException("Cannot save SSL Certificate to keystore for " + server.toString() + ": cannot find keystore file", e);
        } catch (KeyStoreException e) {
            throw new CryptographyException("Cannot save SSL Certificate to keystore for " + server.toString(), e);
        } catch (CertificateException e) {
            throw new CryptographyException("Cannot save SSL Certificate for " + server.toString(), e);
        }
    }

    public static X509TrustManager createX509TrustManagerWithKeystore(SimpleKeystore keystore) throws KeyManagementException {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(createTrustedSslKeystore(keystore));
            TrustManager[] tms = tmf.getTrustManagers();
            for (TrustManager tm : tms) {
                if (tm instanceof X509TrustManager) {
                    return (X509TrustManager) tm;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException("Cannot create X509TrustManager", e);
        } catch (IOException e) {
            throw new KeyManagementException("Cannot create X509TrustManager", e);
        } catch (CertificateException e) {
            throw new KeyManagementException("Cannot create X509TrustManager", e);
        } catch (UnrecoverableEntryException e) {
            throw new KeyManagementException("Cannot create X509TrustManager", e);
        } catch (KeyStoreException e) {
            throw new KeyManagementException("Cannot create X509TrustManager", e);
        }
        throw new IllegalArgumentException("TrustManagerFactory did not return an X509TrustManager instance");
    }
    
}
