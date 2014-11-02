/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Lightweight implementation of MutableDigestRepository backed by HashSet of Digest
 * 
 * The getDigests method returns an immutable list in accordance with the
 * DigestRepository contract. The mutable in MutableDigestRepository
 * is implemented via the addDigest method.
 * 
 * @author jbuhacoff
 */
public class HashSetMutableDigestRepository implements MutableDigestRepository {
    private HashSet<Digest> digests = new HashSet<>();

    @Override
    public void addDigest(Digest digest) {
        digests.add(digest);
    }

    @Override
    public List<Digest> getDigests() {
        return Collections.unmodifiableList(new ArrayList<>(digests));
    }
}
