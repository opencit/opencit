/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.crypto.key.password;

import javax.crypto.SecretKey;

/**
 *
 * @author jbuhacoff
 */
public interface SecretKeyGenerator {
    SecretKey generateSecretKey(String password, byte[] salt, PasswordProtection protection);
}
