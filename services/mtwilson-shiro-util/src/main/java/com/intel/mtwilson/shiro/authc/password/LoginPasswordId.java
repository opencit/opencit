/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;
import java.security.Principal;

/**
 *
 * @author jbuhacoff
 */
public class LoginPasswordId implements Serializable, Principal {
    private static final long serialVersionUID = 854727147L;
    private String username;
    private UUID userId;
    private UUID loginPasswordId;

    protected LoginPasswordId() { }
    
    public LoginPasswordId(String username, UUID userId, UUID loginPasswordId) {
        this.username = username;
        this.userId = userId;
        this.loginPasswordId = loginPasswordId;
    }

    public UUID getUserId() {
        return userId;
    }

    
    public UUID getLoginPasswordId() {
        return loginPasswordId;
    }
    
    @Override
    public String getName() {
        return String.format("%s[%s] password [%s]", username, userId, loginPasswordId);
    }
}
