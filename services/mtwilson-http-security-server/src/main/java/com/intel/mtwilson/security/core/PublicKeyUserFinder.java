/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

/**
 * @since 0.5.2
 * @author jbuhacoff
 */
public interface PublicKeyUserFinder {
    PublicKeyUserInfo getUserForIdentity(byte[] fingerprint);
}
