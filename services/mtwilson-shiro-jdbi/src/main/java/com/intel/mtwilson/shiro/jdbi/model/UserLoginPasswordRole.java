/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi.model;

import com.intel.dcsg.cpg.io.UUID;

/**
  login_password_id uuid NOT NULL,
  role_id uuid NOT NULL
 *
 * @author jbuhacoff
 */
public class UserLoginPasswordRole {
    private UUID loginPasswordId;
    private UUID roleId;

    public UUID getLoginPasswordId() {
        return loginPasswordId;
    }

    public void setLoginPasswordId(UUID loginPasswordId) {
        this.loginPasswordId = loginPasswordId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
    
    
}
