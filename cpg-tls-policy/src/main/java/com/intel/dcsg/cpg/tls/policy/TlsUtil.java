/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.tls.policy.impl.AllowAllHostnameVerifier;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateStoringX509TrustManager;
import com.intel.dcsg.cpg.x509.X509Builder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 * @since 0.1
 */
public class TlsUtil {
    private static final Logger log = LoggerFactory.getLogger(TlsUtil.class);
    
    /*  DEPRECATED, USE TLS-POLICY
    // just a convenience function for extracting trusted certs from a simplekeystore into a java keystore
    public static KeyStore createTrustedSslKeystore(SimpleKeystore keystore) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException {
        String[] aliases = keystore.listTrustedSslCertificates();
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (String alias : aliases) {
            ks.setCertificateEntry(alias, keystore.getX509Certificate(alias));
        }
        return ks;
    }*/

    /*   DEPRECATED, USE TLS-POLICY
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
    */
    
    /**
     * 
     * DEPRECATED,  USE TLS-POLICY or KeystoreUtil
     * 
     * Used by registerUserWithKeystore to automatically add a server's ssl certificates to the keystore.
     * It's important for the user to later review the keystore and validate those certificate fingerprints!!
     * @param keystore
     * @param baseURL
     * @throws Exception
     */
    /*
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
    }*/

    /**    deprecated, use TLS-POLICY
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
    }*/
    
    /**    deprecated,  use TLS-POLICY
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
    */

    /**
     * 
     * NOTE: this function is NOT thread-safe in conjunction with other https requests to hosts using
     * TlsPolicy. 
     * @param url
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    public static X509Certificate[] getServerCertificates(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        if (!"https".equals(url.getProtocol())) {
            throw new IllegalArgumentException("URL scheme must be https");
        }
        CertificateStoringX509TrustManager trustManager = new CertificateStoringX509TrustManager();
        SSLContext sslcontext = SSLContext.getInstance("TLS"); // or "SSL"
        sslcontext.init(null, new X509TrustManager[]{trustManager}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());   
        HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
        log.debug("Saving certificates from server URL: {}", url.toExternalForm());
        try {
        InputStream in = url.openStream();
//        IOUtils.toString(in);
        in.close();
        }
        catch(IOException e) {
            log.debug("TlsUtil: error while opening stream for getServerCertificates: {}",e.toString());
            // ignore the exception 
        }
        return trustManager.getStoredCertificates();
    }
    


    /*
    public static X509Certificate[] getServerCertificatesWithApacheHttpClient(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
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
        HttpHead request = new HttpHead(url.toExternalForm());
        HttpResponse response = httpClient.execute(request);
        log.debug("Server status line: {} {} ({})", new String[]{response.getProtocolVersion().getProtocol(), response.getStatusLine().getReasonPhrase(), String.valueOf(response.getStatusLine().getStatusCode())});
        httpClient.getConnectionManager().shutdown();
        return trustManager.getStoredCertificates();
    }
    */
    
}
