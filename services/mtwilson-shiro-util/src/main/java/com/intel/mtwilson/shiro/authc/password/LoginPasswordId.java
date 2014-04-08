/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;

/**
 *
 * @author jbuhacoff
 */
public class LoginPasswordId implements Serializable {
    private static final long serialVersionUID = 854727147L;
    private UUID userId;
    private UUID loginPasswordId;

    protected LoginPasswordId() { }
    
    public LoginPasswordId(UUID userId, UUID loginPasswordId) {
        this.userId = userId;
        this.loginPasswordId = loginPasswordId;
    }

    public UUID getUserId() {
        return userId;
    }

    
    public UUID getLoginPasswordId() {
        return loginPasswordId;
    }
    
}
