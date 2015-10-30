/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

//import java.util.HashMap;
import com.intel.dcsg.cpg.crypto.digest.Digest;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbuhacoff
 */
public class VMwareConnectionPool {
    private Logger log = LoggerFactory.getLogger(getClass());
//    public static final int DEFAULT_MAX_SIZE = 10;
    private ConcurrentHashMap<String,VMwareClient> pool = new ConcurrentHashMap<String,VMwareClient>();
//    private ConcurrentHashMap<String,Long> lastAccess = new ConcurrentHashMap<String,Long>();
//    private int maxSize = DEFAULT_MAX_SIZE;
    private VmwareClientFactory factory = null;
    
    public VMwareConnectionPool(VmwareClientFactory factory) {
        this.factory = factory;
    }
    /*
    public VMwareConnectionPool(int maxSize) {
//        this.maxSize = maxSize;
    }
    */
    
    /**
     * If a client is already open for the given connection string, it will
     * be returned. Otherwise, a new client is created and added to the pool.
     * 
     * See also borrowObject() in KeyedObjectPool in apache commons pool
     * 
     * @param connectionString
     * @return
     * @throws VMwareConnectionException 
     */
    public VMwareClient getClientForConnection(TlsConnection tlsConnection) throws VMwareConnectionException {
        VMwareClient client = reuseClientForConnection(tlsConnection);
        if( client != null ) { // already validated
            log.debug("Found an already created vcenter connection. Reusing...");
            return client;
        }
        else {
            log.debug("Could not find vcenter connection. Creating a new vcenter connection...");
            return createClientForConnection(tlsConnection);
        }
    }
    
    /**
     * Assumes there is already a client open for the given connection string,
     * and returns it. If there is not already a client open, this method
     * returns null. 
     * 
     * You should only call this method if you are interested in the status
     * of a connection in the pool for reporting purposes - for normal usage
     * getClientForConnection(String) is must more convenient because it creates
     * the connection if it is missing and re-creates it if it has been disconnected.
     * 
     * @param connectionString
     * @return
     * @throws VMwareConnectionException 
     */
    public VMwareClient reuseClientForConnection(TlsConnection tlsConnection) throws VMwareConnectionException {
//        log.debug("VMwareConnectionPool searching for existing connection {}", tlsConnection.getConnectionString());
        VMwareClient client = pool.get(tlsConnection.getURL().toExternalForm());
        if( client == null ) { return null; }
//        log.debug("VMwareConnectionPool validating existing connection for {}", tlsConnection.getConnectionString());
//        lastAccess.put(connectionString, System.currentTimeMillis());
        if( factory.validateObject(tlsConnection, client)) {
            log.debug("Reusing vCenter connection for "+client.getEndpoint());
//            client.setTlsPolicy(tlsConnection.getTlsPolicy());
            return client;                
        }
        log.info("Found stale vCenter connection");
        try {
            factory.destroyObject(tlsConnection, client);
        }
        catch(Exception e) {
            log.error("Error while trying to disconnect from vcenter", e);
        }
        finally {
            pool.remove(tlsConnection.getURL().toExternalForm()); // remove it from the pool, we'll recreate it later
            
        }
        return null;
    }
    
