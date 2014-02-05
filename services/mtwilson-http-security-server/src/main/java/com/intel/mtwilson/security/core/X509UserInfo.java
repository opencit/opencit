/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.core;

import com.intel.mtwilson.datatypes.Role;
import java.security.cert.Certificate;

/**
 * @since 0.5.2
 * @author jbuhacoff
 */
public class X509UserInfo {
    public byte[] fingerprint;
    public Certificate certificate;
    public Role[] roles;
    public String loginName;
}
