/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.intel;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * The IntelClientFactory creates TrustAgentSecureClient instances. The 
 * TrustAgentSecureClient does not have a connect() or disconnect() method.
 * It creates a new connection for every call. This may be changed in a
 * future release.
 * 
 * See also KeyedPoolableObjectFactory in Apache Commons Pool
 * 
 * @author jbuhacoff
 */
public class IntelClientFactory extends BaseKeyedPoolableObjectFactory<TlsConnection,TrustAgentSecureClient> {
    
    @Override
    public TrustAgentSecureClient makeObject(TlsConnection tlsConnection)  {
        TrustAgentSecureClient client = new TrustAgentSecureClient(tlsConnection); // client has to parse the string to get ip address and port for trust agent. 
        return client;
    }
    
    /**
     * This gets called every time an object is being borrowed from the pool.
     * We don't need to do anything here, as vmware clients in the pool should
     * already be connected (that is the purpose of maintaining a pool of vmware
     * clients).
     * @param tlsConnection
     * @param client
     * @throws Exception 
     */
    @Override
    public void activateObject(TlsConnection tlsConnection, TrustAgentSecureClient client)  {
    }
    
    /**
     * If the pool is configured to validate objects before borrowing, then
     * this is called every time an object is being borrowed from the pool.
     * We validate the vmware client connection by making a quick
     * call to vcenter here. that way if it fails the pool can destroy the 
     * client and create a new one for the caller.
     * @param tlsConnection
     * @param client
     * @return 
     */
    @Override
    public boolean validateObject(TlsConnection tlsConnection, TrustAgentSecureClient client) {
//        return client.isConnected(); 
        return true; 
    }
    
    /**
     * This is called when the pool needs to get rid of a client - maybe because
     * it was idle too long and lost its connection, or because there are too
     * many idle clients, etc.
     * @param tlsConnection
     * @param client
     * @throws Exception 
     */
    @Override
    public void destroyObject(TlsConnection tlsConnection, TrustAgentSecureClient client)  {
//        client.disconnect();
    }
}
