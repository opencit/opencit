/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * The TlsPolicy for the given address and port must already be registered
 * with the TlsPolicyManager before calling createSocket
 * 
 * @author jbuhacoff
 */
public class TlsPolicyAwareSSLSocketFactory extends SSLSocketFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TlsPolicyAwareSSLSocketFactory.class);
    
    @Override
    public String[] getDefaultCipherSuites() {
        log.debug("getDefaultCipherSuites: SSL, TLS");
        // we return the a generic list  because at this point we don't know which tls policy is being asked for
        // if you already know the tls policy then create a TlsConnection and use its methods to obtain a socket factory
        // already configured for a specific policy
        // TODO: find the platform default list and return it here
        return new String[]{"SSL", "TLS"};
    }

    @Override
    public String[] getSupportedCipherSuites() {
        log.debug("getSupportedCipherSuites: SSL, TLS");
        // we return the a generic list  because at this point we don't know which tls policy is being asked for
        // if you already know the tls policy then create a TlsConnection and use its methods to obtain a socket factory
        // already configured for a specific policy
        // TODO: find the platform default list and return it here
        return new String[]{"SSL", "TLS"};
    }

    protected SSLSocketFactory getSSLSocketFactory(String address, int port) {
        log.debug("getSSLSocketFactory for {} port {}", address, port);
        TlsPolicy tlsPolicy = TlsPolicyManager.getInstance().getTlsPolicy(address, port);
        if (tlsPolicy == null) {
            throw new IllegalArgumentException("TLS policy cannot be null.");
        }
        try {
            SSLContext sslContext = TlsUtil.findBestContext(tlsPolicy);
            if( sslContext == null ) { throw new IllegalStateException("Cannot find SSL context"); }
            sslContext.init(null, new javax.net.ssl.TrustManager[]{TlsPolicyManager.getInstance().getTrustManager()}, new java.security.SecureRandom()); // throws KeyManagementException
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return sslSocketFactory;
        }
        catch(NoSuchAlgorithmException | KeyManagementException e) {
            throw new TlsPolicyException(e, address, tlsPolicy);
        }
    }
    
    /**
     * 
     * @param socket
     * @param address
     * @param port
     * @param autoClose
     * @return
     * @throws IOException 
     */
    @Override
    public Socket createSocket(Socket socket, String address, int port, boolean autoClose) throws IOException {
        return getSSLSocketFactory(address, port).createSocket(socket, address, port, autoClose);
    }

    @Override
    public Socket createSocket(String address, int port) throws IOException, UnknownHostException {
        return getSSLSocketFactory(address, port).createSocket(address, port);
    }

    @Override
    public Socket createSocket(String address, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
        return getSSLSocketFactory(address, port).createSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port) throws IOException {
        return getSSLSocketFactory(address.getHostAddress(), port).createSocket(address, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return getSSLSocketFactory(address.getHostAddress(), port).createSocket(address, port, localAddress, localPort);
    }
}
