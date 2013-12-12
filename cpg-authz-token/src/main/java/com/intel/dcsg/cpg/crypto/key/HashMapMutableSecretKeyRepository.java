/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author jbuhacoff
 */
public class HashMapMutableSecretKeyRepository implements MutableSecretKeyRepository {
    private HashMap<String,EncryptionKey> store;
    
    public HashMapMutableSecretKeyRepository(HashMap<String,EncryptionKey> map) {
        store = map;
    }
    
    @Override
    public EncryptionKey find(byte[] keyId) {
        return store.get(Base64.encodeBase64String(keyId));
    }

    @Override
    public void add(EncryptionKey key) {
        store.put(Base64.encodeBase64String(key.getKeyId()), key);
    }

    @Override
    public EncryptionKey findExisting(byte[] keyId) throws KeyNotFoundException {
        EncryptionKey key = store.get(Base64.encodeBase64String(keyId));
        if( key == null ) {
            throw new KeyNotFoundException(keyId);
        }
        return key;
    }
    
}
