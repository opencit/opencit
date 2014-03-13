/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import com.intel.dcsg.cpg.io.UUID;

/**
 *
 * @author jbuhacoff
 */
public class LoginCertificateId {
    private UUID userId;
    private UUID loginCertificateId;

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
