/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;

/**
 *
 * @author jbuhacoff
 */
public class LoginCertificateId implements Serializable {
    private static final long serialVersionUID = 9454256276149L;
    private UUID userId;
    private UUID loginCertificateId;

    protected LoginCertificateId() { }
    
    public LoginCertificateId(UUID userId, UUID loginCertificateId) {
        this.userId = userId;
        this.loginCertificateId = loginCertificateId;
    }

    public UUID getUserId() {
        return userId;
    }

    
    public UUID getLoginCertificateId() {
        return loginCertificateId;
    }
    
}
