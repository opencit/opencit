/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509.repository;

import java.security.PublicKey;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public interface PublicKeyRepository {
    List<PublicKey> getPublicKeys();
}
