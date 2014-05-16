/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.user.management.rest.v2.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.Document;

/**
  login_certificate_id uuid NOT NULL,
  role_id uuid NOT NULL
 *
 * @author jbuhacoff
 */
@JacksonXmlRootElement(localName="user_login_certificate_role")
public class UserLoginCertificateRole extends Document {
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
