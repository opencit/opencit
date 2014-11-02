/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Encapsulates what is needed to secure individual connections: URL and TlsPolicy
 * for each one. 
 * 
 * When using openConnection() this class always registers the given TlsPolicy with the
 * TlsPolicyManager to ensure that multiple concurrent connections to different
 * host:port combinations with different TlsPolicy for each one will do the
 * right thing. 
 * 
 * When using connect() the given TlsPolicy is used directly.
 * 
 * @author jbuhacoff
 */
public class TlsConnection {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsConnection.class);
    private final URL url;
    private final TlsPolicy tlsPolicy;
    private transient Integer hashCode = null;
    private SSLContext sslContext = null;
    private SSLSocketFactory sslSocketFactory = null;
    /**
     *
     * @param url
     * @param tlsPolicy the original TlsPolicy - must NOT be a TlsPolicyManager
     * instance
     */
    public TlsConnection(URL url, TlsPolicy tlsPolicy) {
        this.url = url;
        this.tlsPolicy = tlsPolicy;
        // register with the tls policy manager
        int port = port();
        if( port == -1 ) {
            TlsPolicyManager.getInstance().setTlsPolicy(url.getHost(), tlsPolicy);
        }
        else {
            TlsPolicyManager.getInstance().setTlsPolicy(String.format("%s:%d",url.getHost(),port), tlsPolicy);
        }
    }

    public URL getURL() {
        return url;
    }

    public TlsPolicy getTlsPolicy() {
        return tlsPolicy;
    }

    public SSLContext getSSLContext() {
        log.debug("getSSLContext");
        try {
            init();
            return sslContext;
        }
        catch(NoSuchAlgorithmException | KeyManagementException  e) {
            throw new IllegalStateException("Cannot initialize SSL Context", e);
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {
        log.debug("getSSLSocketFactory");
        return sslSocketFactory;
    }
    
    private void init() throws NoSuchAlgorithmException, KeyManagementException {
        if( sslContext == null || sslSocketFactory == null ) { 
            sslContext = TlsUtil.findBestContext(tlsPolicy);  // throws NoSuchAlgorithmException
            log.debug("init with SSLContext class {} hashcode {}", sslContext.getClass().getName(), sslContext.hashCode());
//            KeyManager[] kms = null;
            TrustManager[] tms = new TrustManager[] { tlsPolicy.getTrustManager() };
//            sslContext.init(kms, tms, new java.security.SecureRandom()); //kms always null: klocwork 88
            sslContext.init(null, tms, new java.security.SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        }        
    }
    
    /**
     * Applications that use URL openConnection should instead create a
     * TlsConnection object with the URL and the TlsPolicy and call
     * this openConnection() method which will initialize the HttpsURLConnection
     * with an SSLSocketFactory and HostnameVerifier specified by the TlsPolicy.
     * 
     * The connection itself is not actually opened; see javadoc for
     * URL.openConnection.
     * 
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException 
     */
    public HttpsURLConnection openConnection() throws IOException {
        log.debug("connect");
        try {
            init(); // throws NoSuchAlgorithmException, KeyManagementException
        }
        catch(NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException(e);
        }
        
        URLConnection urlConnection = url.openConnection(); // throws IOException
        if( urlConnection instanceof HttpsURLConnection ) {
            log.debug("Initializing HttpsURLConnection with TlsPolicy {}", tlsPolicy.getClass().getName());
            HttpsURLConnection httpsConnection = (HttpsURLConnection)urlConnection;
            log.debug("Setting SSLSocketFactory: {}", sslSocketFactory.getClass().getName());
            httpsConnection.setSSLSocketFactory(sslSocketFactory);
            log.debug("Setting HostnameVerifier: {}", tlsPolicy.getHostnameVerifier().getClass().getName());
            httpsConnection.setHostnameVerifier(tlsPolicy.getHostnameVerifier());
            return httpsConnection;
        }
        throw new IllegalArgumentException("Unsupported class "+urlConnection.getClass().getName()+" for protocol "+url.getProtocol());
    }
    
//    public void setStaticDefaults() throws IOException, NoSuchAlgorithmException, KeyManagementException {
//        init();
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); // TODO: not clear if it will be possible to create a socket factory that is tlspolicy-aware ; for now using the default Java socket factory        
//        HttpsURLConnection.setDefaultHostnameVerifier(TlsPolicyManager.getInstance().getHostnameVerifier()); // using TlsPolicyManager because this default verifier is shared among new connections        
//    }

    
    /**
     * Caller must close socket when done.
     *
     * For code that opens HttpsURLConnection directly, it's simpler to 
     * set the SSLSocketFactory and the HostnameVerifier on a 
     * per-instance basis.
     *
     * @param timeoutMilliseconds
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws SSLPeerUnverifiedException if destination hostname does not match
     * certificate (socket will be automatically closed)
     */
    public SSLSocket connect(int timeoutMilliseconds) throws IOException {
        log.debug("connect with timeout {}", timeoutMilliseconds);
        try {
            init(); // throws NoSuchAlgorithmException, KeyManagementException
        }
        catch(NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException(e);
        }
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(); // ignore "socket not closed on exit" warnings; clearly we intend our return value to be the open socket
        socket.connect(new InetSocketAddress(url.getHost(), port()), timeoutMilliseconds);
        if (!tlsPolicy.getHostnameVerifier().verify(url.getHost(), socket.getSession())) {
            socket.close();
            throw new SSLPeerUnverifiedException("Invalid certificate for address: " + url.getHost());
        }
        return socket;
    }

    /**
     * No timeout.
     *
     * Caller must close socket when done.
     *
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws SSLPeerUnverifiedException if destination hostname does not match
     * certificate (socket will be automatically closed)
     */
    public SSLSocket connect() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        return connect(0);
    }


    private int port() {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
            if (port == -1) {
                log.warn("No port defined in URL for {} to {}", url.getProtocol(), url.getHost());
            }
        }
        return port;
    }

    /**
     * The following elements are considered when calculating hashCode: 1. url
     * protocol 2. url host 3. url port 4. tls policy name (obtained from the
     * implementation class name) 5. contents of certificate repository
     *
     * @return
     */
    @Override
    public int hashCode() {
        if (hashCode != null) {
            return hashCode;
        }
        return new HashCodeBuilder(19, 47)
                .append(url.getProtocol())
                .append(url.getHost())
                .append(url.getPort())
                .append(tlsPolicy.getClass().getName())
//                .append(tlsPolicy.getCertificateRepository())
                .append(tlsPolicy.getProtocolSelector())
                .toHashCode();
    }

    /**
     * Two TlsConnection instances are equal if they have the same URLs the same
     * TlsPolicy, and the same contents in their CertificateRepository. Note:
     * the URLs are compared using protocol, host, and port; all other
     * parameters are not considered. the same
     *
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass() != this.getClass()) {
            return false;
        }
        TlsConnection rhs = (TlsConnection) other;
        return new EqualsBuilder()
                .append(url.getProtocol(), rhs.url.getProtocol())
                .append(url.getHost(), rhs.url.getHost())
                .append(url.getPort(), rhs.url.getPort())
                .append(tlsPolicy.getClass().getName(), rhs.tlsPolicy.getClass().getName())
//                .append(tlsPolicy.getCertificateRepository(), rhs.tlsPolicy.getCertificateRepository())
                .append(tlsPolicy.getProtocolSelector(), rhs.tlsPolicy.getProtocolSelector())
                .isEquals(); // XXX TODO see note in hashCode about comparing the tls policy
    }
}
