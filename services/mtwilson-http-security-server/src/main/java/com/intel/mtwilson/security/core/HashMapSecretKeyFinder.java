/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import java.util.HashMap;

/**
 * @since 0.5.1
 * @author jbuhacoff
 */
public class HashMapSecretKeyFinder implements SecretKeyFinder {

    public final HashMap<String,String> database = new HashMap<String,String>();
    
    public HashMapSecretKeyFinder() {
        database.put("guest", "password");
    }
    
    @Override
    public String getSecretKeyForUserId(String userId) {
        return database.get(userId);
    }
    
}
