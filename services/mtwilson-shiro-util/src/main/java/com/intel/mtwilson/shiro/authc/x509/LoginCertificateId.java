/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.authc.x509;

import com.intel.dcsg.cpg.io.UUID;
import java.io.Serializable;
import java.security.Principal;

/**
 * A principal indicating the current request has been authenticated using
 * an X509 certificate.
 * 
 * @author jbuhacoff
 */
public class LoginCertificateId implements Serializable, Principal {
    private static final long serialVersionUID = 9454256276149L;
    private String username;
    private UUID userId;
    private UUID loginCertificateId;

    protected LoginCertificateId() { }
    
    public LoginCertificateId(String username, UUID userId, UUID loginCertificateId) {
        this.username = username;
        this.userId = userId;
        this.loginCertificateId = loginCertificateId;
    }

    public UUID getUserId() {
        return userId;
    }

    
    public UUID getLoginCertificateId() {
        return loginCertificateId;
    }

    @Override
    public String getName() {
        return String.format("%s [%s] certificate [%s]", username, userId, loginCertificateId);
    }
    
}
