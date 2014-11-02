/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.tls.policy.impl.AnyProtocolSelector;
import com.intel.dcsg.cpg.tls.policy.impl.CertificateStoringX509TrustManager;
import com.intel.dcsg.cpg.tls.policy.impl.ConfigurableProtocolSelector;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
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

    public static X509Certificate[] getServerCertificates(URL url) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return getServerCertificates(url, new AnyProtocolSelector());
    }    
    
    /**
     * 
     * @param url to connect and download the server certificates
     * @param tlsProtocol like SSL, SSLv2, SSLv3, TLS, TLSv1.1, TLSv1.2; any value accepted by SSLContext.getInstance(...)
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException 
     */
    public static X509Certificate[] getServerCertificates(URL url, String tlsProtocol) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        return getServerCertificates(url, new ConfigurableProtocolSelector(tlsProtocol));
    }
    
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
    public static X509Certificate[] getServerCertificates(URL url, ProtocolSelector selector) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        if (!"https".equals(url.getProtocol())) {
            throw new IllegalArgumentException("URL scheme must be https");
        }
        CertificateStoringX509TrustManager trustManager = new CertificateStoringX509TrustManager();
        SSLContext sslcontext = SSLContext.getInstance(getSafeContextName(selector.preferred()));
        sslcontext.init(null, new X509TrustManager[]{trustManager}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());   
        HttpsURLConnection.setDefaultHostnameVerifier(new org.apache.http.conn.ssl.AllowAllHostnameVerifier());
        log.debug("Saving certificates from server URL: {}", url.toExternalForm());
        try(InputStream in = url.openStream()) {
            String response = IOUtils.toString(in);
            log.trace("Got response: {}", response);
        }
        catch(IOException e) {
            // for example: java.io.FileNotFoundException: https://127.0.0.1:8443  but the ssl cert gets saved anyway even if there is no content in the server response besides 404 Not Found in the status line
            log.trace("TlsUtil: error while opening stream for getServerCertificates",e);
            // ignore the exception 
        }
        return trustManager.getStoredCertificates();
    }
    
    /**
     * CVE-2014-3566: don't use SSL
     * @param contextName for example SSL, SSLv2, SSLv3, TLS, TLSv1.1, TLSv1.2
     * @return 
     */
    public static String getSafeContextName(String contextName) {
        if( contextName == null ) { return "TLS"; }
        if( contextName.startsWith("SSL") ) { return "TLS"; } // CVE-2014-3566 says don't use SSL;   
        return contextName;
    }

    public static String[] getSafeContextNames(String[] contextName) {
        HashSet<String> safe = new HashSet<>();
        if( contextName == null ) {
            safe.add("TLS");
            return safe.toArray(new String[1]);
        }
        for( int i=0; i<contextName.length; i++) {
            if( contextName[i].startsWith("SSL") ) { safe.add("TLS"); }
        }
        if( safe.isEmpty() ) {
            safe.add("TLS");
            return safe.toArray(new String[1]);
        }
        return safe.toArray(new String[0]);
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

    /**
     * Used to automatically add a server's ssl certificates to a keystore.
     * It's important for the user to later review the keystore and validate those certificate fingerprints!!
     * @param keystore
     * @param baseURL
     * @throws Exception
     */
    public static void addSslCertificatesToKeystore(SimpleKeystore keystore, URL server, String tlsProtocol) throws CryptographyException, IOException {
        try {
            X509Certificate[] certs = getServerCertificates(server, tlsProtocol);
            String aliasBasename = server.getHost();
            if (certs != null) {
                int certificateNumber = 0;
                for (X509Certificate cert : certs) {
                    certificateNumber++;
                    String alias = String.format("%s-%d", aliasBasename, certificateNumber);
                    keystore.addTrustedSslCertificate(cert, alias);
                    log.debug("Added SSL certificate with alias {}, subject {}, fingerprint {}, from server {}", alias, cert.getSubjectX500Principal().getName(), Sha1Digest.digestOf(cert.getEncoded()).toHexString(), aliasBasename);
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

    /**
     * Used to automatically add a server's ssl certificates to a keystore.
     * It's important for the user to later review the keystore and validate those certificate fingerprints!!
     * 
     * There is now another method with the TLS protocol
     * specified as a parameter; this method continues to use TLS as the default
     * protocol.
     * @param keystore
     * @param server
     * @throws CryptographyException
     * @throws IOException
     */
    public static void addSslCertificatesToKeystore(SimpleKeystore keystore, URL server) throws CryptographyException, IOException {
        addSslCertificatesToKeystore(keystore, server, "TLS");
    }
    
    /**
     * The SunJSSEProvider includes SSLv3, TLSv1, TLSv1.1, and TLSv1.2 Other
     * algorithms may be available from other providers.
     * http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider
     *
     * @return a matching ssl context instance
     * @throws NoSuchAlgorithmException
     */
    public static SSLContext findBestContext(TlsPolicy tlsPolicy) throws NoSuchAlgorithmException {
        SSLContext ctx = null;
        String[] protocolNames = new String[]{tlsPolicy.getProtocolSelector().preferred()}; // TODO:  when protocolSelector returns an ordered String[], just use that directly
        String[] safeProtocolNames = getSafeContextNames(protocolNames);
        for (int i = 0; i < safeProtocolNames.length && ctx == null; i++) {
            try {
                ctx = SSLContext.getInstance(safeProtocolNames[i]); // for example  SSL, SSLv2, SSLv3, TLS, TLSv1, TLSv1.1, TLSv1.2
                log.debug("SSLContext.getInstance class {} for protocol {}", ctx.getClass().getName(), safeProtocolNames[i]);
            } catch (NoSuchAlgorithmException e) {
                log.debug("No available algorithm for {}: {}", tlsPolicy.getProtocolSelector().preferred(), e.getMessage());
                // TODO:  when  ProtocolSelector is modified to return an ordered array of protocol names, we can try one at a time here,
                // and only throw NoSuchAlgorithmException if we didn't find any match at all.
            }
        }
        if (ctx != null) {
            return ctx;
        }
        log.error("No available algorithms satisfy TlsPolicy class {} with ProtocolSelector class {} preference {}", tlsPolicy.getClass().getName(), tlsPolicy.getProtocolSelector().getClass().getName(), tlsPolicy.getProtocolSelector().preferred());
        throw new NoSuchAlgorithmException("No available algorithms for: " + tlsPolicy.getProtocolSelector().preferred());
    }
    
    /**
     * Registers the TlsPolicy for the given TlsConnection for its URL
     * (host:port)
     * 
     * It's best to use TlsConnection's openConnection or connect methods
     * directly, but for cases where code from an external library uses URL
     * to open SSL connections,  call this method first to ensure 
     * that the default policies
     * are the cpg-tls-policy wrappers that will try to use the right policy for that
     * connection.
     * 
     * 
     * @param tlsConnection 
     */
    public static void setHttpsURLConnectionDefaults(TlsConnection tlsConnection) {
        String portName;
        int port = tlsConnection.getURL().getPort();
        if (port == -1) {
            port = tlsConnection.getURL().getDefaultPort();
            if (port == -1) {
                portName = tlsConnection.getURL().getProtocol(); // use the protocol as the symbolic port name
            }
            else {
                portName = String.valueOf(port);
            }
        }
        else {
            portName = String.valueOf(port);
        }
        String address = String.format("%s:%s", tlsConnection.getURL().getHost(), portName);
        log.debug("setHttpsURLConnectionDefaults registering policy for {}", address);
        TlsPolicyManager.getInstance().setTlsPolicy(address, tlsConnection.getTlsPolicy()); 
        // complete support for the "address:protocol" as shown above is not implemented in the socket factory and tls manager classes
        // so if the actual port is not known, we register using only the hostname
        if( port == -1 ) {
            log.debug("setHttpsURLConnectionDefaults registering policy for {}", tlsConnection.getURL().getHost());
            TlsPolicyManager.getInstance().setTlsPolicy(tlsConnection.getURL().getHost(), tlsConnection.getTlsPolicy());  
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(new TlsPolicyAwareSSLSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(TlsPolicyManager.getInstance());
        
    }
}
