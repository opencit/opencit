/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.vmware;

//import java.util.HashMap;
import com.intel.mtwilson.agent.vmware.VmwareClientFactory;
import com.intel.mtwilson.tls.TlsConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX see also apache commons pool KeyedPoolableObjectFactory - 
 * this ipmlementation was quick but it maybe completely replaceable with
 * apache commons pool.
 * 
 * @author jbuhacoff
 */
public class VMwareConnectionPool {
    private Logger log = LoggerFactory.getLogger(getClass());
//    public static final int DEFAULT_MAX_SIZE = 10;
    private ConcurrentHashMap<TlsConnection,VMwareClient> pool = new ConcurrentHashMap<TlsConnection,VMwareClient>();
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
        if( client != null ) { return client; } // already validated
        return createClientForConnection(tlsConnection);
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
        VMwareClient client = pool.get(tlsConnection);
        if( client == null ) { return null; }
//        lastAccess.put(connectionString, System.currentTimeMillis());
        if( factory.validateObject(tlsConnection, client)) {
            log.info("Reusing vCenter connection for "+client.getEndpoint());
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
            pool.remove(tlsConnection); // remove it from the pool, we'll recreate it later
            
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
            VMwareClient client = factory.makeObject(tlsConnection);
            if( factory.validateObject(tlsConnection, client) ) {
                pool.put(tlsConnection, client);
                // TODO: check pool size, if greater than maxSize then start removing connections (most idle first) until we get down to maxSize
                log.info("Opening new vCenter connection for "+client.getEndpoint());
                return client;
            }
            else {
                throw new Exception("Failed to validate new vmware connection");
            }
        }
        catch(Exception e) {
            try {
                URL url = new URL(tlsConnection.getConnectionString());
                throw new VMwareConnectionException("Cannot connect to vcenter: "+url.getHost(), e);
            }
            catch(MalformedURLException e2) {
                throw new VMwareConnectionException("Cannot connect to vcenter: invalid connection strong", e2);                
            }
        }
    }
    
    public void close() {
        Set<TlsConnection> tlsConnections = pool.keySet();
        for(TlsConnection tlsConnection : tlsConnections) {
            VMwareClient client = pool.get(tlsConnection);
            try {
                factory.destroyObject(tlsConnection, client);
            }
            catch(Exception e) {
                try {
                    URL url = new URL(tlsConnection.getConnectionString());
                    log.error("Failed to disconnect from vcenter: "+url.getHost(), e);
                }
                catch(MalformedURLException e2) {
                    log.error("Failed to disconnect from venter with invalid connection string", e);
                }
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
