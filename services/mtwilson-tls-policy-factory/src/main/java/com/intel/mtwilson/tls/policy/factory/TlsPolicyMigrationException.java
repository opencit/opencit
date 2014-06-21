/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.dcsg.cpg.net.Hostname;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyMigrationException extends RuntimeException {
    private Hostname hostname;
    
    public TlsPolicyMigrationException() {
        super();
    }

    public TlsPolicyMigrationException(String message) {
        super(message);
    }

    public TlsPolicyMigrationException(String message, Hostname hostname) {
        super(message);
        this.hostname = hostname;
    }
    
    public TlsPolicyMigrationException(Throwable cause, Hostname hostname) {
        super(cause);
        this.hostname = hostname;
    }

    public TlsPolicyMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TlsPolicyMigrationException(String message, Throwable cause, Hostname hostname) {
        super(message, cause);
        this.hostname = hostname;
    }

    public Hostname getHostname() {
        return hostname;
    }
    
    
}
