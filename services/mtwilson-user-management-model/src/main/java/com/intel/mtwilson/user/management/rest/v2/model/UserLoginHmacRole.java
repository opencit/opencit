/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;

/**
  login_hmac_id uuid NOT NULL,
  role_id uuid NOT NULL
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_login_hmac_role")
public class UserLoginHmacRole extends Document {
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
