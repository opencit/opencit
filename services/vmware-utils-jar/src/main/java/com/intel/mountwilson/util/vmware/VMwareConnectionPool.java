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
 *
 * @author jbuhacoff
 */
public class VMwareConnectionPool {
    private Logger log = LoggerFactory.getLogger(getClass());
    public static final int DEFAULT_MAX_SIZE = 10;
    private ConcurrentHashMap<String,VMwareClient> pool = new ConcurrentHashMap<String,VMwareClient>();
//    private ConcurrentHashMap<String,Long> lastAccess = new ConcurrentHashMap<String,Long>();
//    private int maxSize = DEFAULT_MAX_SIZE;
    
    public VMwareConnectionPool() {
        
    }
    
    public VMwareConnectionPool(int maxSize) {
//        this.maxSize = maxSize;
    }
    
    public VMwareClient getClientForConnection(String connectionString) throws VMwareConnectionException {
        if( pool.containsKey(connectionString) ) {
            return reuseClientForConnection(connectionString);
        }
        else {
            return createClientForConnection(connectionString);
        }
    }
    
    private VMwareClient reuseClientForConnection(String connectionString) throws VMwareConnectionException {
        VMwareClient client = pool.get(connectionString);
//        lastAccess.put(connectionString, System.currentTimeMillis());
        // TODO: check that client's connection is still valid; if not automatically reconnect
        if( client != null ) {
        	log.info("Reusing vCenter connection for "+client.getEndpoint());
        }
        return client;
    }
    
    private VMwareClient createClientForConnection(String connectionString) throws VMwareConnectionException {
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
