/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.tls.policy;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class exists to coordinate the use of separate TLS Policy instances for separate connections, where the
 * connections are being made by client code that we don't control, using URL.openConnection() or URL.openStream(),
 * which uses the global default trust manager and hostname verifier.
 *
 * To use this class, register the TLS Policy for each server before you open a connection to it.
 *
 * @author jbuhacoff
 */
public class TlsPolicyManager implements HostnameVerifier {

    private static final TlsPolicyManager singleton = new TlsPolicyManager();

    public static TlsPolicyManager getInstance() {
        return singleton;
    }
    private static Logger log = LoggerFactory.getLogger(TlsPolicyManager.class);
    private ConcurrentHashMap<String, TlsPolicy> map = new ConcurrentHashMap<>(32); // default capacity 16 but we're starting with 32, default load factor 0.75

    /**
     * Address should be in one of these formats:
     * 
     * host:port
     * host:protocol   (useful when the user hasn't specified a port and no default port is known for the given protocol)
     * host:*
     * host     (same as host:*)
     * 
     * @param address
     * @param tlsPolicy 
     */
    public void setTlsPolicy(String address, TlsPolicy tlsPolicy) {
        log.debug("TlsPolicyManager: adding {} with policy: {}", address, tlsPolicy.getClass().toString());
        /*
        TlsPolicy previousValue = map.get(address);
        if( previousValue != null ) {
            // XXX TODO unfortuantely we don't have a good way right now to give any more details about the policy...
            // so the two policies might be of the same class yet their trusted certs etc. might be different.
            // maybe we need a toString() override in each TlsPolicy implementation that summarizes what's in the policy?
            // then instead of tlsPolicy.getClass().toString() we would call tlsPolicy.toString() 
            log.warn("TlsPolicyManager: policy for address {} replaced {} with {}", address, previousValue.getClass().toString(), tlsPolicy.getClass().toString());
        }
        */
         map.put(address, tlsPolicy);        
    }
    
//    @Override
    public X509TrustManager getTrustManager() {
        log.debug("TlsPolicyManager: providing workaround allow-all X509TrustManager");
        // using anonymous class equivalent to AllowAllX509TrustManager but
        // without the log.warn messages about insecure policy since in this
        // context the security check is done in the hostname verifier
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String authType) throws CertificateException { log.debug("checkClientTrusted"); }
            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String authType) throws CertificateException { log.debug("checkServerTrusted"); }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                log.debug("getAcceptedIssuers");
                return new X509Certificate[0];
            }
        };

    }

    /*
    @Override
    public CertificateRepository getCertificateRepository() {
        ArrayList<X509Certificate> list = new ArrayList<X509Certificate>();
        for (TlsPolicy tlsPolicy : map.values()) {
            list.addAll(tlsPolicy.getCertificateRepository().getCertificates());
        }
        return new ArrayCertificateRepository(list.toArray(new X509Certificate[list.size()]));
    }
    */
    
    public TlsPolicy getTlsPolicy(String address, int port) {
        TlsPolicy tlsPolicy;
        String addressPort = String.format("%s:%d", address, port);
        tlsPolicy = map.get(addressPort);
        if( tlsPolicy != null ) { 
            return tlsPolicy;
        }
        String addressAnyPort = String.format("%s:*", address);
        tlsPolicy = map.get(addressAnyPort);
        if( tlsPolicy != null ) { 
            return tlsPolicy;
        }
        return null;
    }
    public TlsPolicy getTlsPolicy(String address) {
        if( ValidationUtil.isValidWithRegex(address, RegexPatterns.IPADDR_FQDN+":"+RegexPatterns.PORT) ) {
            log.debug("getTlsPolicy internet address with port {}", address);
            TlsPolicy tlsPolicy = map.get(address); // already in host:port format
            if(tlsPolicy != null) {
                return tlsPolicy;
            }
        }
        else if( ValidationUtil.isValidWithRegex(address, RegexPatterns.IPADDR_FQDN+":(?:[a-zA-Z]+)") ) {
            log.debug("getTlsPolicy internet address {}:protocol", address);
            TlsPolicy tlsPolicy = map.get(address); // already in host:protocol format
            if(tlsPolicy != null) {
                return tlsPolicy;
            }
        }
        else if( ValidationUtil.isValidWithRegex(address, RegexPatterns.IPADDR_FQDN) ) {
            log.debug("getTlsPolicy internet address {}:*", address);
            String addressAnyPort = String.format("%s:*", address);
            TlsPolicy tlsPolicy = map.get(addressAnyPort); // only host was given so we look for any port
            if(tlsPolicy != null) {
                return tlsPolicy;
            }
        }
        else {
            log.debug("getTlsPolicy {}", address);
            TlsPolicy tlsPolicy = map.get(address); 
            if(tlsPolicy != null) {
                return tlsPolicy;
            }
        }
        return null;
    }

    public boolean verify(String address, SSLSession ssls) {
        log.debug("TlsPolicyManager: verify {}", address);
        // look up the TLS Policy for the host
        int port = ssls.getPeerPort();
        TlsPolicy tlsPolicy = map.get(String.format("%s:%d",address,port));
        if (tlsPolicy == null) {
            tlsPolicy = map.get(String.format("%s:*", address));
        }
        if( tlsPolicy == null ) {
            tlsPolicy = map.get(address);
        }
        if( tlsPolicy == null ) {
            throw new TlsPolicyException("No TLS policy for host", address, null, null);
        }
        log.debug("TlsPolicyManager: policy {} for host: {}", tlsPolicy.getClass().getName(), address);
        // get the list of X509 certificates from the remote server
        ArrayList<X509Certificate> x509list = new ArrayList<>();
        try {
            Certificate[] certificates = ssls.getPeerCertificates(); // throws SSLPeerUnverifiedException
            for (Certificate cert : certificates) {
                //                log.debug("TlsPolicyManager: verify: certificate {} is {}", cert.getType(), DigestUtils.sha(cert.getEncoded())); // throws CertificateEncodingException
                if (cert.getType() != null && cert.getType().equals("X.509")) {
                    //                    x509list.add( X509Util.decodeDerCertificate(cert.getEncoded()) );
                    x509list.add((X509Certificate) cert);
                }
            }
        } catch (SSLPeerUnverifiedException e) {
            throw new TlsPolicyException("Peer cannot be verified", address, tlsPolicy, null);
        }
        X509Certificate[] serverCertificates = x509list.toArray(new X509Certificate[x509list.size()]);
        // verify peer certificates (since we skipped it in the trust manager -- because there we don't have the context of which host we are connecting to in order to look up the appropriate tls policy)
        X509TrustManager trustManager = tlsPolicy.getTrustManager();
        try {
            trustManager.checkServerTrusted(serverCertificates, "RSA");
            log.debug("TlsPolicyManager: Server certificate is trusted: {}", address);
        } catch (CertificateException e) {
            log.error("TlsPolicyManager: Server certificate not trusted: {}: " + e.toString(), address, e);
//                return false; // looks like this: java.io.IOException: HTTPS hostname wrong:  should be <10.1.71.201>
            throw new TlsPolicyException("Server certificate is not trusted", address, tlsPolicy, serverCertificates); // looks like this: com.intel.mtwilson.tls.ServerCertificateNotTrustedException: TlsPolicyManager: Server certificate is not trusted
        }
        log.debug("TlsPolicyManager: using policy for {}", address);
        // if the peer certificate is ok, verify the hostname:
        boolean hostnameVerified = tlsPolicy.getHostnameVerifier().verify(address, ssls); // XXX TODO need to create a seaprate instance for each connection, using the         llllllo-ktls policy for that connection
        log.debug("TlsPolicyManager: Hostname verification {} for {}", hostnameVerified ? "passed" : "failed", address);
        if (hostnameVerified) {
            return true;
        }
//            return hostnameVerified; // looks like this: java.io.IOException: HTTPS hostname wrong:  should be <10.1.71.201>
        // but instead of returning false, we're going to throw our custom exception that also provides the tls policy in effect
        throw new TlsPolicyException("Hostname verification failed", address, tlsPolicy, serverCertificates); // looks like this: com.intel.mtwilson.tls.ServerCertificateNotTrustedException: TlsPolicyManager: Hostname verification failed: Should be <10.1.71.201>
    }

}
