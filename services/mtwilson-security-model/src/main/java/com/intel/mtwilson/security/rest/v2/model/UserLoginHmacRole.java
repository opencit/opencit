/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.security.rest.v2.model;

import com.intel.dcsg.cpg.io.UUID;

/**
  login_hmac_id uuid NOT NULL,
  role_id uuid NOT NULL
 *
 * @author jbuhacoff
 */
public class UserLoginHmacRole {
    private UUID loginHmacId;
    private UUID roleId;

    public UUID getLoginHmacId() {
        return loginHmacId;
    }

    public void setLoginHmacId(UUID loginHmacId) {
        this.loginHmacId = loginHmacId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
    
    
}
