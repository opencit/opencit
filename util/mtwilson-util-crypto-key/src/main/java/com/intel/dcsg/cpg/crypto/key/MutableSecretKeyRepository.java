/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key;

import javax.crypto.SecretKey;

/**
 *
 * @author jbuhacoff
 */
public interface MutableSecretKeyRepository extends SecretKeyRepository {
    void add(EncryptionKey key);
}
