/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.rfc822;

import java.security.PrivateKey;

/**
 *
 * @author jbuhacoff
 */
public interface PrivateKeyFinder {
    PrivateKey find(String keyId);
}
