/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mountwilson.util.vmware;

//import java.util.HashMap;
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
    private ConcurrentHashMap<String,VMwareClient> pool = new ConcurrentHashMap<String,VMwareClient>();
//    private ConcurrentHashMap<String,Long> lastAccess = new ConcurrentHashMap<String,Long>();
//    private int maxSize = DEFAULT_MAX_SIZE;
    
    public VMwareConnectionPool() {
        
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
     * @param connectionString
     * @return
     * @throws VMwareConnectionException 
     */
    public VMwareClient getClientForConnection(String connectionString) throws VMwareConnectionException {
        if( pool.containsKey(connectionString) ) {
            VMwareClient client = reuseClientForConnection(connectionString);
            if( client.isConnected() ) {
                return client;
            }
        }
        return createClientForConnection(connectionString);
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
    public VMwareClient reuseClientForConnection(String connectionString) throws VMwareConnectionException {
        VMwareClient client = pool.get(connectionString);
//        lastAccess.put(connectionString, System.currentTimeMillis());
        if( client != null ) {
        	log.info("Reusing vCenter connection for "+client.getEndpoint());
        }
        return client;
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
    public VMwareClient createClientForConnection(String connectionString) throws VMwareConnectionException {
        VMwareClient client = new VMwareClient();
        try {
            client.connect(connectionString);
            pool.put(connectionString, client);
            // TODO: check pool size, if greater than maxSize then start removing connections (most idle first) until we get down to maxSize
            log.info("Opening new vCenter connection for "+client.getEndpoint());
            return client;
        }
        catch(Exception e) {
            throw new VMwareConnectionException("Cannot connect to "+client.getEndpoint(), e);
        }
    }
    
    public void close() {
        Set<String> connectionStrings = pool.keySet();
        for(String connectionString : connectionStrings) {
            VMwareClient client = pool.get(connectionString);
            try {
                client.disconnect();
            }
            catch(Exception e) {
                log.error("Failed to disconnect from "+client.getEndpoint(), e);
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
