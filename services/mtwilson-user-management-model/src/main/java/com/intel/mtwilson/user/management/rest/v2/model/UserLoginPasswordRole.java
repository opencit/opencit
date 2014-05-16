/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;

/**
  login_password_id uuid NOT NULL,
  role_id uuid NOT NULL
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_login_password_role")
public class UserLoginPasswordRole extends Document {
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
