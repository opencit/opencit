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
public class TlsPolicyNotAllowedException extends UnsupportedOperationException {
    private InternetAddress hostname;
    
    public TlsPolicyNotAllowedException(InternetAddress hostname) {
        super();
        this.hostname = hostname;
    }

    public TlsPolicyNotAllowedException(String message) {
        super(message);
    }

    public TlsPolicyNotAllowedException(String message, InternetAddress hostname) {
        super(message);
        this.hostname = hostname;
    }
    
    public TlsPolicyNotAllowedException(Throwable cause, InternetAddress hostname) {
        super(cause);
        this.hostname = hostname;
    }

    public TlsPolicyNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TlsPolicyNotAllowedException(String message, Throwable cause, InternetAddress hostname) {
        super(message, cause);
        this.hostname = hostname;
    }

    public InternetAddress getInternetAddress() {
        return hostname;
    }
    
    
}
