/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tls.policy.factory;

import com.intel.dcsg.cpg.net.InternetAddress;

/**
 *
 * @author jbuhacoff
 */
public class TlsPolicyNotFoundException extends IllegalArgumentException {
    private InternetAddress hostname;
    
    public TlsPolicyNotFoundException(InternetAddress hostname) {
        super();
        this.hostname = hostname;
    }

    public TlsPolicyNotFoundException(String message) {
        super(message);
    }

    public TlsPolicyNotFoundException(String message, InternetAddress hostname) {
        super(message);
        this.hostname = hostname;
    }
    
    public TlsPolicyNotFoundException(Throwable cause, InternetAddress hostname) {
        super(cause);
        this.hostname = hostname;
    }

    public TlsPolicyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TlsPolicyNotFoundException(String message, Throwable cause, InternetAddress hostname) {
        super(message, cause);
        this.hostname = hostname;
    }

    public InternetAddress getInternetAddress() {
        return hostname;
    }
    
    
}
