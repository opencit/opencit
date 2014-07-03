/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.host;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;
import java.security.Principal;

/**
 *
 * @author jbuhacoff
 */
public class LoginHostPrincipal implements Serializable, Principal {
    private static final long serialVersionUID = 854727147L;
    private String host;

    protected LoginHostPrincipal() { }
    
    public LoginHostPrincipal(String host) {
        this.host = host;
    }

    @Override
    public String getName() {
        return host;
    }

    public String getHost() {
        return host;
    }
    
}
