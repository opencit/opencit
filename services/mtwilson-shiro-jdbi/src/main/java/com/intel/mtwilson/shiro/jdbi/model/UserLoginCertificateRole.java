/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.jdbi.model;

import com.intel.dcsg.cpg.io.UUID;

/**
  login_certificate_id uuid NOT NULL,
  role_id uuid NOT NULL
 *
 * @author jbuhacoff
 */
public class UserLoginCertificateRole {
    private UUID loginCertificateId;
    private UUID roleId;

    public UUID getLoginCertificateId() {
        return loginCertificateId;
    }

    public void setLoginCertificateId(UUID loginCertificateId) {
        this.loginCertificateId = loginCertificateId;
    }

    
    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
    
    
}
