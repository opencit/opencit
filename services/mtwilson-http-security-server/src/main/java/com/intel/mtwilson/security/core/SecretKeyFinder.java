/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

/**
 *
 * @author jbuhacoff
 */
public interface SecretKeyFinder {
    String getSecretKeyForUserId(String userId);
}