    /**
     * Creates a new client for the given connection string and adds it to the
     * pool. If there was already an existing client for that connection string,
     * it is replaced with the new one.
     * 
     * For normal use you should call getClientForConnection(String) because it
     * will re-use existing connections and automatically create new ones as needed.
     * 
     * @param connectionString
     * @return
     * @throws VMwareConnectionException 
     */
    public VMwareClient createClientForConnection(TlsConnection tlsConnection) throws VMwareConnectionException {
        try {
//            log.debug("VMwareConnectionPool create client for connection {}", tlsConnection.getConnectionString());
            VMwareClient client = factory.makeObject(tlsConnection);
            if( factory.validateObject(tlsConnection, client) ) {
//                log.debug("VMwareConnectionPool caching new connection {}", tlsConnection.getConnectionString());
                pool.put(tlsConnection.getURL().toExternalForm(), client);
//                log.debug("Opening new vCenter connection for "+client.getEndpoint());
                return client;
            }
            else {
                throw new Exception("Failed to validate new vmware connection");
            }
        }
        catch(javax.xml.ws.WebServiceException e) {
            // is it because of an ssl failure?  we're looking for this:  com.sun.xml.internal.ws.client.ClientTransportException: HTTP transport error: javax.net.ssl.SSLHandshakeException: java.security.cert.CertificateException: Server certificate is not trusted
            if( e.getCause() != null && e.getCause() instanceof javax.net.ssl.SSLHandshakeException) {
                javax.net.ssl.SSLHandshakeException e2 = (javax.net.ssl.SSLHandshakeException)e.getCause();
                if( e2.getCause() != null && e2.getCause() instanceof com.intel.dcsg.cpg.tls.policy.UnknownCertificateException ) {
                    com.intel.dcsg.cpg.tls.policy.UnknownCertificateException e3 = (com.intel.dcsg.cpg.tls.policy.UnknownCertificateException)e2.getCause();
                    log.warn("Failed to connect to vcenter due to unknown certificate exception: {}", e3.toString());
                    X509Certificate[] chain = e3.getCertificateChain();
                    if( chain == null || chain.length == 0 ) {
                        log.error("Server certificate is missing");
                    }
                    else {
                        for(X509Certificate certificate : chain) {
                            try {
                                log.debug("Server certificate SHA-256 fingerprint: {} and subject: {}", Digest.sha256().digest(certificate.getEncoded()).toHex(), certificate.getSubjectX500Principal().getName());
                            }
                            catch(CertificateEncodingException e4) {
                                log.error("Cannot read server certificate: {}", e4.toString(), e4);
                                throw new VMwareConnectionException(e4);
                            }
                        }
                        /*
                        try {
                            log.debug("Trust policy: {}", tlsConnection.getTlsPolicy().getClass().getName());
                            // now show what is in the trusted keystore... to help understand why it didn't match
                            List<X509Certificate> trustedCerts = tlsConnection.getTlsPolicy().getCertificateRepository().getCertificates();
                            log.debug("There are {} trusted certs in the keystore", trustedCerts.size());
                            for(X509Certificate trustedCert : trustedCerts) {
                                log.debug("Trusted certificate fingerprint: {} and subject: {}", new Sha1Digest(X509Util.sha1fingerprint(trustedCert)), trustedCert.getSubjectX500Principal().getName());                                
                            }
                        }
                        catch(Exception e5) {
                            e5.printStackTrace(System.err);
                            log.error("Cannot enumerate truted certificates: "+e5.toString(), e5);
                        }
                        */
                        throw new VMwareConnectionException("VMwareConnectionPool not able to read host information: "+ e3.toString());
                    }
                }
                else {
                    throw new VMwareConnectionException("Failed to connect to vcenter due to SSL handshake exception", e2);
                }
            }
            else {
                throw new VMwareConnectionException("Failed to connect to vcenter due to exception: "+e.toString(), e);
            }
        }
        catch(Exception e) {
//            log.debug("VMwareConnectionPool failed to create client for connection {}", tlsConnection.getConnectionString()); // removed to prevent leaking secrets
            log.error("Failed to connect to vcenter: "+e.toString(),e);
            e.printStackTrace(System.err);
            throw new VMwareConnectionException("Cannot connect to vcenter: " + tlsConnection.getURL().getHost(), e);
        }
        throw new VMwareConnectionException("Failed to connect to vcenter: unknown error");
    }
    
    public void close() {
        Set<String> tlsConnectionUrls = pool.keySet();
        for(String tlsConnectionUrl : tlsConnectionUrls) {
            VMwareClient client = pool.get(tlsConnectionUrl);
            TlsConnection tlsConnection;
            try {
                tlsConnection = new TlsConnection(new URL(tlsConnectionUrl), new InsecureTlsPolicy()); // This TlsConnection is not being used.
                factory.destroyObject(tlsConnection, client);
            }
            catch(Exception e) {
                log.error("Failed to disconnect from vcenter.", e);
            }
        }
    }
    
    /*
    private void drainPool() {
        if( pool.size() > maxSize ) {
            List<String> mostIdleFirst = listIdleConnections();
            
        }
    }
    
    private List<String> 
    * */
}
