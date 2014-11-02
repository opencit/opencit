/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.PublicKey;

/**
 *
 * @author jbuhacoff
 */
public interface MutablePublicKeyRepository extends PublicKeyRepository {
    void addPublicKey(PublicKey publicKey);
}
