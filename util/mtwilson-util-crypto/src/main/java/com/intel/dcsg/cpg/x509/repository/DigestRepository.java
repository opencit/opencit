/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import com.intel.dcsg.cpg.crypto.digest.Digest;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface DigestRepository {
    /**
     * 
     * @return an immutable list of digests (possibly empty); must not return null
     */
    List<Digest> getDigests();
}
