/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.crypto.keystore;

/**
 *
 * @author jbuhacoff
 */
public interface KeyProtectionDelegate {
    char[] getPassword(String keyId);
}
