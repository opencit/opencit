/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

/**
 *
 * @author jbuhacoff
 */
public interface SecretKeyRepository {
    
    /**
     * 
     * @param keyId
     * @return the specified key or null if the key was not found
     */
    EncryptionKey find(byte[] keyId);
    
    /**
     * 
     * @param keyId
     * @return the specified key
     * @throws KeyNotFoundException if the specified key was not found
     */
    EncryptionKey findExisting(byte[] keyId) throws KeyNotFoundException;
}
