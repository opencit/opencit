/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import java.util.HashMap;
import javax.crypto.SecretKey;

/**
 * XXX this class is a draft for use in prototyping; applications should store keys in protected key stores
 * @author jbuhacoff
 */
public class HashMapSecretKeyRepository implements SecretKeyFinder {
    private HashMap<String,SecretKey> map = new HashMap<String,SecretKey>();
    public void put(String keyId, SecretKey key) {
        map.put(keyId, key);
    }
    @Override
    public SecretKey find(String keyId) {
        return map.get(keyId);
    }
    
}
