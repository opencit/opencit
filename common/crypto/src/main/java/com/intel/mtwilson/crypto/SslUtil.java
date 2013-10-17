/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.crypto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 * @since 0.5.4
 */
public class SslUtil {
    private static final Logger log = LoggerFactory.getLogger(SslUtil.class);
    
    /**
     * Creates an RSA Keypair with the default key size and expiration date.
     * @param distinguishedName
     * @return 
     */
    public static RsaCredentialX509 createSelfSignedTlsCredential(String distinguishedName, String hostnameOrIpAddress) throws GeneralSecurityException {
        KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
        X509Builder x509 = X509Builder.factory()
                .subjectName(distinguishedName) // X500Name.asX500Name(ctx.tlsCertificate.getSubjectX500Principal()))
                .subjectPublicKey(keyPair.getPublic())
                .expires(3650, TimeUnit.DAYS)
                .issuerName(distinguishedName)
                .issuerPrivateKey(keyPair.getPrivate())
                .keyUsageKeyEncipherment()
                .keyUsageDataEncipherment()
                .alternativeName(hostnameOrIpAddress);
        X509Certificate newTlsCert = x509.build();
        return new RsaCredentialX509(keyPair.getPrivate(), newTlsCert);
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
    
    /**
     * Used by registerUserWithKeystore to automatically add a server's ssl certificates to the keystore.
     * It's important for the user to later review the keystore and validate those certificate fingerprints!!
     * @param keystore
     * @param baseURL
     * @throws Exception
     */
    public static void addSslCertificatesToKeystore(SimpleKeystore keystore, URL server) throws CryptographyException, IOException {
        try {
            X509Certificate[] certs = getServerCertificates(server);
            String aliasBasename = server.getHost();
            if (certs != null) {
                int certificateNumber = 0;
                for (X509Certificate cert : certs) {
                    certificateNumber++;
                    String alias = String.format("%s-%d", aliasBasename, certificateNumber);
                    keystore.addTrustedSslCertificate(cert, alias);
                    log.debug("Added SSL certificate with alias {}, subject {}, fingerprint {}, from server {}", new String[]{alias, cert.getSubjectX500Principal().getName(), DigestUtils.shaHex(cert.getEncoded()), aliasBasename});
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
    
    public static X509TrustManager createX509TrustManagerWithCertificates(X509Certificate[] certificates) throws KeyManagementException {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(createTrustedSslKeystore(certificates));
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

    public static X509Certificate[] getServerCertificates(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        if (!"https".equals(url.getProtocol())) {
            throw new IllegalArgumentException("URL scheme must be https");
        }
        int port = url.getPort();
        if (port == -1) {
            port = 443;
        }
        X509HostnameVerifier hostnameVerifier = new NopX509HostnameVerifierApache();
        CertificateStoringX509TrustManager trustManager = new CertificateStoringX509TrustManager();
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new X509TrustManager[]{trustManager}, null);
        SSLSocketFactory sf = new SSLSocketFactory(sslcontext, hostnameVerifier);
        Scheme https = new Scheme("https", port, sf);
        SchemeRegistry sr = new SchemeRegistry();
        sr.register(https);
        BasicClientConnectionManager connectionManager = new BasicClientConnectionManager(sr);
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        HttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);
        log.debug("Saving certificates from server URL: {}", url.toExternalForm());
        try {
            HttpHead request = new HttpHead(url.toExternalForm());
            HttpResponse response = httpClient.execute(request);
            log.debug("Server status line: {} {} ({})", new String[]{response.getProtocolVersion().getProtocol(), response.getStatusLine().getReasonPhrase(), String.valueOf(response.getStatusLine().getStatusCode())});
            httpClient.getConnectionManager().shutdown();
        }
        catch(Exception e) {
            log.warn("Error while sending https request", e); // not fatal since we may still have the ssl certificate from the handshake, which is all we want here
        }
        return trustManager.getStoredCertificates();
    }
    
}
