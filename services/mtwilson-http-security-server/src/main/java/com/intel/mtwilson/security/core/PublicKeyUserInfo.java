/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import com.intel.mtwilson.datatypes.Role;
import java.security.PublicKey;

/**
 * @since 0.5.2
 * @author jbuhacoff
 */
public class PublicKeyUserInfo {
    public byte[] fingerprint;
    public PublicKey publicKey;
    public Role[] roles;
}
