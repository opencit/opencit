/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import com.intel.mtwilson.datatypes.Role;
import java.security.PublicKey;
import java.util.HashMap;

/**
 * @since 0.5.2
 * @author jbuhacoff
 */
public class HashMapPublicKeyFinder implements PublicKeyUserFinder {

    public final HashMap<byte[],PublicKey> database = new HashMap<byte[],PublicKey>();
    
    public HashMapPublicKeyFinder() {
//        database.put("guest", "password");
    }

    @Override
    public PublicKeyUserInfo getUserForIdentity(byte[] fingerprint) {
        PublicKeyUserInfo userInfo = new PublicKeyUserInfo();
        userInfo.fingerprint = fingerprint;
        userInfo.publicKey = database.get(fingerprint);
        userInfo.roles = new Role[] { };
        return userInfo;
    }
    
}
