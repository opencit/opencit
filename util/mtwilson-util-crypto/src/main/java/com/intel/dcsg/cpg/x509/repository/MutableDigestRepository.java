/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.crypto.digest.Digest;

/**
 *
 * @author jbuhacoff
 */
public interface MutableDigestRepository extends DigestRepository {
    void addDigest(Digest digest);
}
