/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class HashSetMutablePublicKeyRepository implements MutablePublicKeyRepository {
    private HashSet<PublicKey> publicKeys = new HashSet<>();

    @Override
    public void addPublicKey(PublicKey publicKey) {
        publicKeys.add(publicKey);
    }

    @Override
    public List<PublicKey> getPublicKeys() {
        return Collections.unmodifiableList(new ArrayList<>(publicKeys));
    }
}
