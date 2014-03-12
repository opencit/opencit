/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.password;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author jbuhacoff
 */
public class LoginPasswordId {
    private UUID userId;
    private UUID loginPasswordId;

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
